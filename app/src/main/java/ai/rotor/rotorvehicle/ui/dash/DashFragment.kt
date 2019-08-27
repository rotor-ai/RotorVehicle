package ai.rotor.rotorvehicle.ui.dash

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.rotor.rotorvehicle.R
import ai.rotor.rotorvehicle.databinding.DashFragmentBinding
import android.Manifest
import android.content.pm.PackageManager
import android.view.TextureView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import butterknife.BindView
import butterknife.ButterKnife

class DashFragment : Fragment() {

    companion object {
        fun newInstance() = DashFragment()
    }

    private lateinit var viewModel: DashViewModel
    private var binding: DashFragmentBinding? = null

    @BindView(R.id.viewFinder)
    lateinit var mViewFinder: TextureView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val view = inflater.inflate(R.layout.dash_fragment, container, false)
        ButterKnife.bind(view)
        binding = DataBindingUtil.bind(view)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DashViewModel::class.java)
        binding?.viewmodel = viewModel
    }

}
