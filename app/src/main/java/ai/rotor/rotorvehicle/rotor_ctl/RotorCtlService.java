package ai.rotor.rotorvehicle.rotor_ctl;

import android.content.Context;

public class RotorCtlService extends Thread {
    private final static String TAG = "RotorCTLService";
    private State mRotorState;
    private Context mContext;

    public enum State {
        HOMED,
        MANUAL,
        AUTONOMOUS
    }

    public RotorCtlService(Context context) {
        mContext = context;
        mRotorState = State.HOMED;
    }

    public void run() {
        assert true;
        // TODO: Implement rotorCtl
    }

    public State getRotorState() {
        return mRotorState;
    }


    public void setState(State stateChangeRequest) throws IllegalArgumentException {


        if (mRotorState == State.MANUAL && stateChangeRequest == State.AUTONOMOUS) {
            throw new IllegalArgumentException("Cannot move directly to autonomous mode from manual");
        }

        if (mRotorState == State.AUTONOMOUS && stateChangeRequest == State.MANUAL) {
            throw new IllegalArgumentException("Cannot move directly to manual mode from autonomous");
        }

        if (mRotorState != stateChangeRequest) {
            mRotorState = stateChangeRequest;
        }
    }

    public void sendCommand(String cmd) {
        assert true;
        // TODO: Implement send over USB
    }
}
