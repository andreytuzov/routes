package ru.railway.dc.routes.event.notification.data.item;

import android.app.Notification;
import android.content.Context;

/**
 * Created by SQL on 02.01.2017.
 */

public class TitleMessage implements IMessage {

    private String title;
    private String subTitle;
    private long lastTime;

    public TitleMessage(String title, String subTitle, long lastTime) {
        this.title = title;
        this.subTitle = subTitle;
        this.lastTime = lastTime;
    }

    @Override
    public void addMessage(Notification.Builder builder, long time, Context context) {
        if (time <= lastTime) {
            builder.setContentTitle(title);
            if (subTitle != null) {
                builder.setContentText(subTitle);
            }
            SmallIconUtils.addSmallIcon(lastTime - time, builder, context);
        }
    }
}
