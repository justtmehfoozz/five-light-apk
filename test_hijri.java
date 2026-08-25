import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.chrono.HijrahDate;
import java.time.temporal.ChronoField;
import java.util.Date;

public class test_hijri {
    public static void main(String[] args) {
        Date date = new Date();
        String tzId = "GMT-05:00";
        java.util.TimeZone cityTz = java.util.TimeZone.getTimeZone(tzId);
        
        java.util.Calendar cal = java.util.Calendar.getInstance(cityTz);
        System.out.println("cal.time: " + cal.getTime());
        
        ZoneId zoneId = cityTz.toZoneId();
        ZonedDateTime zdt = cal.getTime().toInstant().atZone(zoneId);
        System.out.println("zdt: " + zdt);
        
        HijrahDate hd = HijrahDate.from(zdt);
        System.out.println("hd: " + hd);
    }
}
