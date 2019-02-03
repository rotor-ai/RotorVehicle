package ai.rotor.rotorvehicle;

public class RotorCtlService extends Thread {
    private final static String TAG = "RotorCTLService";
    private State mRotorState;

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

    public State getRotorState() {
        return mRotorState;
    }


    public void setState(StateChangeRequest stateChangeRequest) throws IllegalArgumentException {
        switch(mRotorState) {
            case HOMED:
                switch (stateChangeRequest) {
                    case TO_HOMED:
                        throw new IllegalArgumentException("Already in the homed state");
                    case TO_AUTONOMOUS:
                        mRotorState = State.AUTONOMOUS;
                        return;
                    case TO_MANUAL:
                        mRotorState = State.MANUAL;
                        return;
                }
            case MANUAL:
                switch (stateChangeRequest) {
                    case TO_HOMED:
                        mRotorState = State.HOMED;
                        return;
                    case TO_AUTONOMOUS:
                        throw new IllegalArgumentException("Cannot move directly to autonomous mode from manual");
                    case TO_MANUAL:
                        throw new IllegalArgumentException("Already in manual mode");
                }
            case AUTONOMOUS:
                switch (stateChangeRequest) {
                    case TO_HOMED:
                        mRotorState = State.HOMED;
                        return;
                    case TO_AUTONOMOUS:
                        throw new IllegalArgumentException("Already in autonomous mode");
                    case TO_MANUAL:
                        throw new IllegalArgumentException("Cannot move directly to manual mode from autonomous");
                }

        }
    }
}
