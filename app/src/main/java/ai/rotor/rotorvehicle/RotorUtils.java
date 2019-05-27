package ai.rotor.rotorvehicle;

import java.util.UUID;

public class RotorUtils {
    public static final String VEHICLE_NAME_REGEX = "RTR.\\d";
    public static final UUID ROTOR_TX_RX_SERVICE_UUID = UUID.fromString("10101010-1234-5678-90ab-101010101010");
    public static final UUID ROTOR_TX_RX_CHARACTERISTIC_UUID = UUID.fromString("10101010-1234-5678-90ab-202020202020");
    public static final String HOMED_CMD = "N000 N000";

    // Camera constants
    public static final int IMAGE_HEIGHT = 300;
    public static final int IMAGE_WIDTH = 300;
    public static final int MAX_IMAGES_USED = 5;

    // Arduino constants
    public static final int BAUD_RATE = 9600;
    public static final int ARDUINO_VENDOR_ID = 9025;
}
