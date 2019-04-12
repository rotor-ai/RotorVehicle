package ai.rotor.rotorvehicle.data

import ai.rotor.rotorvehicle.data.Blackbox.Companion.startupMsg
import org.junit.Before
import org.junit.Test

class BlackboxTest {

    private lateinit var blackbox: Blackbox

    @Before
    fun setup() {
        blackbox = Blackbox()
    }

    @Test
    fun `Should emit starting log event when constructed`() { //ARRANGE
        //ACT
        val testObserver = blackbox.subject.test()
        testObserver.onComplete()

        //ASSERT
        testObserver.assertComplete()
        testObserver.assertValueCount(1)
        testObserver.assertValue { t -> t.contains(startupMsg) && t.count() == 1 }
    }

    @Test
    fun `Should emit updated list for every new log`() {
        //ARRANGE
        val testObserver = blackbox.subject.test()

        //ACT
        blackbox.d("Something happened")
        blackbox.subject.onComplete()

        //ASSERT
        testObserver.assertComplete()
        testObserver.assertValueCount(2)
        testObserver.assertValueAt(1) { t -> t.containsAll(arrayListOf(startupMsg, "Something happened")) }
    }

}