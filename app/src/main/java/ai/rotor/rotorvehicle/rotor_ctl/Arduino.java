package ai.rotor.rotorvehicle.rotor_ctl;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.HashMap;

import static ai.rotor.rotorvehicle.RotorUtils.*;
import timber.log.Timber;

public class Arduino implements UsbSerialInterface.UsbReadCallback {
    private Context mContext;
    private UsbReceiver mUsbReceiver;
    private UsbManager mUsbManager;
    private boolean mIsOpened;
    private ArduinoListener mListener;
    private UsbDevice mLastArduinoAttached;
    private UsbSerialDevice mSerialPort;
    private UsbDeviceConnection mSerialConnection;
    private StringBuilder mBufferedChars = new StringBuilder();
    final private static String ACTION_USB_DEVICE_PERMISSION = "com.example.androidusb.USB_PERMISSION";


    public Arduino(Context context) {
        this.mContext = context;
        this.mUsbReceiver = new UsbReceiver();
        this.mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        this.mIsOpened = false;
    }

    public void setArduinoListener(ArduinoListener listener) {
        this.mListener = listener;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        intentFilter.addAction(ACTION_USB_DEVICE_PERMISSION);

        mContext.registerReceiver(mUsbReceiver, intentFilter);

        this.mLastArduinoAttached = getAttachedArduino();
        if (mLastArduinoAttached != null && listener != null) {
            listener.onArduinoAttached(mLastArduinoAttached);
        }
    }

    public void unSetArduinoListener() {
        this.mListener = null;
    }

    public void open(UsbDevice device) {
        PendingIntent permissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_DEVICE_PERMISSION), 0);
        mUsbManager.requestPermission(device, permissionIntent);
    }

    public void reOpen() {
        open(mLastArduinoAttached);
    }

    public void close() {
        if (mSerialPort != null) {
            mSerialPort.close();
        }
        if (mSerialConnection != null) {
            mSerialConnection.close();
        }

        mIsOpened = false;
        mContext.unregisterReceiver(mUsbReceiver);
    }

    public void send(byte[] bytes) {
        if (mSerialPort != null) {
            mSerialPort.write(bytes);
        }
    }

    private class UsbReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            UsbDevice device;
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                        device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (device.getVendorId() == ARDUINO_VENDOR_ID) {
                            mLastArduinoAttached = device;
                            if (mListener != null) {
                                mListener.onArduinoAttached(device);
                            }
                        }
                        break;
                    case UsbManager.ACTION_USB_DEVICE_DETACHED:
                        device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (device.getVendorId() == ARDUINO_VENDOR_ID) {
                            if (mListener != null) {
                                mListener.onArduinoDetached();
                            }
                        }
                        break;
                    case ACTION_USB_DEVICE_PERMISSION:
                        boolean permissionGranted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                        if (permissionGranted) {
                            device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                            if (device.getVendorId() == ARDUINO_VENDOR_ID) {
                                mSerialConnection = mUsbManager.openDevice(device);
                                mSerialPort = UsbSerialDevice.createUsbSerialDevice(device, mSerialConnection);
                                if (mSerialPort != null) {
                                    if (mSerialPort.open()) {
                                        mSerialPort.setBaudRate(BAUD_RATE);
                                        mSerialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                                        mSerialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                                        mSerialPort.setParity(UsbSerialInterface.PARITY_NONE);
                                        mSerialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                                        mSerialPort.read(Arduino.this);

                                        mIsOpened = true;

                                        if (mListener != null) {
                                            mListener.onArduinoOpened();
                                        }
                                    }
                                } else {
                                    Timber.d("Serial port is null");
                                }
                            }
                        } else if (mListener != null) {
                            mListener.onUsbPermissionDenied();
                        }
                        break;
                }
            }
        }
    }

    private UsbDevice getAttachedArduino() {
        HashMap<String, UsbDevice> map = mUsbManager.getDeviceList();
        for (UsbDevice device : map.values()) {
            if (device.getVendorId() == ARDUINO_VENDOR_ID) {
                return device;
            }
        }
        return null;
    }

    public boolean isOpened() {
        return mIsOpened;
    }

    @Override
    public void onReceivedData(byte[] bytes) {
        String message = new String(bytes);
        if (mListener != null && message.length() > 0) {
            for (int i = 0; i < message.length(); i++) {
                char c = message.charAt(i);
                if (String.valueOf(c).matches(".")) {
                    mBufferedChars.append(c);

                } else {
                    mListener.onArduinoMessage(mBufferedChars.toString());
                    mBufferedChars.setLength(0);
                }
            }
        }
    }
}
