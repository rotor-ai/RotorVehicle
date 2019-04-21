package ai.rotor.rotorvehicle.ai_agent;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;

import timber.log.Timber;

public class RotorAiService implements Runnable {
    private RotorCamera mCamera;
    private Handler mCameraHandler;
    private Handler mUiHandler;
    private ImageView mImageView;
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
                default: {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public RotorAiService(Context context, ImageView imageView) {
        Timber.d("Creating RotorAiService");
        this.mMainContext = context;
        this.mImageView = imageView;
        this.mUiHandler = new Handler(mMainContext.getMainLooper());

        if (mMainContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Timber.d("Unable to use camera. Permission not granted.");
        }

        // Load OpenCV
        if (!OpenCVLoader.initDebug()) {
            Timber.d("Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, mMainContext, mLoaderCallback);
        } else {
            Timber.d("OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        // Create handler threads
        mCameraThread = new HandlerThread("CameraBackground");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());

        // Instantiate RotorCamera object
        mCamera = RotorCamera.getInstance();
    }

    public void startAutoMode() {
        mCamera.startCapturing();
    }

    public void stopAutoMode() {
        mCamera.stopCapturing();
    }


    private Context getMainContext() { return mMainContext; }


    @Override
    public void run() {

        if (mMainContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Timber.d("No permission");
        }

        // Start RotorCamera session
        mCamera.initializeCamera(mMainContext, mCameraHandler, mOnImageAvailableListener);

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

            // Display the image on the imageView
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mImageView.setImageBitmap(resizedImgBitmap);
                }
            });

            jpgImage.close();
        }
    };

    private void runOnUiThread(Runnable r) {
        mUiHandler.post(r);
    }
}
