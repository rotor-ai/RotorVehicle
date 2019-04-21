package ai.rotor.rotorvehicle.rotor_ctl;

import android.content.Context;

import ai.rotor.rotorvehicle.RotorUtils;
import timber.log.Timber;

public class RotorCtlService implements Runnable {
    private State mRotorState;
    private RotorI2cBus mRotorI2cBus;

    public enum State {
        HOMED,
        MANUAL,
        AUTONOMOUS
    }

    public RotorCtlService() {
        Timber.d("Creating new RotorCtlService");
        mRotorState = State.HOMED;
    }

    public void run() {
        mRotorI2cBus = new RotorI2cBus();
    }

    public State getRotorState() {
        return mRotorState;
    }


    public void setState(State stateChangeRequest) throws IllegalArgumentException {

        // If setting to home, send home command
        if (stateChangeRequest == State.HOMED) {
            Timber.d("Homing vehicle");
            this.sendCommand(RotorUtils.HOMED_CMD);
        }

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

        if (mRotorState == State.HOMED) {
            throw new IllegalStateException("Cannot send a command while Rotor is homed.");
        }

        Timber.d("Writing command to Arduino: %s", cmd);
        mRotorI2cBus.write(RotorUtils.ARDUINO_ADDRESS, cmd);
    }
}
