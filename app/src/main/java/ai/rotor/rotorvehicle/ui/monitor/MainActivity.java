package ai.rotor.rotorvehicle.ui.monitor;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ai.rotor.rotorvehicle.R;
import ai.rotor.rotorvehicle.RotorUtils;
import ai.rotor.rotorvehicle.agent.RotorAiService;
import ai.rotor.rotorvehicle.dagger.DaggerRotorComponent;
import ai.rotor.rotorvehicle.data.Blackbox;
import ai.rotor.rotorvehicle.ctl.RotorCtlService;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements LifecycleOwner {
    final private static int REQUEST_CAMERA_PERMISSION = 4545;
    final private static int PERMISSION_REQUEST_DELAY_MILLIS = 3000;

    private BluetoothManager mBluetoothManager;
    private Timber.DebugTree debugTree = new Timber.DebugTree();
    Disposable blackboxSubscription;
    private RotorCtlService mRotorCtlService;
    //private RotorAiService mRotorAiService;
    private BlackboxRecyclerAdapter blackboxRecyclerAdapter;

    private int ENABLE_BT_REQUEST_CODE = 1234;

    //BLE
    private RotorGattServerCallback mGattServerCallback;
    private BluetoothLeAdvertiser mAdvertiser;
    private AdvertiseData mAdData;
    private AdvertiseSettings mAdSettings;
    private BluetoothGattServer mGattServer;
    private BluetoothGattService mGattService;
    private AdvertiseCallback advertiseCallback;

    //image processing
    private PreviewConfig previewConfig = new PreviewConfig
            .Builder()
            .setTargetAspectRatio(new Rational(1,1))
            .setTargetResolution(new Size(RotorUtils.INSTANCE.getIMAGE_WIDTH(), RotorUtils.INSTANCE.getIMAGE_HEIGHT()))
            .build();

    @BindView(R.id.LogRecyclerView)
    RecyclerView logRecyclerView;
    @BindView(R.id.autoBtn)
    Button mAutoBtn;
    @BindView(R.id.viewFinder)
    TextureView mViewFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Blackbox blackbox = DaggerRotorComponent.create().blackbox();

        blackboxRecyclerAdapter = new BlackboxRecyclerAdapter(blackbox);
        logRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        logRecyclerView.setAdapter(blackboxRecyclerAdapter);

        Timber.plant(debugTree);
        Timber.plant(blackbox);

        blackboxSubscription = blackbox.getSubject()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> blackboxRecyclerAdapter.notifyDataSetChanged(), throwable -> Timber.d(throwable.toString()));

        mViewFinder.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> updateViewFinder());

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager.getAdapter() != null){
            mBluetoothManager.getAdapter().setName("Vehicle");
            Timber.d("supports BLE: %s", getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE));
            Timber.d("supports multi advertisement: %s", doesSupportMultiAdvertisement());

            //check that bluetooth is enabled
            if (!mBluetoothManager.getAdapter().isEnabled()) {
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), ENABLE_BT_REQUEST_CODE);
            }
            else {
                setupGATTServer();
            }
        }

        // Start the Rotor control service thread
        mRotorCtlService = new RotorCtlService(this);

        //mRotorAiService = new RotorAiService(this, mImageView, mRotorCtlService);

    }

    @OnClick(R.id.autoBtn)
    public void onClickAutoBtn() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
        else {
            mViewFinder.post(() -> startImageCapture());
        }

