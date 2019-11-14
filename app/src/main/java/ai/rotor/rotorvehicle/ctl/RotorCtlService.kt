package ai.rotor.rotorvehicle.ctl

import android.content.Context
import android.hardware.usb.UsbDevice
import android.os.Handler

import ai.rotor.rotorvehicle.R
import timber.log.Timber

class RotorCtlService(private val mContext: Context) : ArduinoListener, Runnable {
    var rotorState: State? = null
        private set
    private var mRotorArduino: Arduino? = null

    enum class State {
        HOMED,
        MANUAL,
        AUTONOMOUS
    }

    init {
        rotorState = State.HOMED
    }

    override fun run() {
        mRotorArduino = Arduino(mContext)
        mRotorArduino!!.setArduinoListener(this)
    }


    @Throws(IllegalArgumentException::class)
    fun setState(stateChangeRequest: State) {


        require(!(rotorState == State.MANUAL && stateChangeRequest == State.AUTONOMOUS)) { "Cannot move directly to autonomous mode from manual" }

        require(!(rotorState == State.AUTONOMOUS && stateChangeRequest == State.MANUAL)) { "Cannot move directly to manual mode from autonomous" }

        if (rotorState != stateChangeRequest) {
            rotorState = stateChangeRequest
        }

        // If going home, send the home command
        if (rotorState == State.HOMED) {
            sendCommand(mContext.getString(R.string.home_command))
        }
    }

    fun sendCommand(cmd: String) {
        Timber.d("Commanding Arduino: %s", cmd)
        val fullCmd = cmd + "\n"
        mRotorArduino!!.send(fullCmd.toByteArray())
    }

    override fun onArduinoAttached(device: UsbDevice) {
        Timber.d("Initializing Arduino communication")
        mRotorArduino!!.open(device)
    }

    override fun onArduinoDetached() {
        Timber.d("Arduino detached")
    }

    override fun onArduinoOpened() {
        Timber.d("Arduino communication initialized")
    }

    override fun onUsbPermissionDenied() {
        Timber.d("Permission denied...")
        Handler().postDelayed({ mRotorArduino!!.reOpen() }, 3000)
    }

    override fun onArduinoMessage(message: String) {
        Timber.d("Arduino response: %s", message)
    }

    fun stop() {
        mRotorArduino!!.unSetArduinoListener()
        mRotorArduino!!.close()
    }
}
