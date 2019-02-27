package ai.rotor.rotorvehicle.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class Blackbox extends Timber.Tree {

    List<String> logEvents;


    public Blackbox() {
        this.logEvents = new ArrayList<>();
    }

    @Override
    protected void log(int priority, @Nullable String tag, @NotNull String message, @Nullable Throwable t) {
        logEvents.add(tag + " : " + message);
    }
}
