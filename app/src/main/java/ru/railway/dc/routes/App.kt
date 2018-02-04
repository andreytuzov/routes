package ru.railway.dc.routes

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import org.apache.log4j.BasicConfigurator
import ru.railway.dc.routes.database.AssetsDB
import ru.railway.dc.routes.database.DB
import ru.railway.dc.routes.tools.AppUtils
import ru.railway.dc.routes.utils.TooltipManager
import ru.railway.dc.routes.utils.TryMe

class App : Application() {


    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(TryMe())
        super.onCreate()
        BasicConfigurator.configure()
        AppUtils.configure(baseContext)
        AppUtils.startEventService()
        DB.configure(baseContext)
        AssetsDB.configure(baseContext)

        pref = getSharedPreferences(FILE_PREF, Context.MODE_PRIVATE)
        tooltipManager = TooltipManager()

        // CashTableUtils.clearPrevData();
        // CashDetailTableUtils.clearPrevData();
    }

    companion object {
        const val FILE_PREF = "main_pref"

        lateinit var pref: SharedPreferences
            private set
        lateinit var tooltipManager: TooltipManager
            private set
    }
}