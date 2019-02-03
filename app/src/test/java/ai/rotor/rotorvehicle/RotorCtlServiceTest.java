package ai.rotor.rotorvehicle;

import org.junit.Test;

import static org.junit.Assert.*;

public class RotorCtlServiceTest {

    @Test
    public void hasCorrectStates() {
        assert (RotorCtlService.State.valueOf("MANUAL") != null);
        assert (RotorCtlService.State.valueOf("AUTONOMOUS") != null);
        assert (RotorCtlService.State.valueOf("HOMED") != null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transHomedToHomed() {
        RotorCtlService rotorCtlService = new RotorCtlService();
        rotorCtlService.setState(RotorCtlService.StateChangeRequest.TO_HOMED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transManToMan() {
        RotorCtlService rotorCtlService = new RotorCtlService();
        rotorCtlService.setState(RotorCtlService.StateChangeRequest.TO_MANUAL);
        rotorCtlService.setState(RotorCtlService.StateChangeRequest.TO_MANUAL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transAutoToAuto() {
        RotorCtlService rotorCtlService = new RotorCtlService();
        rotorCtlService.setState(RotorCtlService.StateChangeRequest.TO_AUTONOMOUS);
        rotorCtlService.setState(RotorCtlService.StateChangeRequest.TO_AUTONOMOUS);
    }

    @Test
    public void transHomedToMan() {
        RotorCtlService rotorCtlService = new RotorCtlService();
        RotorCtlService.State initialState = rotorCtlService.getRotorState();
        assertEquals(initialState, RotorCtlService.State.HOMED);

        rotorCtlService.setState(RotorCtlService.StateChangeRequest.TO_MANUAL);
        assertEquals(rotorCtlService.getRotorState(), RotorCtlService.State.MANUAL);
    }

    @Test
    public void transHomedToAuto() {
        RotorCtlService rotorCtlService = new RotorCtlService();
        assertEquals(rotorCtlService.getRotorState(), RotorCtlService.State.HOMED);

        rotorCtlService.setState(RotorCtlService.StateChangeRequest.TO_AUTONOMOUS);
        assertEquals(rotorCtlService.getRotorState(), RotorCtlService.State.AUTONOMOUS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transManToAuto() {
        RotorCtlService rotorCtlService = new RotorCtlService();
        rotorCtlService.setState(RotorCtlService.StateChangeRequest.TO_MANUAL);
        rotorCtlService.setState(RotorCtlService.StateChangeRequest.TO_AUTONOMOUS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transAutoToMan() {
        RotorCtlService rotorCtlService = new RotorCtlService();
        rotorCtlService.setState(RotorCtlService.StateChangeRequest.TO_AUTONOMOUS);
        rotorCtlService.setState(RotorCtlService.StateChangeRequest.TO_MANUAL);
    }

    @Test
    public void verifyImmutable() {
        RotorCtlService rotorCtlService = new RotorCtlService();
        RotorCtlService.State initialState = rotorCtlService.getRotorState();
        initialState = RotorCtlService.State.AUTONOMOUS;
        assertEquals(rotorCtlService.getRotorState(), RotorCtlService.State.HOMED);
    }
}