package ai.rotor.rotorvehicle;

import android.content.Context;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RotorCtlServiceTest {
    private Context testCtx;

    @Test
    public void hasCorrectStates() {
        assert (RotorCtlService.State.valueOf("MANUAL") != null);
        assert (RotorCtlService.State.valueOf("AUTONOMOUS") != null);
        assert (RotorCtlService.State.valueOf("HOMED") != null);
    }

    @Test
    public void transHomedToMan() {
        RotorCtlService rotorCtlService = new RotorCtlService(null);
        RotorCtlService.State initialState = rotorCtlService.getRotorState();
        assertEquals(initialState, RotorCtlService.State.HOMED);

        rotorCtlService.setState(RotorCtlService.State.MANUAL);
        assertEquals(rotorCtlService.getRotorState(), RotorCtlService.State.MANUAL);
    }

    @Test
    public void transHomedToAuto() {
        RotorCtlService rotorCtlService = new RotorCtlService(null);
        assertEquals(rotorCtlService.getRotorState(), RotorCtlService.State.HOMED);

        rotorCtlService.setState(RotorCtlService.State.AUTONOMOUS);
        assertEquals(rotorCtlService.getRotorState(), RotorCtlService.State.AUTONOMOUS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transManToAuto() {
        RotorCtlService rotorCtlService = new RotorCtlService(null);
        rotorCtlService.setState(RotorCtlService.State.MANUAL);
        rotorCtlService.setState(RotorCtlService.State.AUTONOMOUS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transAutoToMan() {
        RotorCtlService rotorCtlService = new RotorCtlService(null);
        rotorCtlService.setState(RotorCtlService.State.AUTONOMOUS);
        rotorCtlService.setState(RotorCtlService.State.MANUAL);
    }

    @Test
    public void verifyImmutable() {
        RotorCtlService rotorCtlService = new RotorCtlService(null);
        RotorCtlService.State initialState = rotorCtlService.getRotorState();
        initialState = RotorCtlService.State.AUTONOMOUS;
        assertEquals(rotorCtlService.getRotorState(), RotorCtlService.State.HOMED);
    }
}