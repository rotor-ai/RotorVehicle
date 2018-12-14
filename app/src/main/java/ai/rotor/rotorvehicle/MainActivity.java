package ai.rotor.rotorvehicle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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
    private static final String TAG = "Debug - MainActivity";
    private BluetoothAdapter myBluetoothAdapter;
    private BluetoothConnectionManager myBluetoothConnectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up bluetooth
        BluetoothConfigManager btConfigManager = BluetoothConfigManager.getInstance();
        btConfigManager.setIoCapability(BluetoothConfigManager.IO_CAPABILITY_IO);
        btConfigManager.setBluetoothClass(BluetoothClassFactory.build(BluetoothClass.Service.INFORMATION, BluetoothClass.Device.TOY_VEHICLE));
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        myBluetoothAdapter.setName("Vehicle");
        myBluetoothConnectionManager = BluetoothConnectionManager.getInstance();
        myBluetoothConnectionManager.registerPairingCallback(myBluetoothPairingCallback);

        // Make discoverable
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
        startActivity(discoverableIntent);

    }

    private void alertTest() {
        Log.d(TAG, "alert being created");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        Log.d(TAG, "alert builder created");
        alertDialogBuilder.create();
        Log.d(TAG, "alert created");
        /*alertDialog.setTitle("Alert");
        alertDialog.setMessage("Alert message to be shown");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myBluetoothConnectionManager.unregisterPairingCallback(myBluetoothPairingCallback);
    }

    private BluetoothPairingCallback myBluetoothPairingCallback = new BluetoothPairingCallback() {

        @Override
        public void onPairingInitiated(BluetoothDevice initiatingDevice, PairingParams pairingParams) {
            Log.d(TAG, "pairing initiated");
            alertTest();
        }
    };

    // NOT USING THIS FUNCTION CURRENTLY
    private void pairingDialog(String title, String message, final String cancelString, final String acceptString) {
        Log.d(TAG, "Opening Pairing Dialog");
        android.app.AlertDialog.Builder builderSingle = new android.app.AlertDialog.Builder(MainActivity.this);
        builderSingle.setTitle(title);
        builderSingle.setMessage(message);

        builderSingle.setNegativeButton(cancelString, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "pairing cancelled");
            }
        });

        builderSingle.setPositiveButton(acceptString, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "pairing accepted");
            }
        });

        AlertDialog dialog = builderSingle.create();

        dialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
