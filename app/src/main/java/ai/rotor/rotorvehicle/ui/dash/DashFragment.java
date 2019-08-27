package ai.rotor.rotorvehicle.ui.dash;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Rational;
import android.util.Size;
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

    PreviewConfig frontCameraPreviewConfig =
                    new PreviewConfig.Builder()
                    .setTargetAspectRatio(new Rational(1,1))
                    .setTargetResolution(new Size(128, 128)).build();

    @BindView(R.id.frontCamView)
    TextureView frontCamPreview;

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
            frontCamPreview.post(() -> {
                //setup the camera
                Preview preview = new Preview(frontCameraPreviewConfig);
                preview.setOnPreviewOutputUpdateListener(output -> {
                    refreshCameraPreview(output);
                });
                CameraX.bindToLifecycle(this, preview);
            });

            frontCamPreview.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                //updateFrontCameraPreview();
            });
        }
    }

    private void refreshCameraPreview(Preview.PreviewOutput output) {
        ViewGroup parentView = (ViewGroup) frontCamPreview.getParent();
        parentView.removeView(frontCamPreview);
        parentView.addView(frontCamPreview, 0);
        frontCamPreview.setSurfaceTexture(output.getSurfaceTexture());

        int rotation = frontCamPreview.getDisplay().getRotation() * 90;
        float x = frontCamPreview.getWidth() / 2f;
        float y = frontCamPreview.getHeight() / 2f;

        Matrix matrix = new Matrix();
        matrix.postRotate(-rotation, x, y);
        frontCamPreview.setTransform(matrix);
    }

    private boolean hasCameraPermission() {
        return checkSelfPermission(this.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraAccess() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_ACCESS);
    }

}
