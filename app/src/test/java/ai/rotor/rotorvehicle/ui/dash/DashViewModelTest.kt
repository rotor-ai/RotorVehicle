package ai.rotor.rotorvehicle.ui.dash

import org.junit.Before
import org.junit.Test
import junit.framework.Assert.assertEquals


class DashViewModelTest {


    lateinit var viewModel: DashViewModel

    @Before
    fun setup() {
        viewModel = DashViewModel()
    }

    @Test
    fun `Should construct correctly`() {

        assertEquals("Dashboard", viewModel.viewTitle)
    }

}