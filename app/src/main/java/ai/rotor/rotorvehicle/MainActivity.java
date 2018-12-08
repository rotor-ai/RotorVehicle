package ai.rotor.rotorvehicle;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;

import com.google.android.things.bluetooth.BluetoothClassFactory;
import com.google.android.things.bluetooth.BluetoothConfigManager;
import com.google.android.things.bluetooth.BluetoothConnectionManager;
import com.google.android.things.bluetooth.BluetoothPairingCallback;
import com.google.android.things.bluetooth.PairingParams;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {
    private BTPairingCallback pairingCallback = new BTPairingCallback();
    BluetoothAdapter btAdapter;
    static int REQUEST_TURN_BT_ON = 1;
    static int REQUEST_MAKE_DISCOVERABLE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "Starting up...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothAdapter.getDefaultAdapter().setName("Vehicle");
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothConfigManager btConfigManager = BluetoothConfigManager.getInstance();

        btConfigManager.setIoCapability(BluetoothConfigManager.IO_CAPABILITY_IO);
        btConfigManager.setBluetoothClass(BluetoothClassFactory.build(BluetoothClass.Service.INFORMATION, BluetoothClass.Device.TOY_VEHICLE));

        Log.d("MainActivity","Checking if Bluetooth is on...");
        if (!btAdapter.isEnabled()) {
            Log.d("MainActivity", "Turning Bluetooth on...");
            turnBTRadioON();
        } else {
            Log.d("MainActivity", "Starting discoverability...");
            startDiscoverability();
        }

        BluetoothConnectionManager.getInstance().registerPairingCallback(pairingCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TURN_BT_ON) {
            startDiscoverability();
        }
        else if (requestCode == REQUEST_MAKE_DISCOVERABLE) {
            Log.d("MainActivity", "Starting BTListenForConnectionIS");
            startService(BTListenForConnectionIS.makeIntent(this));
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        BluetoothConnectionManager.getInstance().unregisterPairingCallback(pairingCallback);
        super.onDestroy();
    }

    private class BTPairingCallback implements BluetoothPairingCallback {
        @Override
        public void onPairingInitiated(BluetoothDevice bluetoothDevice, PairingParams pairingParams) {
            BluetoothConnectionManager.getInstance().finishPairing(bluetoothDevice);
            Log.d("MainActivity", "Pairing Initiated...");
        }
    }

    private void turnBTRadioON() {
        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_TURN_BT_ON);
    }

    private void startDiscoverability() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60);
        startActivityForResult(intent, REQUEST_MAKE_DISCOVERABLE);
    }
}
