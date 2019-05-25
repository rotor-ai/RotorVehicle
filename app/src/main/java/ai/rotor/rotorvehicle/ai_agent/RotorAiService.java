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
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.nio.ByteBuffer;

import ai.rotor.rotorvehicle.rotor_ctl.RotorCtlService;
import ai.rotor.rotorvehicle.ui.monitor.MainActivity;
import timber.log.Timber;

public class RotorAiService implements Runnable {
    private RotorCamera mCamera;
    private Handler mCameraHandler;
    private Handler mUiHandler;
    private ImageView mImageView;
    private HandlerThread mCameraThread;
    private Context mMainContext;
    private RotorCtlService mRotorCtlService;

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

    public RotorAiService(Context context, ImageView imageView, RotorCtlService rotorCtlService) {
        Timber.d("Creating RotorAiService");
        this.mMainContext = context;
        this.mImageView = imageView;
        this.mUiHandler = new Handler(mMainContext.getMainLooper());
        this.mRotorCtlService = rotorCtlService;

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

        Timber.d("Starting autonomous mode");
        mRotorCtlService.setState(RotorCtlService.State.AUTONOMOUS);

        mCamera.startCapturing();

    }

    public void stopAutoMode() {

        Timber.d("Stopping autonomous mode");
        mRotorCtlService.setState(RotorCtlService.State.HOMED);

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
            Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_BGR2HSV);

            int hYellowLow = 20;
            int hYellowHigh = 30;
            int hBlueLow = 75;
            int hBlueHigh = 85;
            int sLow = 100;
            int sHigh = 255;
            int vLow = 50;
            int vHigh = 255;

            Mat yellowMask = new Mat();
            Mat blueMask = new Mat();

            Core.inRange(imgMat, new Scalar(hBlueLow, sLow, vLow), new Scalar(hBlueHigh, sHigh, vHigh), blueMask);
            Core.inRange(imgMat, new Scalar(hYellowLow, sLow, vLow), new Scalar(hYellowHigh, sHigh, vHigh), yellowMask);

            Moments blueMoments = Imgproc.moments(blueMask);
            int xBlue = (int) (blueMoments.get_m10() / blueMoments.get_m00());
            int yBlue = (int) (blueMoments.get_m01() / blueMoments.get_m00());

            Moments yellowMoments = Imgproc.moments(yellowMask);
            int xYellow = (int) (yellowMoments.get_m10() / yellowMoments.get_m00());
            int yYellow = (int) (yellowMoments.get_m01() / yellowMoments.get_m00());

//            Mat filteredHsv = new Mat();
//            imgMat.copyTo(filteredHsv, mask);

            int width = 80;
            int height = 80;
            int thickness = 5;

            Imgproc.rectangle(imgMat, new Point(xBlue - width, yBlue - width), new Point(xBlue + width, yBlue + height), new Scalar(255, 255, 255), thickness);
            Imgproc.rectangle(imgMat, new Point(xYellow - width, yYellow - width), new Point(xYellow + width, yYellow + height), new Scalar(255, 255, 255), thickness);

            // Convert back to RGB
            Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_HSV2RGB);

            // Convert to bitmap
            final Bitmap imgBitmap = Bitmap.createBitmap(imgMat.cols(), imgMat.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(imgMat, imgBitmap);
            final Bitmap resizedImgBitmap = Bitmap.createScaledBitmap(imgBitmap, 150, 150, false);

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
