package ru.railway.dc.routes.event.notification.data.item;

import android.app.Notification;
import android.content.Context;
import android.os.Build;

import ru.railway.dc.routes.R;

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
            String textTime = TimeDurationUtils.getTextFromTime((int) (lastTime - time) / 1000);
            String contentTitle = title;
            if (Build.VERSION.SDK_INT >= 23) {
                builder.setSmallIcon(TimeDurationUtils.getIconFromText(context, textTime));
            } else {
                builder.setSmallIcon(R.mipmap.train_launcher);
                contentTitle = textTime + " " + contentTitle;
            }
            builder.setContentTitle(contentTitle);
            if (subTitle != null) {
                builder.setContentText(subTitle);
            }
        }
    }
}
