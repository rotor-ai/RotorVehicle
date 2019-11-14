package ai.rotor.rotorvehicle.ctl

import android.hardware.usb.UsbDevice

interface ArduinoListener {
    fun onArduinoAttached(mLastArduinoAttached: UsbDevice)

    fun onArduinoDetached()

    fun onArduinoOpened()

    fun onUsbPermissionDenied()

    fun onArduinoMessage(message: String)
}
