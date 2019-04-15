package ai.rotor.rotorvehicle.dagger

import ai.rotor.rotorvehicle.data.Blackbox
import dagger.Module
import dagger.Provides
import java.time.Clock
import java.time.ZoneId
import java.time.temporal.ChronoField
import java.util.*

@Module
class RotorTestModule {

    @Provides
    fun provideClock(): Clock {
        val calendar = GregorianCalendar(TimeZone.getTimeZone("UTC"))
        calendar.set(2019, 0, 2, 13, 45, 56)
        var instant = calendar.toInstant()
        //building an instant from a calendar has a side effect of carrying milliseconds
        // (I presume the current milliseconds on the system clock)
        // So we have to clear those milliseconds out manually
        instant = instant.minusMillis(instant[ChronoField.MILLI_OF_SECOND].toLong())

        //then add a known number of milliseconds
        instant = instant.plusMillis(123)

        return Clock.fixed(instant, ZoneId.of("UTC"))
    }

    @Provides
    fun provideBlackbox(clock: Clock) = Blackbox(clock)

}