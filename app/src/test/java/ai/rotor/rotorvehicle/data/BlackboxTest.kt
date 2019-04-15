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
    fun `Should emit starting log event when constructed`() {
        //ARRANGE
        val expectedLog = "[2019AD-01-02 13:45:56.123 UTC] $startupMsg"

        //ACT
        val testObserver = blackbox.subject.test()

        //ASSERT
        testObserver.assertValueCount(1)
        testObserver.assertValue(expectedLog)
        assertEquals(1, blackbox.getLogs().count())
        assertEquals(expectedLog, blackbox.getLogs().first())
    }

    @Test
    fun `Should emit updated list for every new log`() {
        //ARRANGE
        val testObserver = blackbox.subject.test()
        val expectedLog1 = "[2019AD-01-02 13:45:56.123 UTC] $startupMsg"
        val expectedLog2 = "[2019AD-01-02 13:45:56.123 UTC] Something happened"

        //ACT
        blackbox.d("Something happened")

        //ASSERT
        testObserver.assertValueCount(2)
        testObserver.assertValues(expectedLog1, expectedLog2)
        assertEquals(2, blackbox.getLogs().count())
        assertEquals(expectedLog2, blackbox.getLogs().last())
    }

}