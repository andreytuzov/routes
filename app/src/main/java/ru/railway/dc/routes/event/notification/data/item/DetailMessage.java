package ru.railway.dc.routes.event.notification.data.item;

import android.app.Notification;
import android.content.Context;

/**
 * Created by SQL on 02.01.2017.
 */

public class DetailMessage implements IMessage {

    private String detailInfo;
    private long lastTime;

    public DetailMessage(String detailInfo, long lastTime) {
        this.detailInfo = detailInfo;
        this.lastTime = lastTime;
    }

    @Override
    public void addMessage(Notification.Builder builder, long time, Context context) {
        if (time <= lastTime) {
            builder.setContentText(detailInfo + getStringByCode(lastTime - time));
        }
    }

    private String getStringByCode(long code) {

        int time = (int) code / 1000;

        int second = time % 60;
        int hour = time / 3600;
        int minute = (time - hour * 3600 - second) / 60;

        String text = "";
        if (hour >= 10) {
            text = hour + "ч";
        } else if (hour >= 1) {
            text = String.format("%1$d,%2$dч", hour, (int) minute / 6);
        } else if (minute >= 10) {
            text = minute + "м";
        } else if (minute >= 1) {
            text = String.format("%1$d,%2$dм", minute, (int) second / 6);
        } else {
            text = second + "с";
        }
        return text;
    }



}
