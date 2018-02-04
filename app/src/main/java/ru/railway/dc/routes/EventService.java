package ru.railway.dc.routes;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import org.apache.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

import ru.railway.dc.routes.event.ManagerEvent;
import ru.railway.dc.routes.event.ManagerEventUtils;

/**
 * Created by SQL on 31.12.2016.
 */

// TODO автоматическое удаление сервиса
public class EventService extends Service {

    public static final Logger logger = Logger.getLogger(EventService.class);

    public Timer timer;
    private static int flag = 1;

    public static void clearFlag() {
        flag = 0;
    }

    // ============================ РАБОТА С ТАЙМЕРОМ ==============================================

    public void startTimer() {
        logger.info("service startTimer");
        stopTimer();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        int period = Integer.valueOf(sp.getString(getString(R.string.pref_name_period),
                getString(R.string.pref_value_period))) * 1000;
        // Установка начального значения времени
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Если событие завершилось, то останавливаем таймер и сервис
                if (ManagerEvent.isEmpty()) {
                    stopTimer();
                    stopSelf();
                    if (flag == START_FLAG_REDELIVERY) {
                        Process.killProcess(Process.myPid());
                    }
                } else {
                    ManagerEvent.update();
                }
            }
        }, 0, period);
    }

    public void stopTimer() {
        logger.info("service stopTimer");
        // Остановка таймера
        if (timer != null) {
            timer.cancel();
        }
        timer = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        logger.info("Service create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.info("Service start");
        ManagerEvent.removeAll();
        ManagerEventUtils.addNotificationAll(this);
        startTimer();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
        logger.info("Service destroy");
    }
}
