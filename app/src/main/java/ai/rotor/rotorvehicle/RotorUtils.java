package ai.rotor.rotorvehicle;

import java.util.UUID;

public class RotorUtils {
    static final String VEHICLE_NAME_REGEX = "RTR.\\d";
    static final UUID ROTOR_UUID = UUID.fromString("4204ff84-190d-4cce-9e98-526915402758");
    static final int ARDUINO_ADDRESS = 8;

    enum STATE {
        HOMED,
        MANUAL,
        AUTONOMOUS
    }

    enum STATE_CHANGE_REQUEST {
        TO_HOMED,
        TO_MANUAL,
        TO_AUTONOMOUS
    }

    public static STATE setState(STATE initialState, STATE_CHANGE_REQUEST stateChangeRequest) throws IllegalArgumentException {
        switch(initialState) {
            case HOMED:
                switch (stateChangeRequest) {
                    case TO_HOMED:
                        throw new IllegalArgumentException("Already in the homed state");
                    case TO_AUTONOMOUS:
                        return STATE.AUTONOMOUS;
                    case TO_MANUAL:
                        return STATE.MANUAL;
                }
            case MANUAL:
                switch (stateChangeRequest) {
                    case TO_HOMED:
                        return STATE.HOMED;
                    case TO_AUTONOMOUS:
                        throw new IllegalArgumentException("Cannot move directly to autonomous mode from manual");
                    case TO_MANUAL:
                        throw new IllegalArgumentException("Already in manual mode");
                }
            case AUTONOMOUS:
                switch (stateChangeRequest) {
                    case TO_HOMED:
                        return STATE.HOMED;
                    case TO_AUTONOMOUS:
                        throw new IllegalArgumentException("Already in autonomous mode");
                    case TO_MANUAL:
                        throw new IllegalArgumentException("Cannot move directly to manual mode from autonomous");
                }

        }
        return initialState;
    }
}
