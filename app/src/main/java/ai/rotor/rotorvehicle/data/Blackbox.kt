package ai.rotor.rotorvehicle.data

import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.time.Clock
import java.util.*
import javax.inject.Inject


class Blackbox @Inject constructor(clock: Clock) : Timber.Tree() {

    val subject = BehaviorSubject.create<String>()
    private val mahLogs = arrayListOf(clock.toString() + startupMsg)

    init {
        subject.onNext(Date.from(clock.instant()).toGMTString() + startupMsg)
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        mahLogs.add(message)
        subject.onNext(message)
    }

    fun getLogs(): List<String> {
        return mahLogs.toList()
    }

    companion object {
        const val startupMsg = "==========BEGINNING OF BLACKBOX LOG=========="
    }

}
