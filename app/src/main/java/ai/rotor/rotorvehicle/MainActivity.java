package ai.rotor.rotorvehicle;

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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static ai.rotor.rotorvehicle.RotorCtlService.State.AUTONOMOUS;
import static ai.rotor.rotorvehicle.RotorCtlService.State.HOMED;
import static ai.rotor.rotorvehicle.RotorCtlService.State.MANUAL;
import static ai.rotor.rotorvehicle.RotorUtils.ROTOR_TX_RX_SERVICE_UUID;

public class MainActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_PAIR_BT = 3;
    private static final int DISCOVERABLE_DURATION = 30;

    private BluetoothManager mBluetoothManager;
    private BluetoothDevice mPairedBTDevice;
    private RotorGattServerCallback mGattServerCallback;
    private Set<BluetoothDevice> mPairedDevices;
    private BluetoothService mBluetoothService;
    private Boolean connected = false;
    private Timber.DebugTree debugTree = new Timber.DebugTree();
    private final BroadcastReceiver mReceiver = new RotorBroadcastReceiver();
    private RotorCtlService mRotorCtlService;

    //BLE
    private BluetoothLeAdvertiser mAdvertiser;
    private AdvertiseData mAdData;
    private AdvertiseSettings mAdSettings;
    private BluetoothGattServer mGattServer;
    private AdvertiseCallback advertiseCallback;

    @BindView(R.id.statusTv)
    TextView mStatusTv;
    @BindView(R.id.commandTv)
    TextView mCommandTv;
    @BindView(R.id.pairingProgressBar)
    ProgressBar mPairingProgressBar;
    @BindView(R.id.autoBtn)
    Button mAutoBtn;
    @BindView(R.id.autoStatusTv)
    TextView mAutoStatusTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Timber.plant(debugTree);
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothManager.getAdapter().setName("Vehicle");
        mPairedDevices = mBluetoothManager.getAdapter().getBondedDevices();

        Timber.d("onCreate, thread ID: %s", Thread.currentThread().getId());
        Timber.d("checking for BLE support...");
        Timber.d("supports BLE: %s", doesDeviceSupportBLE());
        Timber.d("checking for MultiAdvertisement support...");
        Timber.d("supports multi advertisement: %s", doesSupportMultiAdvertisement());

        // Setup GUI
        mPairingProgressBar.setVisibility(View.INVISIBLE);
        if (mPairedDevices == null || mPairedDevices.size() == 0) {
            showDisabled();
        } else {
            mPairedBTDevice = mPairedDevices.iterator().next();
            showEnabled();
        }

        mAutoBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mRotorCtlService.getRotorState() == HOMED) {
                            goToMode(AUTONOMOUS);
                        } else {
                            goToMode(HOMED);
                        }
                    }
                }
        );

        // Start the Rotor control service thread
        mRotorCtlService = new RotorCtlService(this);
        mRotorCtlService.run();
        updateAutoBtnStyle();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(RotorUtils.ACTION_STREAMS_ACQUIRED);
        filter.addAction(RotorUtils.ACTION_DISCONNECTED);
        filter.addAction(RotorUtils.ACTION_MESSAGE_RECEIVED);

        registerReceiver(mReceiver, filter);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        setupGATTServer();
        beginAdvertisement();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("OnActivityResult request code: " + String.valueOf(requestCode) + ", result code: " + String.valueOf(resultCode));

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Timber.d("OnActivityResult, enabling cancelled");
                showDisabled();
            } else {
                Timber.d("OnActivityResult, enabled");
                //makeDiscoverable();
            }
        } else if (requestCode == REQUEST_PAIR_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Timber.d("OnActivityResult, discovery cancelled");
                showDisabled();
                mPairingProgressBar.setVisibility(View.INVISIBLE);
            } else if (resultCode == Activity.RESULT_OK) {
                showPairing();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);

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
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .build();

        mAdData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(new ParcelUuid(ROTOR_TX_RX_SERVICE_UUID))
                .build();

        advertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                showPairing();
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

     private void showPairing() {
        Timber.d("Show Pairing");
        mStatusTv.setText(getString(R.string.ui_starting_discoverability));
        mStatusTv.setTextColor(Color.GRAY);
        mPairingProgressBar.setVisibility(View.VISIBLE);
    }

    private void showEnabled() {
        String name = mPairedBTDevice.getName();
        mStatusTv.setText(String.format("Bluetooth state: Paired to %s, waiting for connection...", name));
        mStatusTv.setTextColor(Color.BLUE);
        mPairingProgressBar.setVisibility(View.INVISIBLE);

        mCommandTv.setVisibility(View.VISIBLE);
        mCommandTv.setText("");
    }

    private void showDisabled() {
        mStatusTv.setText("Bluetooth state: UNPAIRED");
        mStatusTv.setTextColor(Color.GRAY);

        mCommandTv.setVisibility(View.INVISIBLE);
    }

    private void showConnected() {
        String name = mPairedBTDevice.getName();
        mStatusTv.setText(String.format("Bluetooth state: Connected to %s", name));
        mStatusTv.setTextColor(Color.BLUE);
        mPairingProgressBar.setVisibility(View.INVISIBLE);

        mCommandTv.setVisibility(View.VISIBLE);
        mCommandTv.setText("");
    }

    private void updateAutoBtnStyle() {
        mAutoStatusTv.setText(String.format("Rotor State: %s", mRotorCtlService.getRotorState()));
        mAutoBtn.setText(mRotorCtlService.getRotorState().name());
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private void goToMode(RotorCtlService.State newState) {
        Timber.d("Changing to: %s", newState.name());
        mRotorCtlService.setState(newState);
        updateAutoBtnStyle();
    }

    private boolean doesDeviceSupportBLE() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    private boolean doesSupportMultiAdvertisement() {
        return mBluetoothManager.getAdapter().isMultipleAdvertisementSupported();
    }

    class RotorBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Timber.d("In onReceive, action: %s", action);
            if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
                int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                Timber.d("Scan mode value: %s", String.valueOf(scanMode));

                switch (scanMode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        showPairing();
                        goToMode(HOMED);
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        mPairingProgressBar.setVisibility(View.INVISIBLE);
                        mPairedDevices = mBluetoothManager.getAdapter().getBondedDevices();
                        if (mPairedDevices == null || mPairedDevices.size() == 0) {
                            showDisabled();
                        } else if (mPairedDevices.size() == 1 && !connected) {
                            mPairedBTDevice = mPairedDevices.iterator().next();
                            showEnabled();
                        }
                        break;
                }
            }

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Timber.d("BroadcastReceiver: BOND_BONDED.");
                    mPairedBTDevice = mDevice;
                    showEnabled();
                    mPairingProgressBar.setVisibility(View.INVISIBLE);
                    mBluetoothService = new BluetoothService();
                    mBluetoothService.startClient(MainActivity.this);
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Timber.d("BroadcastReceiver: BOND_BONDING.");
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Timber.d("BroadcastReceiver: BOND_NONE.");
                    mPairedDevices = mBluetoothManager.getAdapter().getBondedDevices();
                    if (mPairedDevices == null || mPairedDevices.size() == 0) {
                        showDisabled();
                    } else if (mPairedDevices.size() == 1) {
                        mPairedBTDevice = mPairedDevices.iterator().next();
                        showEnabled();
                    }
                }
            }

            if (RotorUtils.ACTION_STREAMS_ACQUIRED.equals(action)) {
                connected = true;
                showConnected();
                goToMode(MANUAL);
            }

            if (RotorUtils.ACTION_DISCONNECTED.equals(action)) {
                connected = false;
                showEnabled();
                goToMode(HOMED);
            }

            if (RotorUtils.ACTION_MESSAGE_RECEIVED.equals(action)) {
                String cmd = intent.getStringExtra(RotorUtils.EXTRA_CMD);

                if (cmd.charAt(0) == '_') {
                    if (cmd.charAt(1) == 'A') {
                        goToMode(HOMED);
                        goToMode(AUTONOMOUS);
                    } else {
                        goToMode(HOMED);
                        goToMode(MANUAL);
                    }
                    return;
                }
                mRotorCtlService.sendCommand(cmd);
                mCommandTv.setText(cmd);
            }
        }
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