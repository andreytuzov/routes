package ru.railway.dc.routes.tools;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.apache.log4j.Logger;

import ru.railway.dc.routes.EventService;

/**
 * Created by SQL on 13.01.2017.
 */

public class AppUtils {

    public static final Logger logger = Logger.getLogger(AppUtils.class);

    private static Context context;

    public static void configure(Context cntx) {
        context = cntx;
    }

    // Проверка наличия соединения
    public static boolean hasConnection() {
        if (context == null) {
            logger.debug("AppUtils is not configure");
        }
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public static void startEventService() {
        if (!isEventServiceRunning()) {
            logger.debug("startEventService");
            context.startService(new Intent(context, EventService.class));
        }
    };

    public static void stopEventService() {
        if (isEventServiceRunning()) {
            context.stopService(new Intent(context, EventService.class));
        }
    };

    private static boolean isEventServiceRunning() {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (EventService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
