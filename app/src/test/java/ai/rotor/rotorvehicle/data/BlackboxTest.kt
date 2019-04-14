package ai.rotor.rotorvehicle.data

import ai.rotor.rotorvehicle.dagger.DaggerRotorTestComponent
import ai.rotor.rotorvehicle.data.Blackbox.Companion.startupMsg
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BlackboxTest {

    private lateinit var blackbox: Blackbox

    @Before
    fun setup() {
        blackbox = DaggerRotorTestComponent.create().blackbox()
    }

    @Test
    fun `Should emit starting log event when constructed`() { //ARRANGE
        //ACT
        val testObserver = blackbox.subject.test()

        //ASSERT
        testObserver.assertValueCount(1)
        testObserver.assertValue(startupMsg)
        assertEquals(1, blackbox.getLogs().count())
        assertEquals(startupMsg, blackbox.getLogs().first())
    }

    @Test
    fun `Should emit updated list for every new log`() {
        //ARRANGE
        val testObserver = blackbox.subject.test()

        //ACT
        blackbox.d("Something happened")

        //ASSERT
        testObserver.assertValueCount(2)
        testObserver.assertValues(startupMsg, "Something happened")
        assertEquals(2, blackbox.getLogs().count())
        assertEquals("Something happened", blackbox.getLogs().last())
    }

}