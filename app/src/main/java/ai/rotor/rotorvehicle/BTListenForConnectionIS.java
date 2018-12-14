package ai.rotor.rotorvehicle;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;

public class BTListenForConnectionIS extends IntentService {

    static String VEHICLE_NAME = "RPi3";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BTListenForConnectionIS(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("BTListenForConnectionIS", "onHandleIntent");
        try {
            Log.d("BTListenForConnectionIS", "Inside try statement...");
            BluetoothServerSocket socket = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord("rotor.ai", RotorUtils.ROTOR_UUID);
            BluetoothSocket result = socket.accept();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    static Intent makeIntent(Context context) {
        return new Intent(context, BTListenForConnectionIS.class);
    }
}
