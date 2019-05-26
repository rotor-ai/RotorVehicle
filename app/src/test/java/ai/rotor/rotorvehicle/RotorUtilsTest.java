package ai.rotor.rotorvehicle;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

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
        UUID TestUUID = RotorUtils.ROTOR_TX_RX_SERVICE_UUID;
        assertEquals(TestUUID, correctUUID);
    }
}