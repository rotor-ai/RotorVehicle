package ai.rotor.rotorvehicle

import ai.rotor.rotorvehicle.dagger.DaggerRotorComponent
import ai.rotor.rotorvehicle.dagger.RotorComponent
import android.app.Application
import timber.log.Timber

class RotorApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.i("starting rotor.ai vehicle...")
    }
}