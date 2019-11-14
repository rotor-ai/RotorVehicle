package ai.rotor.rotorvehicle.ui.blackbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import ai.rotor.rotorvehicle.R

class BlackboxFragment : Fragment() {

    private var notificationsViewModel: BlackboxViewModel? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        notificationsViewModel = ViewModelProviders.of(this).get(BlackboxViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_blackbox, container, false)
        val textView = root.findViewById<TextView>(R.id.text_notifications)
        notificationsViewModel!!.text.observe(this, Observer { s -> textView.text = s })
        return root
    }
}