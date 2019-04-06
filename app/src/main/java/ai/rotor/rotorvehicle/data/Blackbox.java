package ai.rotor.rotorvehicle.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

public class Blackbox extends Timber.Tree {

    BehaviorSubject<String> behaviorSubject = BehaviorSubject.create();

    public Blackbox() {
        behaviorSubject.onNext("========== Start of log ==========");
    }

    @Override
    protected void log(int priority, @Nullable String tag, @NotNull String message, @Nullable Throwable t) {
        behaviorSubject.onNext(message);
    }

    public Observable<String> getBehaviorSubject() {
        return behaviorSubject;
    }
}
