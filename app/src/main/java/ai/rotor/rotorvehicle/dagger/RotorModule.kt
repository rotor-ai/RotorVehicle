package ai.rotor.rotorvehicle.dagger

import ai.rotor.rotorvehicle.data.Blackbox
import dagger.Module
import dagger.Provides
import java.time.Clock

@Module
class RotorModule {
    @Provides
    fun provideClock() = Clock.systemUTC()!!

    @Provides
    fun provideBlackbox(clock: Clock) = Blackbox(clock)
}