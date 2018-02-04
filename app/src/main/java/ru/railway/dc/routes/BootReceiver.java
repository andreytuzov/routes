package ru.railway.dc.routes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.railway.dc.routes.tools.AppUtils;

/**
 * Created by SQL on 05.01.2017.
 */

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AppUtils.configure(context);
        AppUtils.startEventService();
    }
}
