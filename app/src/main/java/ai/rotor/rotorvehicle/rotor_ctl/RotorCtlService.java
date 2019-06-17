package ai.rotor.rotorvehicle.rotor_ctl;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Handler;

import ai.rotor.rotorvehicle.R;
import timber.log.Timber;

public class RotorCtlService implements ArduinoListener, Runnable {
    private State mRotorState;
    private Context mContext;
    private Arduino mRotorArduino;

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
        mRotorArduino = new Arduino(mContext);
        mRotorArduino.setArduinoListener(this);
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

        // If going home, send the home command
        if (mRotorState == State.HOMED) {
            sendCommand(mContext.getString(R.string.home_command));
        }
    }

    public void sendCommand(String cmd) {
        Timber.d("Commanding Arduino: %s", cmd);
        String fullCmd = cmd + "\n";
        mRotorArduino.send(fullCmd.getBytes());
    }

    @Override
    public void onArduinoAttached(UsbDevice device) {
        Timber.d("Initializing Arduino communication");
        mRotorArduino.open(device);
    }

    @Override
    public void onArduinoDetached() {
        Timber.d("Arduino detached");
    }

    @Override
    public void onArduinoOpened() {
        Timber.d("Arduino communication initialized");
    }

    @Override
    public void onUsbPermissionDenied() {
        Timber.d("Permission denied...");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mRotorArduino.reOpen();
            }
        }, 3000);
    }

    @Override
    public void onArduinoMessage(String message) {
        Timber.d("Arduino response: %s", message);
    }

    public void stop() {
        mRotorArduino.unSetArduinoListener();
        mRotorArduino.close();
    }
}
