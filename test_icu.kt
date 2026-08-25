import android.icu.util.IslamicCalendar
import android.icu.util.TimeZone

fun main() {
    val icuTz = TimeZone.getTimeZone("GMT-05:00")
    val cal = IslamicCalendar(icuTz)
    cal.calculationType = IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
    println(cal.get(IslamicCalendar.YEAR))
}
