package ai.rotor.rotorvehicle.data

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class Blackbox : Timber.Tree() {

    private var behaviorSubject = BehaviorSubject.create<String>()

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        behaviorSubject.onNext(message)
    }

    fun getBehaviorSubject(): Observable<String> {
        return behaviorSubject
    }
}
