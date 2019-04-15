package ai.rotor.rotorvehicle.data

import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.sql.Timestamp
import java.text.DateFormat
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.time.temporal.TemporalField
import java.util.*
import javax.inject.Inject


class Blackbox @Inject constructor(val clock: Clock) : Timber.Tree() {

    val subject = BehaviorSubject.create<String>()
    private val mahLogs = arrayListOf<String>()

    init {
        record(getFormatedTimeStamp() + " " + startupMsg)
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        record(getFormatedTimeStamp() + " " + message)
    }

    fun getLogs(): List<String> {
        return mahLogs.toList()
    }


    private fun record(line: String) {
        mahLogs.add(line)
        subject.onNext(line)
    }

    private fun getFormatedTimeStamp(): String {

        val timestampFormat = DateTimeFormatterBuilder()
                .appendPattern("YYYYG-MM-DD HH:mm:ss.SSS ")
                .appendZoneText(TextStyle.SHORT)
                .toFormatter()

        val timestamp = ZonedDateTime.ofInstant(clock.instant(), ZoneId.of("UTC")).format(timestampFormat)

        return "[$timestamp]"
    }
    companion object {
        const val startupMsg = "==========BEGINNING OF BLACKBOX LOG=========="
    }
}
