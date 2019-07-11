package ai.rotor.rotorvehicle.ui.monitor;

import androidx.test.core.app.ActivityScenario;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Test //This is a very basic ActivityScenario test
    public void canStartAndFinishActivity() {
        ActivityScenario scenario = ActivityScenario.launch(MainActivity.class);
        scenario.close();
    }

}
