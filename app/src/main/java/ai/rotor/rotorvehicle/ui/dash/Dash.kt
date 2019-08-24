package ai.rotor.rotorvehicle.ui.dash

import ai.rotor.rotorvehicle.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class Dash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dash_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, DashFragment.newInstance())
                    .commitNow()
        }
    }

}
