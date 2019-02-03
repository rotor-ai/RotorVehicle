package ai.rotor.rotorvehicle;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class RotorUtilsTest {
    private UUID correctUUID;
    private int correctAddress;

    @Before
    public void setup() {
        correctUUID = UUID.fromString("4204ff84-190d-4cce-9e98-526915402758");
        correctAddress = 8;

    }

    @Test
    public void isUUIDCorrect() {
        UUID TestUUID = RotorUtils.ROTOR_UUID;
        assertEquals(TestUUID, correctUUID);
    }

    @Test
    public void isArduinoAddressCorrect() {
        int testAddress = RotorUtils.ARDUINO_ADDRESS;
        assertEquals(testAddress, correctAddress);
    }

    @Test
    public void hasCorrectStates() {
        assert (RotorUtils.State.valueOf("MANUAL") != null);
        assert (RotorUtils.State.valueOf("AUTONOMOUS") != null);
        assert (RotorUtils.State.valueOf("HOMED") != null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transHomedToHomed() {
        RotorUtils.State initialState = RotorUtils.State.HOMED;
        RotorUtils.State returnState = RotorUtils.setState(initialState, RotorUtils.StateChangeRequest.TO_HOMED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transManToMan() {
        RotorUtils.State initialState = RotorUtils.State.MANUAL;
        RotorUtils.State returnState = RotorUtils.setState(initialState, RotorUtils.StateChangeRequest.TO_MANUAL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transAutoToAuto() {
        RotorUtils.State initialState = RotorUtils.State.AUTONOMOUS;
        RotorUtils.State returnState = RotorUtils.setState(initialState, RotorUtils.StateChangeRequest.TO_AUTONOMOUS);
    }

    @Test
    public void transHomedToMan() {
        RotorUtils.State initialState = RotorUtils.State.HOMED;
        RotorUtils.State returnState = RotorUtils.setState(initialState, RotorUtils.StateChangeRequest.TO_MANUAL);
        assertEquals(returnState, RotorUtils.State.MANUAL);
    }

    @Test
    public void transHomedToAuto() {
        RotorUtils.State initialState = RotorUtils.State.HOMED;
        RotorUtils.State returnState = RotorUtils.setState(initialState, RotorUtils.StateChangeRequest.TO_AUTONOMOUS);
        assertEquals(returnState, RotorUtils.State.AUTONOMOUS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transManToAuto() {
        RotorUtils.State initialState = RotorUtils.State.MANUAL;
        RotorUtils.State returnState = RotorUtils.setState(initialState, RotorUtils.StateChangeRequest.TO_AUTONOMOUS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transAutoToMan() {
        RotorUtils.State initialState = RotorUtils.State.AUTONOMOUS;
        RotorUtils.State returnState = RotorUtils.setState(initialState, RotorUtils.StateChangeRequest.TO_MANUAL);
    }
}