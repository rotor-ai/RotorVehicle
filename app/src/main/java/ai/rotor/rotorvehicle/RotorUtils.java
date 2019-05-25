package ai.rotor.rotorvehicle;

import java.util.UUID;

public class RotorUtils {
    public static final String VEHICLE_NAME_REGEX = "RTR.\\d";
    public static final UUID ROTOR_TX_RX_SERVICE_UUID = UUID.fromString("10101010-1234-5678-90ab-101010101010");
    public static final UUID ROTOR_TX_RX_CHARACTERISTIC_UUID = UUID.fromString("10101010-1234-5678-90ab-202020202020");
    public static final int ARDUINO_ADDRESS = 8;
    public static final String ACTION_STREAMS_ACQUIRED = "streamsAcquired";
    public static final String ACTION_DISCONNECTED = "disconnected";
    public static final String ACTION_MESSAGE_RECEIVED = "messageReceived";
    public static final String EXTRA_CMD = "cmd";
    public static final String HOMED_CMD = "N000 N000";
}
