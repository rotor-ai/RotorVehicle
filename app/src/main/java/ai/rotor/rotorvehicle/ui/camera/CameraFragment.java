package ai.rotor.rotorvehicle.ui.camera;

import android.Manifest;
import android.content.Context;
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
import androidx.lifecycle.ViewModelProviders;

import ai.rotor.rotorvehicle.DashActivity;
import ai.rotor.rotorvehicle.R;
import ai.rotor.rotorvehicle.databinding.FragmentCameraBinding;
import ai.rotor.rotorvehicle.ui.AutoFitPreviewBuilder;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static androidx.core.content.ContextCompat.checkSelfPermission;

public class CameraFragment extends Fragment {

    private CameraViewModel mViewModel;
    private FragmentCameraBinding binding;
    private Preview preview;

    private final int REQUEST_CAMERA_ACCESS = 101;

    static CameraFragment newInstance() { return new CameraFragment(); }

    @BindView(R.id.camTextureView)
    TextureView camPreviewView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Timber.d("rphdebug - CameraFragment onAttach()");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("rphdebug - CameraFragment onCreate()");
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Timber.d("rphdebug - CameraFragment onCreateView");

        // Inflate view
        mViewModel = ViewModelProviders.of(this).get(CameraViewModel.class);
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        ButterKnife.bind(this, view);
        binding = DataBindingUtil.bind(view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        Timber.d("rphdebug - CameraFragment onActivityCreated");

        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(CameraViewModel.class);
        binding.setViewModel(mViewModel);

        setupCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_ACCESS:
                if (hasCameraPermission()) {
                    setupCamera();
                }
                break;
        }
    }

    private void setupCamera() {
        if (!hasCameraPermission()) {
            requestCameraAccess();
        }
        else {
            camPreviewView.post(() -> {
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

    @Override
    public void onPause() {
        super.onPause();

        Timber.d("rphdebug - onPause");

    }
}