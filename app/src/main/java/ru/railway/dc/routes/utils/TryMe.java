package ru.railway.dc.routes.utils;

/**
 * Created by SQL on 25.02.2017.
 */

public class TryMe implements Thread.UncaughtExceptionHandler {

    Thread.UncaughtExceptionHandler oldHandler;

    public TryMe() {
        oldHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        System.err.println("exception: " + ex);
        if (oldHandler != null) {
            oldHandler.uncaughtException(thread, ex);
        }
    }
}
