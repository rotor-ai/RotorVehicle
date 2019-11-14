package ai.rotor.rotorvehicle.ui.dash;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class DashViewModelTest {

    private DashViewModel testObj;

    @Before
    public void setup() {
        testObj = new DashViewModel();
    }

    @Test
    public void ShouldProperlyConstructViewModel() {
        assertEquals("Dashboard", testObj.getViewTitle());
    }

}
