package ai.rotor.rotorvehicle.data

import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class Blackbox : Timber.Tree() {

    val subject = BehaviorSubject.create<String>()
    private val mahLogs = arrayListOf(startupMsg)

    init {
        subject.onNext(startupMsg)
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        mahLogs.add(message)
        subject.onNext(message)
    }

    fun getLogs(): List<String> { return mahLogs.toList() }

    companion object {
        const val startupMsg = "==========BEGINNING OF BLACKBOX LOG=========="
    }

}
