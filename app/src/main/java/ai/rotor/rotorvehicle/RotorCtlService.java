package ai.rotor.rotorvehicle;

public class RotorCtlService extends Thread {
    private final static String TAG = "RotorCTLService";
    State mRotorState;

    enum State {
        HOMED,
        MANUAL,
        AUTONOMOUS
    }

    enum StateChangeRequest {
        TO_HOMED,
        TO_MANUAL,
        TO_AUTONOMOUS
    }

    public RotorCtlService() {
        mRotorState = State.HOMED;
    }

    public static State setState(State initialState, StateChangeRequest stateChangeRequest) throws IllegalArgumentException {
        switch(initialState) {
            case HOMED:
                switch (stateChangeRequest) {
                    case TO_HOMED:
                        throw new IllegalArgumentException("Already in the homed state");
                    case TO_AUTONOMOUS:
                        return State.AUTONOMOUS;
                    case TO_MANUAL:
                        return State.MANUAL;
                }
            case MANUAL:
                switch (stateChangeRequest) {
                    case TO_HOMED:
                        return State.HOMED;
                    case TO_AUTONOMOUS:
                        throw new IllegalArgumentException("Cannot move directly to autonomous mode from manual");
                    case TO_MANUAL:
                        throw new IllegalArgumentException("Already in manual mode");
                }
            case AUTONOMOUS:
                switch (stateChangeRequest) {
                    case TO_HOMED:
                        return State.HOMED;
                    case TO_AUTONOMOUS:
                        throw new IllegalArgumentException("Already in autonomous mode");
                    case TO_MANUAL:
                        throw new IllegalArgumentException("Cannot move directly to manual mode from autonomous");
                }

        }
        return initialState;
    }
}
