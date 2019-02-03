package ai.rotor.rotorvehicle;

import java.util.UUID;

public class RotorUtils {
    static final String VEHICLE_NAME_REGEX = "RTR.\\d";
    static final UUID ROTOR_UUID = UUID.fromString("4204ff84-190d-4cce-9e98-526915402758");
    static final int ARDUINO_ADDRESS = 8;

    enum STATE {
        MANUAL,
        AUTONOMOUS
    }
}
