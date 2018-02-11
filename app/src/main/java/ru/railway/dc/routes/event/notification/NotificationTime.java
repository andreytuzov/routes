package ru.railway.dc.routes.event.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import ru.railway.dc.routes.FavouriteActivity;
import ru.railway.dc.routes.database.utils.EventTableUtils;
import ru.railway.dc.routes.event.ITime;
import ru.railway.dc.routes.event.notification.data.MsgSchedule;

public class NotificationTime implements ITime {

    public static final String PARAM_EVENT_ID = "eventID";

    private int eventID;

    private MsgSchedule msg;

    private PendingIntent pendingIntent;
//    private Notification.Action action;
    private NotificationManager nm;
    private Context context;

    public NotificationTime(Context context, MsgSchedule msg, int eventID) {
        this.context = context;
        this.eventID = eventID;

        // Добавляем PendingIntent
        Intent intent = new Intent(context, FavouriteActivity.class);
        intent.putExtra(PARAM_EVENT_ID, eventID);
        // Делаем intent разными
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

//        Intent intentButton = new Intent(context, MainActivity.class);
//        intentButton.putExtra(PARAM_EVENT_ID, eventID);
//        intentButton.setData(Uri.parse(intentButton.toUri(Intent.URI_INTENT_SCHEME)));
//        PendingIntent piButton = PendingIntent.getActivity(context, 0, intentButton,
//                PendingIntent.FLAG_UPDATE_CURRENT);


//        action = new Notification.Action.Builder(android.R.drawable.ic_menu_edit,
//                "Изменение маршрута", piButton).build();

        this.nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.msg = msg;
    }

    @Override
    public boolean update(long code) {
        // Добавляем заголовки
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentIntent(pendingIntent);
        // Добавляем эффекты
        if (msg.addMessage(code, builder)) {
            builder.setSmallIcon(android.R.drawable.ic_menu_delete);
            builder.setContentTitle("Событие завершено");
            nm.notify(eventID, builder.build());
            EventTableUtils.update(eventID, false, null);
            return true;
        }
//        builder.addAction(action);
        if (Build.VERSION.SDK_INT >= 17)
            builder.setShowWhen(false);
        builder.setOngoing(true);
        nm.notify(eventID, builder.build());
        return false;
    }
}
