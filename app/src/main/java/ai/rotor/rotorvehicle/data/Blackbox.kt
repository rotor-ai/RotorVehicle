package ai.rotor.rotorvehicle.data

import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class Blackbox : Timber.Tree() {

    val behaviorSubject = BehaviorSubject.create<String>()

    init {
        behaviorSubject.onNext("==========BEGINNING OF BLACKBOX LOG==========")
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        behaviorSubject.onNext(message)
    }

}
