package ai.rotor.rotorvehicle.ai_agent;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.os.Handler;

import java.util.Collections;

import timber.log.Timber;

import static ai.rotor.rotorvehicle.RotorUtils.IMAGE_HEIGHT;
import static ai.rotor.rotorvehicle.RotorUtils.IMAGE_WIDTH;
import static ai.rotor.rotorvehicle.RotorUtils.MAX_IMAGES_USED;

public class RotorCamera {
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private ImageReader mImageReader;

    private static class InstanceHolder {
        private static RotorCamera mCamera = new RotorCamera();
    }


    public static RotorCamera getInstance() {
        return InstanceHolder.mCamera;
    }

    public void initializeCamera(Context context,
                                 Handler backgroundHandler,
                                 ImageReader.OnImageAvailableListener imageAvailableListener) {

        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        String[] camIds = {};
        try {
            camIds = manager.getCameraIdList();
        } catch (CameraAccessException e) {
            Timber.d("Cam access exception getting IDs");
        }

        if (camIds.length < 1) {
            Timber.d("No cameras found");
            return;
        }

        String id = camIds[0];
        Timber.d("Using camera id: %s", id);

        // Initialize the image processor
        mImageReader = ImageReader.newInstance(IMAGE_WIDTH, IMAGE_HEIGHT, ImageFormat.JPEG, MAX_IMAGES_USED);
        mImageReader.setOnImageAvailableListener(imageAvailableListener, backgroundHandler);

        // Open the camera resource
        try {
            Timber.d("Attempting to open the camera");
            if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Timber.d("Unable to use camera. Permission not granted.");
            } else {
                manager.openCamera(id, mStateCallback, backgroundHandler);
            }
        } catch (CameraAccessException e) {
            Timber.d("Camera access exception due to: %s", e.toString());
        }
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Timber.d("Opened camera");
            mCameraDevice = camera;
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Timber.d("Closed camera");
            mCameraDevice = camera;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Timber.d("Error using camera device, closing. Error code: %s", error);
            camera.close();
        }

        @Override
        public void onClosed(CameraDevice camera) {
            super.onClosed(camera);
            Timber.d("Closed camera, releasing");
            mCameraDevice = null;
        }
    };

    public void startCapturing() {
        Timber.d("Starting camera capture session");

        if (mCameraDevice == null) {
            Timber.d("Cannot take picture, camera not initialized");
            return;
        }

        try {
            mCameraDevice.createCaptureSession(
                    Collections.singletonList(mImageReader.getSurface()),
                    mSessionCallback,
                    null);
        } catch (CameraAccessException e) {
            Timber.d("access exception while preparing pic due to: %s", e.toString());
        }
    }

    private CameraCaptureSession.StateCallback mSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            // The camera is already closed
            if (mCameraDevice == null) {
                return;
            }

            // When the session is ready, we start capture
            mCaptureSession = session;
            triggerImageCapturing();
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            Timber.d("Failed to configure camera");
        }
    };

    private void triggerImageCapturing() {
        try {
            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            Timber.d("Capture session initialized");
            mCaptureSession.setRepeatingRequest(captureBuilder.build(), mCaptureCallback, null);
        } catch (CameraAccessException e) {
            Timber.d("Camera access exception: %s", e.toString());
        }
    }

    private final CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            Timber.d("Partial result");
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

        }
    };

    public void stopCapturing() {
        Timber.d("Stopping camera capture session");

        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;

            Timber.d("Capture session closed");
        }
    }

    public void shutDown() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
        }
    }

    public boolean isOpen() {
        if (mCameraDevice != null) {
            return true;
        } else {
            return false;
        }
    }
}
