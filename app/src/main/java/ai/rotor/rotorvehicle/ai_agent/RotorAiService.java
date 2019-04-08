package ai.rotor.rotorvehicle.ai_agent;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;

import ai.rotor.rotorvehicle.MainActivity;
import timber.log.Timber;

public class RotorAiService implements Runnable {
    private RotorCamera mCamera;
    private Handler mCameraHandler;
    private HandlerThread mCameraThread;
    private Context mMainContext;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this.getMainContext()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Timber.d("OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);

                } break;
            }
        }
    };

    public void RotorAiService(Context context) {
        Timber.d("Creating RotorAiService");
        this.mMainContext = context;

        if (mMainContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Timber.d("No permission");
        }

        mCameraThread = new HandlerThread("CameraBackground");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());

        mCamera = RotorCamera.getInstance();
        mCamera.initializeCamera(mMainContext, mCameraHandler, mOnImageAvailableListener);

    }


    private Context getMainContext() { return mMainContext; }


    @Override
    public void run() {
        mCamera.startCapturing();
    }


    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image jpgImage = reader.acquireLatestImage();

            ByteBuffer imgBuffer = jpgImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[imgBuffer.remaining()];
            imgBuffer.get(bytes);

            // Byte array to Mat
            Mat imgMat = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_UNCHANGED);

            // Image manipulation
            Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGB2GRAY);

            // Convert to bitmap
            final Bitmap imgBitmap = Bitmap.createBitmap(imgMat.cols(), imgMat.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(imgMat, imgBitmap);
            final Bitmap resizedImgBitmap = imgBitmap.createScaledBitmap(imgBitmap, 150, 150, false);

            jpgImage.close();
        }
    };
}
