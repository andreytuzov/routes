package ru.railway.dc.routes.event;

import android.content.Context;

import java.util.Calendar;
import java.util.List;

import ru.railway.dc.routes.database.utils.EventTableUtils;
import ru.railway.dc.routes.event.notification.NotificationTime;
import ru.railway.dc.routes.event.notification.data.MsgSchedule;
import ru.railway.dc.routes.event.notification.data.factory.MsgScheduleFactory;

/**
 * Created by SQL on 09.01.2017.
 */

public class ManagerEventUtils {

    // ==================================== ДОБАВЛЕНИЕ СОБЫТИЙ =====================================

    public static void addNotification(Context context, int eventID) {
        add(context, eventID, true, false, false);
    }

    public static void addNotificationAll(Context context) {
        addAll(context, true, false, false);
    }

    // Загрузка одного события
    private static void add(Context context, int eventID,
                            boolean isNotification, boolean isWidget, boolean isActivity) {
        // Если уведомления, то добавляем
        if (isNotification) {
            MsgSchedule msg = MsgScheduleFactory.parse(context, eventID, true, true);
            if (msg == null) {
                return;
            }
            ManagerEvent.add(new NotificationTime(context, msg, eventID), eventID,
                    ManagerEvent.PREF_NOTIFICATION);
        }
    }

    // Добавление всех событий
    private static void addAll(Context context,
                            boolean isNotification, boolean isWidget, boolean isActivity) {
        List<EventTableUtils.EventID> list = EventTableUtils.loadEventIDs(
                Calendar.getInstance().getTimeInMillis());
        if (list == null) {
            return;
        }
        for (EventTableUtils.EventID eventID : list) {
            // Проверяем, что данные нужны
            if (isActivity ||
                    (isNotification && eventID.isNotification()) ||
                    (isWidget && eventID.isWidget())) {
                // Если уведомление
                if (isNotification && eventID.isNotification()){
                    add(context, eventID.getId(), true, false, false);
                }
            }
        }
    }

}
