package ru.railway.dc.routes.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.util.TypedValue
import android.view.WindowManager

/**
 * Created by User on 02.02.2018.
 */

object RUtils {

    fun getIdFromAttr(attr: Int, c: Context): Int {
        val typedValue = TypedValue()
        return try {
            c.theme.resolveAttribute(attr, typedValue, true)
            typedValue.resourceId
        } catch (t: Throwable) {
            0
        }
    }

    fun getDimenFromAttr(attr: Int, c: Context): Int {
        val id = getIdFromAttr(attr, c)
        return if (id == 0) 0 else c.resources.getDimensionPixelSize(id)
    }

    fun getDimenFromRes(name: String, c: Context): Int {
        val resourceId = c.resources.getIdentifier(name, "dimen", "android")
        return if (resourceId > 0) c.resources.getDimensionPixelSize(resourceId) else 0
    }

    fun getScreenSize(windowManager: WindowManager): Point {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        return size
    }

    fun getScreenWidth(windowManager: WindowManager): Int {
        return getScreenSize(windowManager).x
    }

    fun getScreenHeight(windowManager: WindowManager): Int {
        return getScreenSize(windowManager).y
    }

    fun convertDpToPixels(value: Int, c: Context): Float {
        return c.resources.displayMetrics.density * value
    }

}