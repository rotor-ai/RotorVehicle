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
    private final BroadcastReceiver mReceiver = new RotorCtlService.RotorBroadcastReceiver();

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

    public RotorCtlService(Context context) {
        mContext = context;
        mRotorState = State.HOMED;

    }

    public void run() {
        // Adding all filter actions in run() for testability
        IntentFilter filter = new IntentFilter();
        filter.addAction(RotorUtils.ACTION_MESSAGE_RECEIVED);
        mRotorI2cBus = new RotorI2cBus();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, filter);
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

    class RotorBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Timber.d(action);

            if (RotorUtils.ACTION_MESSAGE_RECEIVED.equals(action)) {
                String cmd = intent.getStringExtra("cmd");

                mRotorI2cBus.write(RotorUtils.ARDUINO_ADDRESS, cmd);
            }
        }
    }
}
