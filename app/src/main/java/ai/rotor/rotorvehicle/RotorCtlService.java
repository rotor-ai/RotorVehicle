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


        if (mRotorState == State.MANUAL && stateChangeRequest == State.AUTONOMOUS) {
            throw new IllegalArgumentException("Cannot move directly to autonomous mode from manual");
        }

        if (mRotorState == State.AUTONOMOUS && stateChangeRequest == State.MANUAL) {
            throw new IllegalArgumentException("Cannot move directly to manual mode from autonomous");
        }

        if (mRotorState != stateChangeRequest){
            mRotorState = stateChangeRequest;
        }
    }

    public void sendCommand(String cmd) {
        mRotorI2cBus.write(RotorUtils.ARDUINO_ADDRESS, cmd);
    }
}
