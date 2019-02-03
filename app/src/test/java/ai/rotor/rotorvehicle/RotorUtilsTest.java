package ai.rotor.rotorvehicle;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import androidx.core.widget.TextViewCompat;

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
        assert (RotorUtils.STATE.valueOf("MANUAL") != null);
        assert (RotorUtils.STATE.valueOf("AUTONOMOUS") != null);
        assert (RotorUtils.STATE.valueOf("HOMED") != null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transHomedToHomed() {
        RotorUtils.STATE initialState = RotorUtils.STATE.HOMED;
        RotorUtils.STATE returnState = RotorUtils.setState(initialState, RotorUtils.STATE_CHANGE_REQUEST.TO_HOMED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transManToMan() {
        RotorUtils.STATE initialState = RotorUtils.STATE.MANUAL;
        RotorUtils.STATE returnState = RotorUtils.setState(initialState, RotorUtils.STATE_CHANGE_REQUEST.TO_MANUAL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transAutoToAuto() {
        RotorUtils.STATE initialState = RotorUtils.STATE.AUTONOMOUS;
        RotorUtils.STATE returnState = RotorUtils.setState(initialState, RotorUtils.STATE_CHANGE_REQUEST.TO_AUTONOMOUS);
    }

    @Test
    public void transHomedToMan() {
        RotorUtils.STATE initialState = RotorUtils.STATE.HOMED;
        RotorUtils.STATE returnState = RotorUtils.setState(initialState, RotorUtils.STATE_CHANGE_REQUEST.TO_MANUAL);
        assertEquals(returnState, RotorUtils.STATE.MANUAL);
    }

    @Test
    public void transHomedToAuto() {
        RotorUtils.STATE initialState = RotorUtils.STATE.HOMED;
        RotorUtils.STATE returnState = RotorUtils.setState(initialState, RotorUtils.STATE_CHANGE_REQUEST.TO_AUTONOMOUS);
        assertEquals(returnState, RotorUtils.STATE.AUTONOMOUS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transManToAuto() {
        RotorUtils.STATE initialState = RotorUtils.STATE.MANUAL;
        RotorUtils.STATE returnState = RotorUtils.setState(initialState, RotorUtils.STATE_CHANGE_REQUEST.TO_AUTONOMOUS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transAutoToMan() {
        RotorUtils.STATE initialState = RotorUtils.STATE.AUTONOMOUS;
        RotorUtils.STATE returnState = RotorUtils.setState(initialState, RotorUtils.STATE_CHANGE_REQUEST.TO_MANUAL);
    }
}