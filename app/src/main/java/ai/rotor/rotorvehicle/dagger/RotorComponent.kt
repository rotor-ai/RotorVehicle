package ai.rotor.rotorvehicle.dagger

import ai.rotor.rotorvehicle.data.Blackbox
import dagger.Component

@Component(modules = [RotorModule::class])
interface RotorComponent {

    fun blackbox(): Blackbox
}