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
        correctUUID = UUID.fromString("10101010-1234-5678-90ab-101010101010");
        correctAddress = 8;

    }

    @Test
    public void isUUIDCorrect() {
        UUID TestUUID = RotorUtils.INSTANCE.getROTOR_TX_RX_SERVICE_UUID();
        assertEquals(TestUUID, correctUUID);
    }
}