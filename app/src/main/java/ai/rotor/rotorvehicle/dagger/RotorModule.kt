package ai.rotor.rotorvehicle.dagger

import ai.rotor.rotorvehicle.data.Blackbox
import dagger.Module
import dagger.Provides
import java.time.Clock

@Module
class RotorModule {
    //The module class is where you can define @Provides methods to new up injectable classes!

    @Provides//For example, this line tells dagger to use Clock.systemUTC() when it needs to create a Clock object
    fun provideClock() = Clock.systemUTC()!!

    @Provides
    fun provideBlackbox(clock: Clock) = Blackbox(clock)
}