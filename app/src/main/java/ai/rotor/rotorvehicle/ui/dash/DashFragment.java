package ai.rotor.rotorvehicle.ui.dash;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
            });
        }
    }

    private void actuallySetupCamera() {
        //setup the camera
        Preview preview = new Preview(cameraPreviewConfig());

        preview.setOnPreviewOutputUpdateListener(this::refreshCameraPreview);

        CameraX.unbindAll();
        CameraX.bindToLifecycle(this, preview);
    }

    private PreviewConfig cameraPreviewConfig() {
        int wid = frontCamPreviewTextureView.getMeasuredWidth();
        int height = frontCamPreviewTextureView.getMeasuredHeight();
        return new PreviewConfig.Builder()
                .setTargetResolution(new Size(wid, height))
                .setTargetAspectRatio(new Rational(wid,height))
                .setTargetRotation(frontCamPreviewTextureView.getDisplay().getRotation())
                .build();
    }

    private void refreshCameraPreview(Preview.PreviewOutput output) {
        ViewGroup parentView = (ViewGroup) frontCamPreviewTextureView.getParent();
        parentView.removeView(frontCamPreviewTextureView);
        parentView.addView(frontCamPreviewTextureView, 0);
        setCameraPreviewTransform(output);
        frontCamPreviewTextureView.setSurfaceTexture(output.getSurfaceTexture());

    }

    private void setCameraPreviewTransform(Preview.PreviewOutput output) {
        int rotation = frontCamPreviewTextureView.getDisplay().getRotation() * 90;
        Size sourceImageSize = output.getTextureSize();
        Size viewSize = new Size(frontCamPreviewTextureView.getMeasuredWidth(), frontCamPreviewTextureView.getMeasuredHeight());
        float viewCenterx = viewSize.getWidth() / 2f;
        float viewCentery = viewSize.getHeight() / 2f;
        Timber.d(">>>>sourceImageSize: " + sourceImageSize + " viewSize: " + viewSize);

        Matrix matrix = new Matrix();
        matrix.postRotate(-rotation, viewCenterx, viewCentery);

        float sourceAspectRatio = sourceImageSize.getWidth() / (float)sourceImageSize.getHeight();
        float viewAspectRatio = viewSize.getWidth() / (float) viewSize.getHeight();

        Timber.d(">>>>sourceAspectRatio: " + sourceAspectRatio + " viewAspectRatio: " + viewAspectRatio);

        float sy = viewSize.getWidth() / (float) sourceImageSize.getHeight();
        float sx = viewSize.getHeight() / (float) sourceImageSize.getWidth();

        //normalize our results
        float sxFinal = 1;
        float syFinal = sy/sx;

        Timber.d(">>>>sxFinal: " + sxFinal + " syFinal: " + syFinal);
        matrix.preScale(sxFinal, syFinal, viewCenterx, viewCentery);//stretch the texture height to match the source aspect ratio

        frontCamPreviewTextureView.setTransform(matrix);
    }

    private boolean hasCameraPermission() {
        return checkSelfPermission(this.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraAccess() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_ACCESS);
    }

}
