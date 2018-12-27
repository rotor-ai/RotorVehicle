package ai.rotor.rotorvehicle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

import static ai.rotor.rotorvehicle.RotorUtils.ROTOR_UUID;

public class AcceptThread extends Thread {
    private static final String TAG = "AcceptThread Debug: ";
    private final static String NAME = "AcceptThreadService";
    private final BluetoothServerSocket mServerSocket;
    private BluetoothAdapter mBluetoothAdapter;

    public AcceptThread() {
        Log.d(TAG, "AcceptThread initializing");
        BluetoothServerSocket tmp = null;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, ROTOR_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
        }
        mServerSocket = tmp;
    }

    public void run() {
        Log.d(TAG, "AcceptThread running, thread ID: " + Thread.currentThread().getId());
        BluetoothSocket socket = null;

        while(true) {
            Log.d(TAG, "in loop...");
            try {
                socket = mServerSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                //manageMyConnectedSocket(socket);
                try {
                    mServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

}
