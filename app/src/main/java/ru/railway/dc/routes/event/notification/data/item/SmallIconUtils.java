package ru.railway.dc.routes.event.notification.data.item;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Icon;
import android.os.Build;

/**
 * Created by SQL on 03.01.2017.
 */

public class SmallIconUtils {

    public static void addSmallIcon(long code, Notification.Builder builder, Context context) {

        int time = (int) code / 1000;

        int second = time % 60;
        int hour = time / 3600;
        int minute = (time - hour * 3600 - second) / 60;

        // Если версия андроид больше или равна 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String textIcon = "";
            if (hour >= 10) {
                textIcon = hour + "ч";
            } else if (hour >= 1) {
                textIcon = String.format("%1$d,%2$dч", hour, (int) minute / 6);
            } else if (minute >= 10) {
                textIcon = minute + "м";
            } else if (minute >= 1) {
                textIcon = String.format("%1$d,%2$dм", minute, (int) second / 6);
            } else {
                textIcon = second + "с";
            }

            float scale = context.getResources().getDisplayMetrics().scaledDensity;
            int height = (int) scale * 25;
            int width = (int) scale * 25;
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(height / 2);
            canvas.drawText(textIcon, 0, height * 3 / 4, paint);

            builder.setSmallIcon(Icon.createWithBitmap(bitmap));
            builder.setSubText(textIcon);
        }
    }

}
