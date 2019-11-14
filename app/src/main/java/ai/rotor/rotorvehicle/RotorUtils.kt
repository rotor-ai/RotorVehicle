package ai.rotor.rotorvehicle

import java.util.UUID

object RotorUtils {
    val VEHICLE_NAME_REGEX = "RTR.\\d"
    val ROTOR_TX_RX_SERVICE_UUID = UUID.fromString("10101010-1234-5678-90ab-101010101010")
    val ROTOR_TX_RX_CHARACTERISTIC_UUID = UUID.fromString("10101010-1234-5678-90ab-202020202020")
    val HOMED_CMD = "N000 N000"

    // Camera constants
    val IMAGE_HEIGHT = 256
    val IMAGE_WIDTH = 256
    val MAX_IMAGES_USED = 5

    // Arduino constants
    val BAUD_RATE = 9600
    val ARDUINO_VENDOR_ID = 9025
}
