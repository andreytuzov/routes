package ru.railway.dc.routes.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by SQL on 31.12.2016.
 */

public class TimeUtils {

    public static final SimpleDateFormat FORMAT_TIME = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat FORMAT_TIME_SECOND = new SimpleDateFormat("HH:mm:ss");

    // Получение кода времени по строке
    public static int getCodeByString(String time, TimeFormat timeFormat)
            throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timeFormat.format().parse(time));
        return getCodeByCalendar(calendar, timeFormat);
    }

    // Получение кода времени по календарю
    public static int getCodeByCalendar(Calendar calendar, TimeFormat timeFormat) {
        int code = -1;
        switch (timeFormat) {
            case HOUR_MINUTE:
                code = calendar.get(Calendar.HOUR_OF_DAY) * 60 +
                        calendar.get(Calendar.MINUTE);
                break;
            case HOUR_MINUTE_SECOND:
                code = calendar.get(Calendar.HOUR_OF_DAY) * 3600 +
                        calendar.get(Calendar.MINUTE) * 60 +
                        calendar.get(Calendar.SECOND);
                break;
        }
        return code;
    }

    // Получение строки времени по коду
    public static String getStringByCode(int code, TimeFormat timeFormat) {
        Calendar calendar = getCalendarByCode(code, timeFormat);
        return timeFormat.format().format(calendar.getTime());
    }

    public static Calendar getCalendarByCode(int code, TimeFormat timeFormat) {
        Calendar calendar = Calendar.getInstance();
        int hour = 0, minute = 0, second = 0;
        switch (timeFormat) {
            case HOUR_MINUTE:
                minute = code % 60;
                hour = code / 60;
                break;
            case HOUR_MINUTE_SECOND:
                second = code % 60;
                hour = code / 3600;
                minute = (code - hour * 3600) / 60;
                break;
        }
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        return calendar;
    }

    public static long getCurrentDate() {
        return Calendar.getInstance().getTimeInMillis();
    }

    // =================== ПОЛУЧЕНИЕ КЛАССА ДЛЯ ФОРМАТИРОВАНИЯ =====================================

    interface ITimeFormat {
        SimpleDateFormat format();
    }

    public enum TimeFormat implements ITimeFormat {
        HOUR_MINUTE {
            @Override
            public SimpleDateFormat format() {
                return FORMAT_TIME;
            }
        },
        HOUR_MINUTE_SECOND {
            @Override
            public SimpleDateFormat format() {
                return FORMAT_TIME_SECOND;
            }
        };
    }

}
