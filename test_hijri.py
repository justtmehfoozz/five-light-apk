import java.time.ZonedDateTime;
import java.time.chrono.HijrahDate;

public class test_hijri {
    public static void main(String[] args) {
        System.out.println(HijrahDate.from(ZonedDateTime.now()));
    }
}
