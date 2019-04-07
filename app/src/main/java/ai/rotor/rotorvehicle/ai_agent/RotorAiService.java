package ai.rotor.rotorvehicle.ai_agent;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.os.HandlerThread;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;

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
        this.mMainContext = context;
    }

    private Context getMainContext() { return mMainContext; }

    @Override
    public void run() {

    }


}
