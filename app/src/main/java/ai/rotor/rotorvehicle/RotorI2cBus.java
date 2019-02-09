package ai.rotor.rotorvehicle;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.util.List;

import timber.log.Timber;

public class RotorI2cBus {
    private final static String TAG = "Debug, RotorI2cBus";

    private static String mLocalI2cBusDeviceName;
    private PeripheralManager mPeripheralManager;

    public RotorI2cBus() {
        mPeripheralManager = PeripheralManager.getInstance();
        List<String> deviceList = mPeripheralManager.getI2cBusList();
        mLocalI2cBusDeviceName = deviceList.get(0);
    }

    public void write(int deviceAddress, String message) {
        try {
            I2cDevice slaveDevice = mPeripheralManager.openI2cDevice(mLocalI2cBusDeviceName, deviceAddress);
            byte[] bytes = message.getBytes("UTF-8");
            int byteCount = bytes.length;
            slaveDevice.write(bytes, byteCount);
            slaveDevice.close();
        } catch (IOException e) {
            Timber.d("Unable to access I2C device: " + deviceAddress);
        }
    }

}
