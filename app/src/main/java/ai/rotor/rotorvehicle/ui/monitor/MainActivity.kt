package ai.rotor.rotorvehicle.ui.monitor

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.ParcelUuid
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import ai.rotor.rotorvehicle.R
import ai.rotor.rotorvehicle.RotorUtils
import ai.rotor.rotorvehicle.agent.RotorAiService
import ai.rotor.rotorvehicle.dagger.DaggerRotorComponent
import ai.rotor.rotorvehicle.data.Blackbox
import ai.rotor.rotorvehicle.ctl.RotorCtlService
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import timber.log.Timber

class MainActivity : AppCompatActivity(), LifecycleOwner {

    private var mBluetoothManager: BluetoothManager? = null
    private val debugTree = Timber.DebugTree()
    lateinit var blackboxSubscription: Disposable
    private var mRotorCtlService: RotorCtlService? = null
    //private RotorAiService mRotorAiService;
    private var blackboxRecyclerAdapter: BlackboxRecyclerAdapter? = null

    private val ENABLE_BT_REQUEST_CODE = 1234

    //BLE
    private var mGattServerCallback: RotorGattServerCallback? = null
    private var mAdvertiser: BluetoothLeAdvertiser? = null
    private var mAdData: AdvertiseData? = null
    private var mAdSettings: AdvertiseSettings? = null
    private var mGattServer: BluetoothGattServer? = null
    private var mGattService: BluetoothGattService? = null
    private var advertiseCallback: AdvertiseCallback? = null

    //image processing
    private val previewConfig = PreviewConfig.Builder()
            .setTargetAspectRatio(Rational(1, 1))
            .setTargetResolution(Size(RotorUtils.IMAGE_WIDTH, RotorUtils.IMAGE_HEIGHT))
            .build()

    @BindView(R.id.LogRecyclerView)
    lateinit var logRecyclerView: RecyclerView
    @BindView(R.id.autoBtn)
    lateinit var mAutoBtn: Button
    @BindView(R.id.viewFinder)
    lateinit var mViewFinder: TextureView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        val blackbox = DaggerRotorComponent.create().blackbox()

        blackboxRecyclerAdapter = BlackboxRecyclerAdapter(blackbox)
        logRecyclerView!!.layoutManager = LinearLayoutManager(this)
        logRecyclerView!!.adapter = blackboxRecyclerAdapter

        Timber.plant(debugTree)
        Timber.plant(blackbox)

