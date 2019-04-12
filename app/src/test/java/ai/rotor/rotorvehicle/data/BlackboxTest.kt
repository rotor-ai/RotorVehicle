package ai.rotor.rotorvehicle.data

import io.reactivex.observers.TestObserver
import org.junit.Test

class BlackboxTest {

    @Test
    fun `Should emit starting log event when constructed`() {
        val blackbox = Blackbox()

        val testObserver = TestObserver<String>()

        blackbox.behaviorSubject.subscribe(testObserver)

        testObserver.assertValue("==========BEGINNING OF BLACKBOX LOG==========")

        testObserver.dispose()

    }
}