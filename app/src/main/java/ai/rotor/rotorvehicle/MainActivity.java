package ai.rotor.rotorvehicle;

import android.app.Activity;
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
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.TextView;

import ai.rotor.rotorvehicle.data.Blackbox;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static ai.rotor.rotorvehicle.RotorUtils.ROTOR_TX_RX_SERVICE_UUID;

public class MainActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_PAIR_BT = 3;
    private static final int DISCOVERABLE_DURATION = 30;

    private BluetoothManager mBluetoothManager;
    private Timber.DebugTree debugTree = new Timber.DebugTree();
    private Blackbox blackbox = new Blackbox();
    private RotorCtlService mRotorCtlService;

    //BLE
    private RotorGattServerCallback mGattServerCallback;
    private BluetoothLeAdvertiser mAdvertiser;
    private AdvertiseData mAdData;
    private AdvertiseSettings mAdSettings;
    private BluetoothGattServer mGattServer;
    private AdvertiseCallback advertiseCallback;

    @BindView(R.id.debugText)
    TextView debugTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Timber.plant(debugTree);
        Timber.plant(blackbox);
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothManager.getAdapter().setName("Vehicle");

        Timber.d("onCreate, thread ID: %s", Thread.currentThread().getId());
        Timber.d("checking for BLE support...");
        Timber.d("supports BLE: %s", getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE));
        Timber.d("checking for MultiAdvertisement support...");
        Timber.d("supports multi advertisement: %s", doesSupportMultiAdvertisement());

        // Start the Rotor control service thread
        //mRotorCtlService = new RotorCtlService(this);
        //mRotorCtlService.run();

        setupGATTServer();
        beginAdvertisement();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("OnActivityResult request code: " + String.valueOf(requestCode) + ", result code: " + String.valueOf(resultCode));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Stop advertising
        mGattServer.close();
        mAdvertiser.stopAdvertising(advertiseCallback);
    }

    private void setupGATTServer() {
        mAdvertiser = mBluetoothManager.getAdapter().getBluetoothLeAdvertiser();
        mGattServerCallback = new RotorGattServerCallback();
        mGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);
        mGattServer.addService(new BluetoothGattService(ROTOR_TX_RX_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY));

        mAdSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(180000)//1 minute
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .build();

        mAdData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(new ParcelUuid(ROTOR_TX_RX_SERVICE_UUID))
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
    }

    private void beginAdvertisement() {
        Timber.d("Beginning advertisement");
        mAdvertiser.startAdvertising(mAdSettings, mAdData, advertiseCallback);
    }

    private void goToMode(RotorCtlService.State newState) {
        Timber.d("Changing to: %s", newState.name());
        mRotorCtlService.setState(newState);
    }

    private boolean doesSupportMultiAdvertisement() {
        return mBluetoothManager.getAdapter().isMultipleAdvertisementSupported();
    }

    public class RotorGattServerCallback extends BluetoothGattServerCallback {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.d("STUDEBUG", "onConnectionStateChange: " + newState);
        }


        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.d("STUDEBUG", "onCharacteristicReadRequest " + characteristic.toString());
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
        }
    }

}