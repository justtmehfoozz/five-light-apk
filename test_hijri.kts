import kotlin.math.floor

fun hijriToJdn(year: Int, month: Int, day: Int): Double {
    return day.toDouble() + Math.ceil(29.5 * (month - 1)) + (year - 1) * 354 + floor((3.0 + 11.0 * year) / 30.0) + 1948439.0 - 0.5
}

fun getStartWeekday(year: Int, monthNumber: Int): Int {
    val d = hijriToJdn(year, monthNumber, 1)
    val jd = floor(d + 0.5).toLong()
    return ((jd + 2) % 7).toInt() 
}

println("Muharram 1445 start: " + getStartWeekday(1445, 1)) // 1 Muharram 1445 was Wed (19 July 2023)
println("Safar 1445 start: " + getStartWeekday(1445, 2)) // 1 Safar 1445 was Fri (18 Aug 2023)
