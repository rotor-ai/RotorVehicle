package ai.rotor.rotorvehicle.ui.dash;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ai.rotor.rotorvehicle.R;

public class Dash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dash_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, DashFragment.newInstance())
                    .commitNow();
        }
    }
}
