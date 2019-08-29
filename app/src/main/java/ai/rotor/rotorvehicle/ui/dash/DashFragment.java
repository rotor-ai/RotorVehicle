package ai.rotor.rotorvehicle.ui.dash;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Rational;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;

import ai.rotor.rotorvehicle.R;
import ai.rotor.rotorvehicle.databinding.DashFragmentBinding;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static androidx.core.content.ContextCompat.checkSelfPermission;


public class DashFragment extends Fragment implements LifecycleOwner {

    private DashViewModel mViewModel;

    private final int REQUEST_CAMERA_ACCESS = 101;

    private DashFragmentBinding binding;

    static DashFragment newInstance() {
        return new DashFragment();
    }

    @BindView(R.id.frontCamView)
    TextureView frontCamPreviewTextureView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dash_fragment, container, false);
        ButterKnife.bind(this, view);
        binding = DataBindingUtil.bind(view);

        Timber.plant(new Timber.DebugTree());

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(DashViewModel.class);
        binding.setViewModel(mViewModel);

        setupCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_CAMERA_ACCESS:
                if (hasCameraPermission()) {
                    setupCamera();
                }
                break;
        }
    }

    private void setupCamera() {
        if (!hasCameraPermission()){
            requestCameraAccess();
        }
        else {
            frontCamPreviewTextureView.post(() -> {
                actuallySetupCamera();
                frontCamPreviewTextureView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        Timber.d(">>>>left: %d top: %d right: %d bottom: %d oldLeft: %d oldTop: %d oldRight: %d oldBottom: %d", left, top, right,bottom, oldLeft, oldTop, oldRight, oldBottom);
                        setCameraPreviewTransform();
                    }
                });
            });
        }
    }

    private void actuallySetupCamera() {
        //setup the camera
        Preview preview = new Preview(cameraPreviewConfig());

        preview.setOnPreviewOutputUpdateListener(output -> {
            refreshCameraPreview(output);
        });
        CameraX.unbindAll();
        CameraX.bindToLifecycle(this, preview);
    }

    private PreviewConfig cameraPreviewConfig() {
        int wid = frontCamPreviewTextureView.getMeasuredWidth();
        int height = frontCamPreviewTextureView.getMeasuredHeight();
        Timber.d(">>>>>wid: " + wid + " height: " + height);
        return new PreviewConfig.Builder()
                .setTargetAspectRatio(new Rational(wid, height))
                .setTargetRotation(frontCamPreviewTextureView.getDisplay().getRotation())
                .build();
    }

    private void refreshCameraPreview(Preview.PreviewOutput output) {
        ViewGroup parentView = (ViewGroup) frontCamPreviewTextureView.getParent();
        parentView.removeView(frontCamPreviewTextureView);
        parentView.addView(frontCamPreviewTextureView, 0);
        frontCamPreviewTextureView.setSurfaceTexture(output.getSurfaceTexture());
    }

    private void setCameraPreviewTransform() {
        int rotation = frontCamPreviewTextureView.getDisplay().getRotation() * 90;
        float wid = frontCamPreviewTextureView.getMeasuredWidth();
        float height = frontCamPreviewTextureView.getMeasuredHeight();
        Timber.d("width: %f, height: %f", wid, height);
        float x = wid / 2f;
        float y = height / 2f;

        Timber.d(">>>>>rotation: " + rotation);

        Matrix matrix = new Matrix();
        matrix.postRotate(-rotation, x, y);
        if (rotation != 0 && rotation != 180) {
            matrix.preScale((height/wid), (wid/height), x, y);
        }
        frontCamPreviewTextureView.setTransform(matrix);
    }

    private boolean hasCameraPermission() {
        return checkSelfPermission(this.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraAccess() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_ACCESS);
    }

}
