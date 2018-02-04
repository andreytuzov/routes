package ru.railway.dc.routes.event;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.railway.dc.routes.tools.AppUtils;

/**
 * Created by SQL on 04.01.2017.
 */

public class ManagerEvent {

    public static final Logger logger = Logger.getLogger(ManagerEvent.class);

    private static List<ITime> listTime = new ArrayList<>();
    private static Map<String, ITime> mapEventID = new HashMap<>();

    public final static String PREF_NOTIFICATION = "n";
    public final static String PREF_ACTIVITY = "a";
    public final static String PREF_WIDGET = "w";

    public static boolean isEmpty() {
        return listTime.isEmpty();
    }

    // Обновление с указанным периодом
    public static void update() {
        logger.debug("update");
        long code = Calendar.getInstance().getTimeInMillis();
        List<ITime> list = new ArrayList<>();
        for (ITime iTime : listTime) {
            if (iTime.update(code)) {
                list.add(iTime);
            }
            logger.debug("iTime = " + iTime);
        }

        if (!list.isEmpty()) {
            // Чистим карту
            List<String> keys = new ArrayList<>();
            for (String key : mapEventID.keySet()) {
                if (list.contains(mapEventID.get(key))) {
                    keys.add(key);
                }
            }
            mapEventID.remove(keys);
            // Удаляем завершенные
            listTime.removeAll(list);
        }
    }


    // =============================== РАБОТА СО СПИСКОМ ===========================================

    private static void add(ITime iTime) {
        if (iTime == null) {
            logger.error("iTime is null");
        }
        if (listTime == null) {
//            logger.error("ManagerEvent is not configured");
        } else {
            listTime.add(iTime);
            if (listTime.size() == 1) {
                AppUtils.startEventService();
            }
            update();
            logger.info(listTime.size() + ") Event объект добавлен в ManagerEvent, iTime: " + iTime);
        }
    }

    public static void add(ITime iTime, int eventID, String pref) {
        if (iTime != null && !mapEventID.containsKey(pref + eventID)) {
            logger.debug("Event add, iTime = " + iTime);
            logger.debug("listTime.size = " + listTime.size() +
                    ", mapEventID.size = " + mapEventID.size());
            mapEventID.put(pref + eventID, iTime);
            add(iTime);
        }
    }

    private static void remove(ITime iTime) {
        if (iTime == null) {
            logger.error("iTime is null");
        }
        if (listTime == null) {
            logger.error("ManagerEvent is not configured");
        } else {
            listTime.remove(iTime);
            logger.debug(listTime.size() + ") remove iTime = " + iTime);
        }
    }

    public static void remove(int eventID, String pref) {
        remove(mapEventID.get(pref + eventID));
        mapEventID.remove(pref + eventID);
        logger.debug(listTime.size() + ") remove");
    }

    public static void removeAll() {
        listTime.clear();
        mapEventID.clear();
    }

    public void removeAllPref(String pref) {

        List<ITime> list = new ArrayList<>();
        for (String eventID : mapEventID.keySet()) {
            if (eventID.startsWith(pref)) {
                ITime iTime = mapEventID.get(eventID);
                remove(iTime);
                list.add(iTime);
            }
        }
        // Очищаем карту
        mapEventID.remove(list);
    }
}
