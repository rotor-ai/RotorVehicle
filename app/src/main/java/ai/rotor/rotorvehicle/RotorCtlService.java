package ai.rotor.rotorvehicle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import timber.log.Timber;

public class RotorCtlService extends Thread {
    private final static String TAG = "RotorCTLService";
    private State mRotorState;
    private RotorI2cBus mRotorI2cBus;
    private Context mContext;

    enum State {
        HOMED,
        MANUAL,
        AUTONOMOUS
    }

    public RotorCtlService(Context context) {
        mContext = context;
        mRotorState = State.HOMED;

    }

    public void run() {
        mRotorI2cBus = new RotorI2cBus();
    }

    public State getRotorState() {
        return mRotorState;
    }


    public void setState(State stateChangeRequest) throws IllegalArgumentException {
        switch(mRotorState) {
            case HOMED:
                switch (stateChangeRequest) {
                    case HOMED:
                        return;
                    case AUTONOMOUS:
                        mRotorState = State.AUTONOMOUS;
                        return;
                    case MANUAL:
                        mRotorState = State.MANUAL;
                        return;
                }
            case MANUAL:
                switch (stateChangeRequest) {
                    case HOMED:
                        mRotorState = State.HOMED;
                        return;
                    case AUTONOMOUS:
                        throw new IllegalArgumentException("Cannot move directly to autonomous mode from manual");
                    case MANUAL:
                        return;
                }
            case AUTONOMOUS:
                switch (stateChangeRequest) {
                    case HOMED:
                        mRotorState = State.HOMED;
                        return;
                    case AUTONOMOUS:
                        return;
                    case MANUAL:
                        throw new IllegalArgumentException("Cannot move directly to manual mode from autonomous");
                }

        }
    }

    public void sendCommand(String cmd) {
        mRotorI2cBus.write(RotorUtils.ARDUINO_ADDRESS, cmd);
    }
}
