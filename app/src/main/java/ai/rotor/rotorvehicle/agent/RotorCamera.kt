package ai.rotor.rotorvehicle.agent

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.media.ImageReader
import android.os.Handler

import java.util.Collections

import ai.rotor.rotorvehicle.RotorUtils
import android.view.Surface
import timber.log.Timber

class RotorCamera {
    private var mCameraDevice: CameraDevice? = null
    private var mCaptureSession: CameraCaptureSession? = null
    private var mImageReader: ImageReader? = null

    private val mStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Timber.d("Opened camera")
            mCameraDevice = camera
        }

        override fun onDisconnected(camera: CameraDevice) {
            Timber.d("Closed camera")
            mCameraDevice = camera
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Timber.d("Error using camera device, closing. Error code: %s", error)
            camera.close()
        }

        override fun onClosed(camera: CameraDevice) {
            super.onClosed(camera)
            Timber.d("Closed camera, releasing")
            mCameraDevice = null
        }
    }

    private val mSessionCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            // The camera is already closed
            if (mCameraDevice == null) {
                return
            }

            // When the session is ready, we start capture
            mCaptureSession = session
            triggerImageCapturing()
        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            Timber.d("Failed to configure camera")
        }
    }

    private val mCaptureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureProgressed(session: CameraCaptureSession, request: CaptureRequest, partialResult: CaptureResult) {
            super.onCaptureProgressed(session, request, partialResult)
            Timber.d("Partial result")
        }

        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
            super.onCaptureCompleted(session, request, result)

        }
    }

    val isOpen: Boolean
        get() = if (mCameraDevice != null) {
            true
        } else {
            false
        }

    private object InstanceHolder {
        val mCamera = RotorCamera()
    }

    internal fun initializeCamera(context: Context,
                                  backgroundHandler: Handler,
                                  imageAvailableListener: ImageReader.OnImageAvailableListener) {

        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        var camIds = arrayOf<String>()
        try {
            camIds = manager.cameraIdList
        } catch (e: CameraAccessException) {
            Timber.d("Cam access exception getting IDs")
        }

        if (camIds.size < 1) {
            Timber.d("No cameras found")
            return
        }

        val id = camIds[0]
        Timber.d("Using camera id: %s", id)

        // Initialize the image processor
        mImageReader = ImageReader.newInstance(RotorUtils.IMAGE_WIDTH, RotorUtils.IMAGE_HEIGHT, ImageFormat.JPEG, RotorUtils.MAX_IMAGES_USED)
        mImageReader!!.setOnImageAvailableListener(imageAvailableListener, backgroundHandler)

        // Open the camera resource
        try {
            Timber.d("Attempting to open the camera")
            if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Timber.d("Unable to use camera. Permission not granted.")
            } else {
                manager.openCamera(id, mStateCallback, backgroundHandler)
            }
        } catch (e: CameraAccessException) {
            Timber.d("Camera access exception due to: %s", e.toString())
        }

    }

    internal fun startCapturing() {
        Timber.d("Starting camera capture session")

        if (mCameraDevice == null) {
            Timber.d("Cannot take picture, camera not initialized")
            return
        }

        try {
            mCameraDevice!!.createCaptureSession(
                    listOf<Surface>(mImageReader!!.surface),
                    mSessionCallback, null)
        } catch (e: CameraAccessException) {
            Timber.d("access exception while preparing pic due to: %s", e.toString())
        }

    }

    private fun triggerImageCapturing() {
        try {
            val captureBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(mImageReader!!.surface)
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
            Timber.d("Capture session initialized")
            mCaptureSession!!.setRepeatingRequest(captureBuilder.build(), mCaptureCallback, null)
        } catch (e: CameraAccessException) {
            Timber.d("Camera access exception: %s", e.toString())
        }

    }

    internal fun stopCapturing() {
        Timber.d("Stopping camera capture session")

        if (mCaptureSession != null) {
            mCaptureSession!!.close()
            mCaptureSession = null

            Timber.d("Capture session closed")
        }
    }

    internal fun shutDown() {
        if (mCameraDevice != null) {
            mCameraDevice!!.close()
        }
    }

    companion object {


        internal val instance: RotorCamera
            get() = InstanceHolder.mCamera
    }
}
