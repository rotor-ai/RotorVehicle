package ai.rotor.rotorvehicle;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.lang.reflect.Method;
import java.util.Set;

public class MainActivity extends Activity {
    private static final String TAG = "Debug";
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_PAIR_BT = 3;
    private static final int DISCOVERABLE_DURATION = 30;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mPairedBTDevice;
    private Set<BluetoothDevice> mPairedDevices;
    private BluetoothService mBluetoothService;
    private Boolean connected;
    private Timber.DebugTree debugTree = new Timber.DebugTree();

    @BindView(R.id.pairBtn) Button mPairBtn;
    @BindView(R.id.statusTv) TextView mStatusTv;
    @BindView(R.id.commandTv) TextView mCommandTv;
    @BindView(R.id.pairingProgressBar) ProgressBar mPairingProgressBar;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        private static final String TAG = "Debug";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Timber.d(action);
            if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
                int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                Timber.d("Scan mode value: " + String.valueOf(scanMode));
                if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    showPairing();
                } else if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
                    hideProgress();
                    mPairedDevices = mBluetoothAdapter.getBondedDevices();
                    if (mPairedDevices == null || mPairedDevices.size() == 0) {
                        showDisabled();
                    } else if (mPairedDevices.size() == 1 && !connected) {
                        mPairedBTDevice = mPairedDevices.iterator().next();
                        showEnabled();
                    }
                }
            }

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Timber.d("BroadcastReceiver: BOND_BONDED.");
                    mPairedBTDevice = mDevice;
                    showEnabled();
                    hideProgress();
                    mBluetoothService = new BluetoothService();
                    mBluetoothService.startClient(MainActivity.this);
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Timber.d("BroadcastReceiver: BOND_BONDING.");
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Timber.d("BroadcastReceiver: BOND_NONE.");
                    mPairedDevices = mBluetoothAdapter.getBondedDevices();
                    if (mPairedDevices == null || mPairedDevices.size() == 0) {
                        showDisabled();
                    } else if (mPairedDevices.size() == 1) {
                        mPairedBTDevice = mPairedDevices.iterator().next();
                        showEnabled();
                    }
                }
            }

            if (action.equals("streamsAcquired")) {
                connected = true;
                showConnected();
            }

            if (action.equals("disconnected")) {
                connected = false;
                showEnabled();
            }

            if (action.equals("messageReceived")) {
                String cmd = intent.getStringExtra("cmd");
                mCommandTv.setText(cmd);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Timber.plant(debugTree);

        Timber.d("onCreate, thread ID: " + Thread.currentThread().getId());

        // Setup GUI
        mPairingProgressBar.setVisibility(View.INVISIBLE);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.setName("Vehicle");

        mPairedDevices = mBluetoothAdapter.getBondedDevices();
        if (mPairedDevices == null || mPairedDevices.size() == 0) {
            showDisabled();
        } else if (mPairedDevices.size() == 1) {
            mPairedBTDevice = mPairedDevices.iterator().next();
            showEnabled();
        } else {
            showMultipleDevices();
        }

        connected = false;

        mPairBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPairedDevices = mBluetoothAdapter.getBondedDevices();
                if (mPairedDevices.size() > 0) {
                    for (BluetoothDevice device : mPairedDevices) {
                        unpairDevice(device);
                    }
                    showDisabled();
                } else {
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        Timber.d("Starting enabling activity");
                    } else {
                        makeDiscoverable();
                    }

                }
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction("streamsAcquired");
        filter.addAction("disconnected");
        filter.addAction("messageReceived");

        registerReceiver(mReceiver, filter);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
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
                makeDiscoverable();
            }
        } else if (requestCode == REQUEST_PAIR_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Timber.d("OnActivityResult, discovery cancelled");
                showDisabled();
                hideProgress();
            } else if (resultCode == Activity.RESULT_OK) {
                showPairing();
            }
        }
    }

    private void makeDiscoverable() {
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
        startActivityForResult(discoverableIntent, REQUEST_PAIR_BT);
    }

    private void showPairing() {
        Timber.d("Show Pairing");
        mStatusTv.setText("starting discoverability...");
        mStatusTv.setTextColor(Color.GRAY);
        mPairingProgressBar.setVisibility(View.VISIBLE);

        mPairBtn.setEnabled(false);
    }

    private void showEnabled() {
        String name = mPairedBTDevice.getName();
        mStatusTv.setText("Paired to " + name + ", waiting for connection...");
        mStatusTv.setTextColor(Color.BLUE);
        mPairingProgressBar.setVisibility(View.INVISIBLE);

        mPairBtn.setText("UNPAIR");
        mPairBtn.setEnabled(true);
        mCommandTv.setVisibility(View.VISIBLE);
        mCommandTv.setText("");
    }

    private void showDisabled() {
        mStatusTv.setText("UNPAIRED");
        mStatusTv.setTextColor(Color.GRAY);

        mPairBtn.setText("PAIR");
        mPairBtn.setEnabled(true);
        mCommandTv.setVisibility(View.INVISIBLE);
    }

    private void showMultipleDevices() {
        mStatusTv.setText("Too many paired devices!");
        mStatusTv.setTextColor(Color.RED);

        mPairBtn.setText("UNPAIR");
        mCommandTv.setVisibility(View.INVISIBLE);
    }

    private void showConnected() {
        String name = mPairedBTDevice.getName();
        mStatusTv.setText("Connected to " + name);
        mStatusTv.setTextColor(Color.BLUE);
        mPairingProgressBar.setVisibility(View.INVISIBLE);

        mPairBtn.setText("UNPAIR");
        mPairBtn.setEnabled(true);
        mCommandTv.setVisibility(View.VISIBLE);
        mCommandTv.setText("");
    }

    private void hideProgress() {
        mPairingProgressBar.setVisibility(View.INVISIBLE);
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Timber.e(e.getMessage());
        }
    }

}

