package ai.rotor.rotorvehicle.dagger

import ai.rotor.rotorvehicle.data.Blackbox
import dagger.Component


@Component(modules = [RotorTestModule::class])
interface RotorTestComponent : RotorComponent {
    override fun blackbox(): Blackbox
}