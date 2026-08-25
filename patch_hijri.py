import re

with open("app/src/main/java/com/example/data/util/HijriCalc.kt", "r") as f:
    content = f.read()

target = r"""    fun getHijriDate\(date: Date = Date\(\), timeZone: java\.util\.TimeZone = java\.util\.TimeZone\.getDefault\(\)\): HijriDate \{.*?return HijriDate\(.*?year = hYear\n        \)\n    \}"""

replacement = """    fun getHijriDate(date: Date = Date(), timeZone: java.util.TimeZone = java.util.TimeZone.getDefault()): HijriDate {
        val cal = Calendar.getInstance(timeZone)
        cal.time = date
        var day = cal.get(Calendar.DAY_OF_MONTH)
        var month = cal.get(Calendar.MONTH)
        var year = cal.get(Calendar.YEAR)
        var m = month + 1
        var y = year
        if (m < 3) {
            y -= 1
            m += 12
        }
        var a = y / 100
        var b = 2 - a + a / 4
        if (y < 1583) b = 0
        if (y == 1582) {
            if (m > 10) b = -10
            if (m == 10) {
                b = 0
                if (day > 4) b = -10
            }
        }
        val jd = Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + day + b - 1524.5
        var b2 = 0
        if (jd > 2299160) {
            val a2 = Math.floor((jd - 1867216.25) / 36524.25)
            b2 = 1 + a2.toInt() - Math.floor(a2 / 4.0).toInt()
        }
        val bb = jd + b2 + 1524
        var cc = Math.floor((bb - 122.1) / 365.25)
        var dd = Math.floor(365.25 * cc)
        var ee = Math.floor((bb - dd) / 30.6001)
        day = (bb - dd - Math.floor(30.6001 * ee)).toInt()
        month = (ee - 1).toInt()
        if (ee > 13) {
            cc += 1
            month = (ee - 13).toInt()
        }
        year = (cc - 4716).toInt()
        var wd = (jd % 7).toInt() + 1
        val iyear = 10631.0 / 30.0
        val epochAstro = 1948084.0
        val epochCivil = 1948085.0
        val shift1 = 8.01 / 60.0
        var z = jd - epochCivil
        val cyc = Math.floor(z / 10631.0)
        z -= 10631 * cyc
        val j = Math.floor((z - shift1) / iyear)
        val iy = 30 * cyc + j
        z -= Math.floor(j * iyear + shift1)
        var im = Math.floor((z + 28.5001) / 29.5)
        if (im == 13.0) im = 12.0
        val id = z - Math.floor(29.5001 * im - 29)
        val hYear = iy.toInt()
        val hMonth = im.toInt()
        val hDay = id.toInt()
        val clampedMonth = hMonth.coerceIn(1, 12)
        val monthIdx = clampedMonth - 1
        return HijriDate(
            day = hDay,
            monthName = MONTH_NAMES_EN[monthIdx],
            monthArabic = MONTH_NAMES_AR[monthIdx],
            monthNumber = clampedMonth,
            year = hYear
        )
    }"""

new_content = re.sub(target, replacement, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/data/util/HijriCalc.kt", "w") as f:
    f.write(new_content)
