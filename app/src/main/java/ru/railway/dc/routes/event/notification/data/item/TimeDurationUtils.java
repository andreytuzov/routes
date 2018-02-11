package ru.railway.dc.routes.event.notification.data.item;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.RequiresApi;

public class TimeDurationUtils {

    public static String getTextFromTime(int timeInSecond) {
        int second = timeInSecond % 60;
        int hour = timeInSecond / 3600;
        int minute = (timeInSecond - hour * 3600 - second) / 60;

        String textTime = "";
        if (hour >= 10) {
            textTime = hour + "ч";
        } else if (hour >= 1) {
            textTime = String.format("%1$d,%2$dч", hour, (int) minute / 6);
        } else if (minute >= 10) {
            textTime = minute + "м";
        } else if (minute >= 1) {
            textTime = String.format("%1$d,%2$dм", minute, (int) second / 6);
        } else {
            textTime = second + "с";
        }
        return textTime;
    }

    @RequiresApi(Build.VERSION_CODES.M)
    public static Icon getIconFromText(Context context, String textIcon) {
        float scale = context.getResources().getDisplayMetrics().scaledDensity;
        int height = (int) scale * 25;
        int width = height;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(height / 2);
        canvas.drawText(textIcon, 0, height * 3 / 4, paint);

        return Icon.createWithBitmap(bitmap);
    }
}