//        if (!mRotorAiService.isAutoMode()) {
//            mRotorAiService.startAutoMode();
//            showAuto();
//        } else {
//            mRotorAiService.stopAutoMode();
//            showManual();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            //mRotorAiService.run();
        }

        mRotorCtlService.run();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("OnActivityResult request code: " + requestCode + ", result code: " + resultCode);

        if (requestCode == ENABLE_BT_REQUEST_CODE && resultCode == RESULT_OK) {
            setupGATTServer();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        mRotorCtlService.stop();
        //mRotorAiService.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //unsubscribe RxJava stuff
        blackboxSubscription.dispose();

        //Stop advertising
        if (mGattServer != null) mGattServer.close();
        stopAdvertisement();
    }

    private void setupGATTServer() {
        mAdvertiser = mBluetoothManager.getAdapter().getBluetoothLeAdvertiser();
        mGattServerCallback = new RotorGattServerCallback();
        mGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);
        mGattService = new BluetoothGattService(RotorUtils.INSTANCE.getROTOR_TX_RX_SERVICE_UUID(), BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(RotorUtils.INSTANCE.getROTOR_TX_RX_CHARACTERISTIC_UUID(), BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE, BluetoothGattCharacteristic.PERMISSION_WRITE);
        characteristic.setValue(new byte[]{0x00, 0x01, 0x02, 0x03});
        mGattService.addCharacteristic(characteristic);
        mGattServer.addService(mGattService);

        mAdSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(180000)//1 minute
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .build();

        mAdData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(new ParcelUuid(RotorUtils.INSTANCE.getROTOR_TX_RX_SERVICE_UUID()))
                .build();

        advertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                Timber.d("GATT Server successfully started");
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                Timber.d("GATT Server failed to start");
            }
        };

        StartAdvertisement();
    }

    private void StartAdvertisement() {
        if (mAdvertiser != null && advertiseCallback != null) {
            Timber.d("Beginning advertisement");
            mAdvertiser.startAdvertising(mAdSettings, mAdData, advertiseCallback);
        }
    }

    private void stopAdvertisement() {
        if (mAdvertiser != null) {
            Timber.d("Stopping advertisement");
            mAdvertiser.stopAdvertising(advertiseCallback);
        }
    }

    private boolean doesSupportMultiAdvertisement() {
        return mBluetoothManager.getAdapter().isMultipleAdvertisementSupported();
    }

    public class RotorGattServerCallback extends BluetoothGattServerCallback {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Timber.d("onConnectionStateChange: %s", newState);
        }


        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Timber.d("onCharacteristicReadRequest ");
            String s = "this is a response";
            byte[] bytes = s.getBytes();
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, bytes);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            String s = new String(value, 0, value.length);
            Timber.d("Command received: %s", s);
            mRotorCtlService.sendCommand(s);
        }
    }

    private void showAuto() {
        String homedText = getString(R.string.ui_home);
        mAutoBtn.setText(homedText);
    }

    private void showManual() {
        String autoText = getString(R.string.ui_auto);
        mAutoBtn.setText(autoText);
    }

    void startImageCapture() {
        Preview preview = new Preview(previewConfig);

        preview.setOnPreviewOutputUpdateListener(previewOutput -> {
            ViewGroup parent = (ViewGroup) mViewFinder.getParent();
            parent.removeView(mViewFinder);
            parent.addView(mViewFinder, 0);

            mViewFinder.setSurfaceTexture(previewOutput.getSurfaceTexture());
            updateViewFinder();
        });

        CameraX.bindToLifecycle(this, preview);
    }

    void updateViewFinder() {

        Matrix matrix = new Matrix();

        float x = mViewFinder.getWidth() / 2f;
        float y = mViewFinder.getHeight() / 2f;

        switch (mViewFinder.getDisplay().getRotation()) {
            case Surface.ROTATION_90:
                matrix.postRotate(-90f, x, y);
                break;
            case Surface.ROTATION_180:
                matrix.postRotate(-180f, x, y);
                break;
            case Surface.ROTATION_270:
                matrix.postRotate(-270f, x, y);
                break;
            default:
                matrix.postRotate(0f, x, y);
                break;
        }
        mViewFinder.setTransform(matrix);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mViewFinder.post(() -> startImageCapture());
                }
                else {
                    //rip, permission denied :(
                }
            }
        }
    }
}