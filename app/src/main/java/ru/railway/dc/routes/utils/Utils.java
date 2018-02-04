package ru.railway.dc.routes.utils;

import java.util.Calendar;

/**
 * Created by SQL on 12.02.2017.
 */

public class Utils {

    // Получение текстового выражения времени по количеству минут
    public static String getTextTime(int minutes) {
        String time = "";
        int d = minutes / (24 * 60);
        int m = minutes % 60;
        int h = (minutes - d * 24 * 60 - m) / 60;
        if (d != 0) {
            time += d + " д ";
        }
        if (h != 0) {
            time += h + " ч ";
        }
        if (m != 0) {
            time += m + " м";
        }
        return time.trim();
    }

    // Получение сокращенного названия станции
    public static String getShortStation(String station) {
        final int LIMIT = 15;
        String pattern = "аоеёиуэюя";
        // Если строка входит в LIMIT
        if (station.length() <= LIMIT) {
            return station;
        } else {
            int end = LIMIT - 1;
            // Пропускаем символы, записанные в pattern
            while (pattern.indexOf(station.charAt(end)) != -1) {
                end--;
            }
            return station.substring(0, end + 1) + ".";
        }
    }

    // Получение текстового выражения времени
    public static String getTextDate(Calendar date) {
        String[] months = { "янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен",
                "окт", "ноя", "дек" };
        int day = date.get(Calendar.DAY_OF_MONTH);
        int month = date.get(Calendar.MONTH);
        return day + " " + months[month];
    }

}
