package ai.rotor.rotorvehicle.ui.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import ai.rotor.rotorvehicle.R
import timber.log.Timber

class ControllerFragment : Fragment() {

    private var controllerViewModel: ControllerViewModel? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        controllerViewModel = ViewModelProviders.of(this).get(ControllerViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_controller, container, false)
        val textView = view.findViewById<TextView>(R.id.text_home)
        controllerViewModel!!.text.observe(this, Observer { s -> textView.text = s })

        Timber.d("rhdebug - Creating ControllerFragment")

        return view
    }
}