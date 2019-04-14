package ai.rotor.rotorvehicle.dagger

import ai.rotor.rotorvehicle.data.Blackbox
import dagger.Module
import dagger.Provides
import java.time.Clock
import java.time.ZoneId
import java.util.*

@Module
class RotorTestModule {

    @Provides
    fun provideClock() = Clock.fixed(GregorianCalendar(2019, 0, 2, 3, 4, 5).toInstant(), ZoneId.systemDefault())

    @Provides
    fun provideBlackbox(clock: Clock) = Blackbox(clock)

}