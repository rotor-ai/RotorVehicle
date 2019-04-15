package ai.rotor.rotorvehicle.data

import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject


class Blackbox @Inject constructor(val clock: Clock) : Timber.Tree() {

    val subject = BehaviorSubject.create<String>()
    private val mahLogs = arrayListOf<String>()
    private val timestampFormat: DateTimeFormatter = DateTimeFormatterBuilder()
            .appendPattern("YYYY-MM-dd HH:mm:ss.SSS ")
            .appendZoneText(TextStyle.NARROW)
            .toFormatter()

    init {
        record(getFormattedTimeStamp() + " " + startupMsg)
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        record(getFormattedTimeStamp() + " " + message)
    }

    fun getLogs(): List<String> = mahLogs.toList()

    private fun record(line: String) {
        mahLogs.add(line)
        subject.onNext(line)
    }

    private fun getFormattedTimeStamp(): String {

        val timestamp = ZonedDateTime.ofInstant(clock.instant(), TimeZone.getTimeZone("UTC").toZoneId()).format(timestampFormat)

        return "[$timestamp]"
    }

    companion object {
        const val startupMsg = "==========BEGINNING OF BLACKBOX LOG=========="
    }
}
