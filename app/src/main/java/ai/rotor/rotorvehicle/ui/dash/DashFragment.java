package ai.rotor.rotorvehicle.ui.dash;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import ai.rotor.rotorvehicle.ui.AutoFitPreviewBuilder;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static androidx.core.content.ContextCompat.checkSelfPermission;


public class DashFragment extends Fragment implements LifecycleOwner {

    private DashViewModel mViewModel;

    private final int REQUEST_CAMERA_ACCESS = 101;

    private DashFragmentBinding binding;
    private Preview preview;

    static DashFragment newInstance() {
        return new DashFragment();
    }

    @BindView(R.id.frontCamView)
    TextureView camPreviewView;

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
            camPreviewView.post(() -> {
                //setup the camera preview using Google's AutoFit builder from the CameraX demo (used with attribution [Apache 2.0])
                preview = AutoFitPreviewBuilder.Companion.build(camPreviewConfig(), camPreviewView);
                CameraX.bindToLifecycle(this, preview);
            });
        }

    }

    private PreviewConfig camPreviewConfig() {
        return new PreviewConfig.Builder()
                .setTargetAspectRatio(new Rational(camPreviewView.getMeasuredWidth(), camPreviewView.getMeasuredHeight()))
                .setTargetRotation(camPreviewView.getDisplay().getRotation())
                .build();
    }

    private boolean hasCameraPermission() {
        return checkSelfPermission(this.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraAccess() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_ACCESS);
    }

}
