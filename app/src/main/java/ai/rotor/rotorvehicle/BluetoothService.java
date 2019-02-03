package ai.rotor.rotorvehicle;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import androidx.localbroadcastmanager.content.LocalBroadcastManager; //https://github.com/flutter/flutter-intellij/issues/966



import static ai.rotor.rotorvehicle.RotorUtils.ROTOR_UUID;

@SuppressLint("LogNotTimber")
public class BluetoothService {
    private static String TAG =  "Debug, BluetoothService";
    private AcceptThread mAcceptThread;
    private ManageConnectedThread mManageConnectedThread;
    private BluetoothSocket mSocket;
    private Context mContext;
    private RotorUtils.State mRotorState;

    public void startClient(Context context, RotorUtils.State state) {
        mContext = context;
        Log.d(TAG, "Starting accept thread...");
        mAcceptThread = new AcceptThread();
        mAcceptThread.start();
        mRotorState = state;
    }

    /**
     * Accept thread sets up a BT server and accepts all incoming connections with the correct UUID
     */
    public class AcceptThread extends Thread {
        private static final String TAG = "AcceptThread Debug";
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
            mSocket = null;

            while(true) {
                try {
                    mSocket = mServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (mSocket != null) {
                    Log.d(TAG, "Connection created...");

                    manageConnectedThread();

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

    private void manageConnectedThread() {
        Log.d(TAG, "Starting ManageConnectedThread...");
        mManageConnectedThread = new ManageConnectedThread();
        mManageConnectedThread.start();
    }


    /**
     * Connected thread handles the connected socket passed by ConnectThread
     */
    private class ManageConnectedThread extends Thread {
        private static final String TAG = "Debug, ManageConThread";
        private final InputStream mInStream;
        private final OutputStream mOutStream;
        private byte[] mBuffer;

        public ManageConnectedThread() {
            Log.d(TAG, "acquiring input and output streams...");
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }

            try {
                tmpOut = mSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mInStream = tmpIn;
            mOutStream = tmpOut;
            Log.d(TAG, "Input and output streams acquired!");

            Intent streamsAcquiredIntent = new Intent("streamsAcquired");
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(streamsAcquiredIntent);
        }

        public void run() {
            mBuffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mInStream.read(mBuffer);
                    String incomingMessage = new String(mBuffer, 0, bytes);
                    Log.d(TAG, "Input stream: " + incomingMessage);
                    Intent messageReceivedIntent = new Intent("messageReceived");
                    messageReceivedIntent.putExtra("cmd", incomingMessage);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(messageReceivedIntent);
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    Intent disconnectedIntent = new Intent("disconnected");
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(disconnectedIntent);
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "Output stream: " + text);
            try {
                mOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}
