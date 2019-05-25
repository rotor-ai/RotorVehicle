package ai.rotor.rotorvehicle.rotor_ctl;

import android.hardware.usb.UsbDevice;

public interface ArduinoListener {
    void onArduinoAttached(UsbDevice mLastArduinoAttached);

    void onArduinoDetached();

    void onArduinoOpened();

    void onUsbPermissionDenied();

    void onArduinoMessage(String message);
}
