package ai.rotor.rotorvehicle.ui.camera

import android.Manifest
import android.content.Context
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
import androidx.lifecycle.ViewModelProviders

import ai.rotor.rotorvehicle.DashActivity
import ai.rotor.rotorvehicle.R
import ai.rotor.rotorvehicle.databinding.FragmentCameraBinding
import ai.rotor.rotorvehicle.ui.AutoFitPreviewBuilder
import butterknife.BindView
import butterknife.ButterKnife
import timber.log.Timber

import androidx.core.content.ContextCompat.checkSelfPermission

class CameraFragment : Fragment() {

    private var mViewModel: CameraViewModel? = null
    private var binding: FragmentCameraBinding? = null
    private var preview: Preview? = null

    private val REQUEST_CAMERA_ACCESS = 101

    @BindView(R.id.camTextureView)
    lateinit var camPreviewView: TextureView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Timber.d("rphdebug - CameraFragment onAttach()")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("rphdebug - CameraFragment onCreate()")
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {

        Timber.d("rphdebug - CameraFragment onCreateView")

        // Inflate view
        mViewModel = ViewModelProviders.of(this).get(CameraViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_camera, container, false)

        ButterKnife.bind(this, view)
        binding = DataBindingUtil.bind(view)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        Timber.d("rphdebug - CameraFragment onActivityCreated")

        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProviders.of(this).get(CameraViewModel::class.java)
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

    override fun onPause() {
        super.onPause()

        Timber.d("rphdebug - onPause")

    }

    companion object {

        internal fun newInstance(): CameraFragment {
            return CameraFragment()
        }
    }
}