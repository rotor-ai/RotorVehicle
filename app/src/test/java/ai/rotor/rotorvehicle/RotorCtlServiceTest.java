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
        RotorCtlService.State initialState = RotorCtlService.State.HOMED;
        RotorCtlService.State returnState = RotorCtlService.setState(initialState, RotorCtlService.StateChangeRequest.TO_HOMED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transManToMan() {
        RotorCtlService.State initialState = RotorCtlService.State.MANUAL;
        RotorCtlService.State returnState = RotorCtlService.setState(initialState, RotorCtlService.StateChangeRequest.TO_MANUAL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transAutoToAuto() {
        RotorCtlService.State initialState = RotorCtlService.State.AUTONOMOUS;
        RotorCtlService.State returnState = RotorCtlService.setState(initialState, RotorCtlService.StateChangeRequest.TO_AUTONOMOUS);
    }

    @Test
    public void transHomedToMan() {
        RotorCtlService.State initialState = RotorCtlService.State.HOMED;
        RotorCtlService.State returnState = RotorCtlService.setState(initialState, RotorCtlService.StateChangeRequest.TO_MANUAL);
        assertEquals(returnState, RotorCtlService.State.MANUAL);
    }

    @Test
    public void transHomedToAuto() {
        RotorCtlService.State initialState = RotorCtlService.State.HOMED;
        RotorCtlService.State returnState = RotorCtlService.setState(initialState, RotorCtlService.StateChangeRequest.TO_AUTONOMOUS);
        assertEquals(returnState, RotorCtlService.State.AUTONOMOUS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transManToAuto() {
        RotorCtlService.State initialState = RotorCtlService.State.MANUAL;
        RotorCtlService.State returnState = RotorCtlService.setState(initialState, RotorCtlService.StateChangeRequest.TO_AUTONOMOUS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transAutoToMan() {
        RotorCtlService.State initialState = RotorCtlService.State.AUTONOMOUS;
        RotorCtlService.State returnState = RotorCtlService.setState(initialState, RotorCtlService.StateChangeRequest.TO_MANUAL);
    }

}