        blackboxSubscription = blackbox.subject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ s -> blackboxRecyclerAdapter!!.notifyDataSetChanged() }, { throwable -> Timber.d(throwable.toString()) })

        mViewFinder!!.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom -> updateViewFinder() }

        mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (mBluetoothManager!!.adapter != null) {
            mBluetoothManager!!.adapter.name = "Vehicle"
            Timber.d("supports BLE: %s", packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
            Timber.d("supports multi advertisement: %s", doesSupportMultiAdvertisement())

            //check that bluetooth is enabled
            if (!mBluetoothManager!!.adapter.isEnabled) {
                startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), ENABLE_BT_REQUEST_CODE)
            } else {
                setupGATTServer()
            }
        }

        // Start the Rotor control service thread
        mRotorCtlService = RotorCtlService(this)

        //mRotorAiService = new RotorAiService(this, mImageView, mRotorCtlService);

    }

    @OnClick(R.id.autoBtn)
    fun onClickAutoBtn() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            mViewFinder!!.post { startImageCapture() }
        }

        //        if (!mRotorAiService.isAutoMode()) {
        //            mRotorAiService.startAutoMode();
        //            showAuto();
        //        } else {
        //            mRotorAiService.stopAutoMode();
        //            showManual();
        //        }
    }

    override fun onResume() {
        super.onResume()

        if (this.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            //mRotorAiService.run();
        }

        mRotorCtlService!!.run()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("OnActivityResult request code: $requestCode, result code: $resultCode")

        if (requestCode == ENABLE_BT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            setupGATTServer()
        }

    }

    override fun onPause() {
        super.onPause()

        mRotorCtlService!!.stop()
        //mRotorAiService.stop();
    }

    override fun onDestroy() {
        super.onDestroy()

        //unsubscribe RxJava stuff
        blackboxSubscription.dispose()

        //Stop advertising
        if (mGattServer != null) mGattServer!!.close()
        stopAdvertisement()
    }

    private fun setupGATTServer() {
        mAdvertiser = mBluetoothManager!!.adapter.bluetoothLeAdvertiser
        mGattServerCallback = RotorGattServerCallback()
        mGattServer = mBluetoothManager!!.openGattServer(this, mGattServerCallback)
        mGattService = BluetoothGattService(RotorUtils.ROTOR_TX_RX_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val characteristic = BluetoothGattCharacteristic(RotorUtils.ROTOR_TX_RX_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE, BluetoothGattCharacteristic.PERMISSION_WRITE)
        characteristic.value = byteArrayOf(0x00, 0x01, 0x02, 0x03)
        mGattService!!.addCharacteristic(characteristic)
        mGattServer!!.addService(mGattService)

        mAdSettings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(180000)//1 minute
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .build()

        mAdData = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(ParcelUuid(RotorUtils.ROTOR_TX_RX_SERVICE_UUID))
                .build()

        advertiseCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                super.onStartSuccess(settingsInEffect)
                Timber.d("GATT Server successfully started")
            }

            override fun onStartFailure(errorCode: Int) {
                super.onStartFailure(errorCode)
                Timber.d("GATT Server failed to start")
            }
        }

        StartAdvertisement()
    }

    private fun StartAdvertisement() {
        if (mAdvertiser != null && advertiseCallback != null) {
            Timber.d("Beginning advertisement")
            mAdvertiser!!.startAdvertising(mAdSettings, mAdData, advertiseCallback)
        }
    }

    private fun stopAdvertisement() {
        if (mAdvertiser != null) {
            Timber.d("Stopping advertisement")
            mAdvertiser!!.stopAdvertising(advertiseCallback)
        }
    }

    private fun doesSupportMultiAdvertisement(): Boolean {
        return mBluetoothManager!!.adapter.isMultipleAdvertisementSupported
    }

    inner class RotorGattServerCallback : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            Timber.d("onConnectionStateChange: %s", newState)
        }


        override fun onCharacteristicReadRequest(device: BluetoothDevice, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            Timber.d("onCharacteristicReadRequest ")
            val s = "this is a response"
            val bytes = s.toByteArray()
            mGattServer!!.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, bytes)
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice, requestId: Int, characteristic: BluetoothGattCharacteristic, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            val s = String(value, 0, value.size)
            Timber.d("Command received: %s", s)
            mRotorCtlService!!.sendCommand(s)
        }
    }

    private fun showAuto() {
        val homedText = getString(R.string.ui_home)
        mAutoBtn!!.text = homedText
    }

    private fun showManual() {
        val autoText = getString(R.string.ui_auto)
        mAutoBtn!!.text = autoText
    }

    internal fun startImageCapture() {
        val preview = Preview(previewConfig)

        preview.setOnPreviewOutputUpdateListener { previewOutput ->
            val parent = mViewFinder!!.parent as ViewGroup
            parent.removeView(mViewFinder)
            parent.addView(mViewFinder, 0)

            mViewFinder!!.surfaceTexture = previewOutput.surfaceTexture
            updateViewFinder()
        }

        CameraX.bindToLifecycle(this, preview)
    }

    internal fun updateViewFinder() {

        val matrix = Matrix()

        val x = mViewFinder!!.width / 2f
        val y = mViewFinder!!.height / 2f

        when (mViewFinder!!.display.rotation) {
            Surface.ROTATION_90 -> matrix.postRotate(-90f, x, y)
            Surface.ROTATION_180 -> matrix.postRotate(-180f, x, y)
            Surface.ROTATION_270 -> matrix.postRotate(-270f, x, y)
            else -> matrix.postRotate(0f, x, y)
        }
        mViewFinder!!.setTransform(matrix)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mViewFinder!!.post { startImageCapture() }
                } else {
                    //rip, permission denied :(
                }
            }
        }
    }

    companion object {
        private val REQUEST_CAMERA_PERMISSION = 4545
        private val PERMISSION_REQUEST_DELAY_MILLIS = 3000
    }
}