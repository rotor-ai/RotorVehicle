package ai.rotor.rotorvehicle.agent

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.widget.ImageView

import java.nio.ByteBuffer

import ai.rotor.rotorvehicle.RotorUtils
import ai.rotor.rotorvehicle.ctl.RotorCtlService
import timber.log.Timber


class RotorAiService(private val mMainContext: Context, private val mImageView: ImageView, private val mRotorCtlService: RotorCtlService) : Runnable {
    private val mCamera: RotorCamera
    private val mCameraHandler: Handler
    private val mUiHandler: Handler
    private val mCameraThread: HandlerThread
    var isAutoMode = false
        private set


    private val mOnImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        val jpgImage = reader.acquireLatestImage()

        if (jpgImage == null) {
            Timber.d("Null image")
            return@OnImageAvailableListener
        }

        val imgBuffer = jpgImage.planes[0].buffer
        val bytes = ByteArray(imgBuffer.remaining())
        imgBuffer.get(bytes)

        val imgBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)

        val matrix = Matrix()
        matrix.postRotate(90f)

        val rotatedBitmap = Bitmap.createBitmap(imgBitmap, 0, 0, imgBitmap.width, imgBitmap.height, matrix, true)
        val resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, RotorUtils.IMAGE_WIDTH, RotorUtils.IMAGE_HEIGHT, false)

        // Display the image on the imageView
        runOnUiThread(Runnable { mImageView.setImageBitmap(resizedBitmap) })

        jpgImage.close()
    }

    init {
        Timber.d("Creating RotorAiService")
        this.mUiHandler = Handler(mMainContext.mainLooper)

        // Create handler threads
        mCameraThread = HandlerThread("CameraBackground")
        mCameraThread.start()
        mCameraHandler = Handler(mCameraThread.looper)

        // Instantiate RotorCamera object
        mCamera = RotorCamera.instance
    }

    fun startAutoMode() {

        Timber.d("Starting autonomous mode")
        mRotorCtlService.setState(RotorCtlService.State.AUTONOMOUS)

        mCamera.startCapturing()

        isAutoMode = true

    }

    fun stopAutoMode() {

        Timber.d("Stopping autonomous mode")
        mRotorCtlService.setState(RotorCtlService.State.HOMED)

        mCamera.stopCapturing()

        isAutoMode = false
    }

    override fun run() {

        // Start RotorCamera session
        mCamera.initializeCamera(mMainContext, mCameraHandler, mOnImageAvailableListener)

    }

    private fun runOnUiThread(r: Runnable) {
        mUiHandler.post(r)
    }

    fun stop() {
        mCamera.shutDown()
    }
}
