package ai.rotor.rotorvehicle;

import android.bluetooth.BluetoothSocket;

public class ManageConnectionThread extends Thread {
    private final BluetoothSocket mSocket;

    public ManageConnectionThread(BluetoothSocket socket) {
        mSocket = socket;
    }
}
