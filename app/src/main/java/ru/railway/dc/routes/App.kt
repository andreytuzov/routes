package ru.railway.dc.routes

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import com.facebook.drawee.backends.pipeline.Fresco
import org.apache.log4j.BasicConfigurator
import ru.railway.dc.routes.database.assets.search.AssetsDB
import ru.railway.dc.routes.database.DB
import ru.railway.dc.routes.database.assets.photos.AssetsPhotoDB
import ru.railway.dc.routes.request.data.RequestData
import ru.railway.dc.routes.tools.AppUtils
import ru.railway.dc.routes.utils.TooltipManager
import ru.railway.dc.routes.utils.TryMe

class App : Application() {


    override fun onCreate() {
        super.onCreate()
        instance = this
        handler = Handler()
        Thread.setDefaultUncaughtExceptionHandler(TryMe())
        super.onCreate()
        BasicConfigurator.configure()
        AppUtils.configure(baseContext)
        AppUtils.startEventService()
        DB.configure(baseContext)
        AssetsDB.configure(baseContext)
        AssetsPhotoDB.configure(baseContext)
        Fresco.initialize(this)

        pref = getSharedPreferences(FILE_PREF, Context.MODE_PRIVATE)
        requestData = RequestData.newInstance(this)
        tooltipManager = TooltipManager()
    }

    companion object {
        const val FILE_PREF = "main_pref"

        lateinit var pref: SharedPreferences
            private set
        lateinit var requestData: RequestData
            private set
        lateinit var tooltipManager: TooltipManager
            private set
        lateinit var instance: App
            private set
        lateinit var handler: Handler
            private set
    }
}