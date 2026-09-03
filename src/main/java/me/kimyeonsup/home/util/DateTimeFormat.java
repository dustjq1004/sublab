package me.kimyeonsup.home.util;


import java.time.LocalDateTime;
import java.util.Locale;
import org.ocpsoft.prettytime.PrettyTime;

public class DateTimeFormat {

    private static final PrettyTime prettyTime = new PrettyTime(Locale.KOREA);

    public static String diffDateFromNow(LocalDateTime date) {
        return prettyTime.format(date);
    }

}
