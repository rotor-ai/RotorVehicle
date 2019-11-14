package ai.rotor.rotorvehicle.ui.dash

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Rational
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders

import ai.rotor.rotorvehicle.R
import ai.rotor.rotorvehicle.databinding.DashFragmentBinding
import ai.rotor.rotorvehicle.ui.AutoFitPreviewBuilder
import butterknife.BindView
import butterknife.ButterKnife
import timber.log.Timber

import androidx.core.content.ContextCompat.checkSelfPermission


class DashFragment : Fragment(), LifecycleOwner {

    private var mViewModel: DashViewModel? = null

    private val REQUEST_CAMERA_ACCESS = 101

    private var binding: DashFragmentBinding? = null
    private var preview: Preview? = null

    @BindView(R.id.frontCamView)
    lateinit var camPreviewView: TextureView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dash_fragment, container, false)
        ButterKnife.bind(this, view)
        binding = DataBindingUtil.bind(view)

        Timber.plant(Timber.DebugTree())

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProviders.of(this).get(DashViewModel::class.java)
        binding!!.viewModel = mViewModel

        setupCamera()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_ACCESS -> if (hasCameraPermission()) {
                setupCamera()
            }
        }
    }

    private fun setupCamera() {
        if (!hasCameraPermission()) {
            requestCameraAccess()
        } else {
            camPreviewView!!.post {
                //setup the camera preview using Google's AutoFit builder from the CameraX demo (used with attribution [Apache 2.0])
                preview = AutoFitPreviewBuilder.build(camPreviewConfig(), camPreviewView!!)
                CameraX.bindToLifecycle(this, preview!!)
            }
        }

    }

    private fun camPreviewConfig(): PreviewConfig {
        return PreviewConfig.Builder()
                .setTargetAspectRatio(Rational(camPreviewView!!.measuredWidth, camPreviewView!!.measuredHeight))
                .setTargetRotation(camPreviewView!!.display.rotation)
                .build()
    }

    private fun hasCameraPermission(): Boolean {
        return checkSelfPermission(this.context!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraAccess() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_ACCESS)
    }

    companion object {

        internal fun newInstance(): DashFragment {
            return DashFragment()
        }
    }

}
