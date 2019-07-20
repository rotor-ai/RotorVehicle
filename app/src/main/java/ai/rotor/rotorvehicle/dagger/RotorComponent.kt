package ai.rotor.rotorvehicle.dagger

import ai.rotor.rotorvehicle.data.Blackbox
import dagger.Component

@Component(modules = [RotorModule::class])
interface RotorComponent {
    //The component class references one or more modules
    //This is the class that we will reference when we need to new up stuff
    //Like this:     DaggerRotorComponent.create().blackbox()

    fun blackbox(): Blackbox
}