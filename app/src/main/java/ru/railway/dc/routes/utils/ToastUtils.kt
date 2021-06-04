package ru.railway.dc.routes.utils

import android.app.Activity
import android.widget.Toast
import ru.railway.dc.routes.App

object ToastUtils {

    private var mIsActive = false
    private var mIsQueue = false
    private var mMessage: String? = null

    fun show(activity: Activity, messageId: Int, duration: Int = Toast.LENGTH_SHORT) {
        show(activity, activity.resources.getString(messageId), duration)
    }

    fun show(activity: Activity, message: String, duration: Int = Toast.LENGTH_SHORT) {
        if ((mIsActive && mMessage == message) || mIsQueue)
            return

        if (mIsActive)
            mIsQueue = true
        else
            mIsActive = true
        mMessage = message

        Toast.makeText(activity, message, duration).show()

        App.handler.postDelayed({
            mIsActive = mIsQueue
            mIsQueue = false
        }, duration.toLong())
    }

}