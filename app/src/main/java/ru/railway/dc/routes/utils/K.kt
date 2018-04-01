package ru.railway.dc.routes.utils

import android.app.Activity
import android.util.Log
import ru.railway.dc.routes.BuildConfig
import java.util.*

object K {
    private val LOG_TAG = "routes"


    fun d(msg: String) {
        Log.d(LOG_TAG, msg)
    }
}

fun Calendar.log() {
    K.d("" + get(Calendar.DAY_OF_MONTH) + "-"
            + get(Calendar.MONTH) + "-"
            + get(Calendar.YEAR) + ", "
            + get(Calendar.HOUR_OF_DAY) + ":"
            + get(Calendar.MINUTE))
}