package ai.rotor.rotorvehicle.ctl

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager

import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface

import java.util.HashMap

import ai.rotor.rotorvehicle.RotorUtils
import timber.log.Timber

class Arduino internal constructor(private val mContext: Context) : UsbSerialInterface.UsbReadCallback {
    private val mUsbReceiver: UsbReceiver
    private val mUsbManager: UsbManager
    private var mIsOpened: Boolean = false
    private var mListener: ArduinoListener? = null
    private var mLastArduinoAttached: UsbDevice? = null
    private var mSerialPort: UsbSerialDevice? = null
    private var mSerialConnection: UsbDeviceConnection? = null
    private val mBufferedChars = StringBuilder()

    private val attachedArduino: UsbDevice?
        get() {
            val map = mUsbManager.deviceList
            for (device in map.values) {
                if (device.vendorId == RotorUtils.ARDUINO_VENDOR_ID) {
                    return device
                }
            }
            return null
        }


    init {
        this.mUsbReceiver = UsbReceiver()
        this.mUsbManager = mContext.getSystemService(Context.USB_SERVICE) as UsbManager
        this.mIsOpened = false
    }

    fun setArduinoListener(listener: ArduinoListener?) {
        this.mListener = listener

        val intentFilter = IntentFilter()
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        intentFilter.addAction(ACTION_USB_DEVICE_PERMISSION)

        mContext.registerReceiver(mUsbReceiver, intentFilter)

        this.mLastArduinoAttached = attachedArduino
        if (mLastArduinoAttached != null && listener != null) {
            listener.onArduinoAttached(mLastArduinoAttached!!)
        }
    }

    fun unSetArduinoListener() {
        this.mListener = null
    }

    fun open(device: UsbDevice?) {
        val permissionIntent = PendingIntent.getBroadcast(mContext, 0, Intent(ACTION_USB_DEVICE_PERMISSION), 0)
        mUsbManager.requestPermission(device, permissionIntent)
    }

    fun reOpen() {
        open(mLastArduinoAttached)
    }

    fun close() {
        if (mSerialPort != null) {
            mSerialPort!!.close()
        }
        if (mSerialConnection != null) {
            mSerialConnection!!.close()
        }

        mIsOpened = false
        mContext.unregisterReceiver(mUsbReceiver)
    }

    fun send(bytes: ByteArray) {
        if (mSerialPort != null) {
            mSerialPort!!.write(bytes)
        }
    }

    private inner class UsbReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val device: UsbDevice
            if (intent.action != null) {
                when (intent.action) {
                    UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                        device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        if (device.vendorId == RotorUtils.ARDUINO_VENDOR_ID) {
                            mLastArduinoAttached = device
                            if (mListener != null) {
                                mListener!!.onArduinoAttached(device)
                            }
                        }
                    }
                    UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                        device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        if (device.vendorId == RotorUtils.ARDUINO_VENDOR_ID) {
                            if (mListener != null) {
                                mListener!!.onArduinoDetached()
                            }
                        }
                    }
                    ACTION_USB_DEVICE_PERMISSION -> {
                        val permissionGranted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        if (permissionGranted) {
                            device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                            if (device.vendorId == RotorUtils.ARDUINO_VENDOR_ID) {
                                mSerialConnection = mUsbManager.openDevice(device)
                                mSerialPort = UsbSerialDevice.createUsbSerialDevice(device, mSerialConnection)
                                if (mSerialPort != null) {
                                    if (mSerialPort!!.open()) {
                                        mSerialPort!!.setBaudRate(RotorUtils.BAUD_RATE)
                                        mSerialPort!!.setDataBits(UsbSerialInterface.DATA_BITS_8)
                                        mSerialPort!!.setStopBits(UsbSerialInterface.STOP_BITS_1)
                                        mSerialPort!!.setParity(UsbSerialInterface.PARITY_NONE)
                                        mSerialPort!!.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                                        mSerialPort!!.read(this@Arduino)

                                        mIsOpened = true

                                        if (mListener != null) {
                                            mListener!!.onArduinoOpened()
                                        }
                                    }
                                } else {
                                    Timber.d("Serial port is null")
                                }
                            }
                        } else if (mListener != null) {
                            mListener!!.onUsbPermissionDenied()
                        }
                    }
                }
            }
        }
    }

    override fun onReceivedData(bytes: ByteArray) {
        val message = String(bytes)
        if (mListener != null && message.length > 0) {
            for (i in 0 until message.length) {
                val c = message[i]
                if (c.toString().matches(".".toRegex())) {
                    mBufferedChars.append(c)

                } else {
                    mListener!!.onArduinoMessage(mBufferedChars.toString())
                    mBufferedChars.setLength(0)
                }
            }
        }
    }

    companion object {
        private val ACTION_USB_DEVICE_PERMISSION = "com.example.androidusb.USB_PERMISSION"
    }
}
