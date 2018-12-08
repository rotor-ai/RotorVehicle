package ai.rotor.rotorvehicle;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.bluetooth.BluetoothConfigManager;
import com.google.android.things.bluetooth.BluetoothConnectionManager;
import com.google.android.things.bluetooth.BluetoothPairingCallback;
import com.google.android.things.bluetooth.PairingParams;

import timber.log.Timber;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "Starting up...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothAdapter.getDefaultAdapter().setName("Vehicle");
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothConfigManager btConfigManager = BluetoothConfigManager.getInstance();

        btConfigManager.setIoCapability(BluetoothConfigManager.IO_CAPABILITY_IO);
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "Resuming...");
    }

    private class BTPairingCallback implements BluetoothPairingCallback {
        @Override
        public void onPairingInitiated(BluetoothDevice bluetoothDevice, PairingParams pairingParams) {
            BluetoothConnectionManager.getInstance().finishPairing(bluetoothDevice);
        }
    }
}
