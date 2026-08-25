import com.example.data.util.PrayerCalc
import com.example.data.model.CalcMethod
import com.example.data.model.Madhab
import com.example.data.model.PrayerName
import java.util.Date

fun main() {
    val timesStd = PrayerCalc.calculatePrayerTimes(19.0760, 72.8777, Date(), CalcMethod.MWL, Madhab.STANDARD, 5.5, false)
    val timesHan = PrayerCalc.calculatePrayerTimes(19.0760, 72.8777, Date(), CalcMethod.MWL, Madhab.HANAFI, 5.5, false)
    println(timesStd.find { it.name == PrayerName.ASR })
    println(timesHan.find { it.name == PrayerName.ASR })
}
