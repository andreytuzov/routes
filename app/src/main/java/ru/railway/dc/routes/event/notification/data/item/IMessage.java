package ru.railway.dc.routes.event.notification.data.item;

import android.app.Notification;
import android.content.Context;

/**
 * Created by SQL on 02.01.2017.
 */

public interface IMessage {
    void addMessage(Notification.Builder builder, long time, Context context);
}
