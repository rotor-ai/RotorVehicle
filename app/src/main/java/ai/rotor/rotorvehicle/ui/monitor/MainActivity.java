package ai.rotor.rotorvehicle.ui.monitor;

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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ai.rotor.rotorvehicle.R;
import ai.rotor.rotorvehicle.rotor_ctl.RotorCtlService;
import ai.rotor.rotorvehicle.ai_agent.RotorAiService;
import ai.rotor.rotorvehicle.dagger.DaggerRotorComponent;
import ai.rotor.rotorvehicle.data.Blackbox;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

import static ai.rotor.rotorvehicle.RotorUtils.ROTOR_TX_RX_CHARACTERISTIC_UUID;
import static ai.rotor.rotorvehicle.RotorUtils.ROTOR_TX_RX_SERVICE_UUID;

public class MainActivity extends Activity {
    private static final int DISCOVERABLE_DURATION = 30;

    private BluetoothManager mBluetoothManager;
    private Timber.DebugTree debugTree = new Timber.DebugTree();
    Disposable blackboxSubscription;
    private RotorCtlService mRotorCtlService;
    private Blackbox blackbox;
    private BlackboxRecyclerAdapter blackboxRecyclerAdapter;
    private boolean mAutoMode;

    //BLE
    private RotorGattServerCallback mGattServerCallback;
    private BluetoothLeAdvertiser mAdvertiser;
    private AdvertiseData mAdData;
    private AdvertiseSettings mAdSettings;
    private BluetoothGattServer mGattServer;
    private BluetoothGattService mGattService;
    private AdvertiseCallback advertiseCallback;

    @BindView(R.id.LogRecyclerView) RecyclerView logRecyclerView;
    @BindView(R.id.autoBtn) Button mAutoBtn;
    @BindView(R.id.imageView) ImageView mImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        blackbox = DaggerRotorComponent.create().blackbox();

        blackboxRecyclerAdapter = new BlackboxRecyclerAdapter(blackbox);
        logRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        logRecyclerView.setAdapter(blackboxRecyclerAdapter);

        Timber.plant(debugTree);
        Timber.plant(blackbox);

        blackboxSubscription = blackbox.getSubject()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        blackboxRecyclerAdapter.notifyDataSetChanged();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Timber.d(throwable.toString());
                    }
                });

//        blackboxSubscription = blackbox.getSubject()
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<String>() {
//            @Override
//            public void accept(String s) {
//                debugTextView.setText(String.format("%s\n%s", debugTextView.getText(), s));
//            }
//        }, new Consumer<Throwable>() {
//            @Override
//            public void accept(Throwable throwable) throws Exception {
//                Timber.d("blackbox error: " + throwable.getMessage());
//            }
//        });

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothManager.getAdapter().setName("Vehicle");

        Timber.d("onCreate, thread ID: %s", Thread.currentThread().getId());
        Timber.d("checking for BLE support...");
        Timber.d("supports BLE: %s", getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE));
        Timber.d("checking for MultiAdvertisement support...");
        Timber.d("supports multi advertisement: %s", doesSupportMultiAdvertisement());

        // Start the Rotor control service thread
        mRotorCtlService = new RotorCtlService();
        mRotorCtlService.run();

        setupGATTServer();
        beginAdvertisement();

        // Ai Agent Setup
        mAutoMode = false;
        final RotorAiService mRotorAiService = new RotorAiService(this, mImageView, mRotorCtlService);
        mRotorAiService.run();

        mAutoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mAutoMode) {
                    mRotorAiService.startAutoMode();
                    showAuto();
                } else {
                    mRotorAiService.stopAutoMode();
                    showManual();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("OnActivityResult request code: " + String.valueOf(requestCode) + ", result code: " + String.valueOf(resultCode));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //unsubscribe RxJava stuff
        blackboxSubscription.dispose();

        //Stop advertising
        mGattServer.close();
        mAdvertiser.stopAdvertising(advertiseCallback);
    }

    private void setupGATTServer() {
        mAdvertiser = mBluetoothManager.getAdapter().getBluetoothLeAdvertiser();
        mGattServerCallback = new RotorGattServerCallback();
        mGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);
        mGattService = new BluetoothGattService(ROTOR_TX_RX_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(ROTOR_TX_RX_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE, BluetoothGattCharacteristic.PERMISSION_WRITE);
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
            Timber.d("onConnectionStateChange: %s", newState);
        }


        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Timber.d("onCharacteristicReadRequest ");
            String s = "this is a reponse";
            byte[] bytes = s.getBytes();
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, bytes);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            String s = new String(value, 0, value.length);
            Timber.d("onCharacteristicWriteRequest: %s", s);
            mRotorCtlService.sendCommand(s);
        }
    }

    private void showAuto() {
        String homedText = getString(R.string.ui_home);
        mAutoBtn.setText(homedText);
        mAutoMode = true;
    }

    private void showManual() {
        String autoText = getString(R.string.ui_auto);
        mAutoBtn.setText(autoText);
        mAutoMode = false;
    }

}