package ru.railway.dc.routes

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar
import com.melnykov.fab.FloatingActionButton
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import it.sephiroth.android.library.tooltip.Tooltip
import ru.railway.dc.routes.database.utils.CashDetailTableUtils
import ru.railway.dc.routes.database.utils.CashTableUtils
import ru.railway.dc.routes.request.data.RequestData
import ru.railway.dc.routes.request.fragment.dialog.DateFragment
import ru.railway.dc.routes.setting.MyPreferenceFragment
import ru.railway.dc.routes.utils.DateUtils
import ru.railway.dc.routes.utils.TooltipManager
import java.util.*

class MainActivity : AppCompatActivity(), RequestData.OnChangeDataListener {

    companion object {
        private const val COUNT_MILLIS_IN_DAY = 86400000
    }

    // Компоненты
    private lateinit var holder: ViewHolder

    private var drawer: Drawer? = null
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private var isBottomSheetExpanded = false

    // =========================== ОБЪЯВЛЕНИЕ И ИНИЦИАЛИЗАЦИЯ ФРАГМЕНТОВ ===========================

    private lateinit var bDateFragment: DateFragment

    // ======================== ДЕЙСТВИЯ ПРИ ОБНОВЛЕНИИ ============================================

    override fun changeData(param: String) {
        when (param) {
            RequestData.DATETIME -> {
                val bDateTime = App.requestData.bDateTime
                val eDateTime = App.requestData.eDateTime

                val bDateTimeString = DateUtils.format(bDateTime!!, DateUtils.FORMAT_SHORT_DATE)
                val today = DateUtils.format(System.currentTimeMillis(), DateUtils.FORMAT_SHORT_DATE)
                val tomorrow = DateUtils.format(System.currentTimeMillis()
                        + COUNT_MILLIS_IN_DAY, DateUtils.FORMAT_SHORT_DATE)

                // Update date
                holder.bDate.text = bDateTimeString
                if (bDateTimeString == today) {
                    selectToday(true)
                    selectTomorrow(false)
                } else {
                    if (bDateTimeString == tomorrow) {
                        selectTomorrow(true)
                        selectToday(false)
                    } else {
                        selectToday(false)
                        selectTomorrow(false)
                    }
                }
                // Update seekBar
                holder.progress.setMinStartValue(getSeekbarValue(bDateTime).toFloat())
                        .setMaxStartValue(getSeekbarValue(eDateTime!!).toFloat())
                        .setGap(30F).apply()
                holder.startTime.text = DateUtils.format(bDateTime, DateUtils.FORMAT_TIME)
                holder.endTime.text = DateUtils.format(eDateTime, DateUtils.FORMAT_TIME)
            }
            RequestData.B_STATION -> {
                holder.bStation.text = App.requestData.bStation!!.name
            }
            RequestData.E_STATION -> {
                holder.eStation.text = App.requestData.eStation!!.name
            }
        }
    }

    // ====================== Update UI components =================================================

    private fun selectToday(isToday: Boolean) {
        if (isToday) {
            holder.today.textSize = 19f
            holder.today.setTextColor(Color.BLACK)
        } else {
            holder.today.textSize = 17f
            holder.today.setTextColor(Color.GRAY)
        }
    }

    private fun selectTomorrow(isTomorrow: Boolean) {
        if (isTomorrow) {
            holder.tomorrow.textSize = 19f
            holder.tomorrow.setTextColor(Color.BLACK)
        } else {
            holder.tomorrow.textSize = 17f
            holder.tomorrow.setTextColor(Color.GRAY)
        }
    }

    // ================ Utils for update UI Components =========================================

    private fun getSeekbarValue(calendar: Calendar) =
            calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)


    private fun convertSeekbarValue(value: Int): String {
        val minute = value % 60
        val hour = value / 60
        return String.format("%s$hour:%s$minute",
                if (hour < 10) "0" else "",
                if (minute < 10) "0" else "")
    }

// ======================== ON_CREATE ==========================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventService.clearFlag()

//        // Если activity вызвано уведомлением
//        val eventID = intent.getIntExtra(NotificationTime.PARAM_EVENT_ID, -1)
//        if (eventID != -1) {
//        } else {
//
//        }

        bDateFragment = DateFragment.newInstance(App.requestData.bDateTime!!.timeInMillis)

        setContentView(R.layout.activity_main)
        holder = ViewHolder(findViewById(android.R.id.content))
        holder.progress.setOnRangeSeekbarChangeListener { minValue, maxValue ->
            holder.startTime.text = convertSeekbarValue(minValue.toInt())
            holder.endTime.text = convertSeekbarValue(maxValue.toInt())
        }
        holder.progress.setOnRangeSeekbarFinalValueListener { minValue, maxValue ->
            App.requestData.bDateTime = DateUtils.composeHM(App.requestData.bDateTime!!,
                    DateUtils.createCalendar(convertSeekbarValue(minValue.toInt()), DateUtils.FORMAT_TIME))
            App.requestData.eDateTime = DateUtils.composeHM(App.requestData.eDateTime!!,
                    DateUtils.createCalendar(convertSeekbarValue(maxValue.toInt()), DateUtils.FORMAT_TIME))
        }
        initialsNavigationDrawer(createToolbar())
        initBottomSheet()

        App.requestData.register(this)
        // Show tooltip
        prepareToolTip(true)

        App.requestData.notificationAll()

    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.currentTime -> {
                App.requestData.bDateTime = DateUtils.composeHM(App.requestData.bDateTime!!,
                        DateUtils.nowCalendar())
            }
            R.id.bCalendarDate -> {
                bDateFragment.show(fragmentManager, "bDate")
            }
            R.id.today, R.id.tomorrow -> {
                App.requestData.bDateTime = DateUtils.composeHM(DateUtils.nowCalendar(), App.requestData.bDateTime!!).apply {
                    if (view.id == R.id.tomorrow)
                        add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            R.id.fabAdd -> {
                startActivity(Intent(this, StationActivity::class.java)
                        .putExtra(StationActivity.STATION_TYPE_PARAM, RequestData.I_STATION))
            }
            R.id.swapStation -> if (hasStations()) {
                val bStation = App.requestData.bStation!!.name
                val eStation = App.requestData.eStation!!.name
                if (bStation != eStation) {
                    val temp = App.requestData.bStation
                    App.requestData.bStation = App.requestData.eStation
                    App.requestData.eStation = temp
                }
            }
            R.id.bStation -> {
                startActivity(Intent(this, StationActivity::class.java)
                        .putExtra(StationActivity.STATION_TYPE_PARAM, RequestData.B_STATION))
            }
            R.id.eStation -> {
                startActivity(Intent(this, StationActivity::class.java)
                        .putExtra(StationActivity.STATION_TYPE_PARAM, RequestData.E_STATION))
            }
            R.id.additionalSearch, R.id.fab -> {
                if (hasStations()) {
                    App.pref.edit()
                            .putString(Constant.KEY_PREF_B_STATION, App.requestData.bStation!!.name)
                            .putString(Constant.KEY_PREF_E_STATION, App.requestData.eStation!!.name)
                            .putLong(Constant.KEY_PREF_B_DATETIME, App.requestData.bDateTime!!.timeInMillis)
                            .putLong(Constant.KEY_PREF_E_DATETIME, App.requestData.eDateTime!!.timeInMillis)
                            .putBoolean(Constant.KEY_PREF_CURRENT_TIME, App.requestData.currentDateTime)
                            .apply()
                    startActivity(Intent(this, ScheduleActivity::class.java))
                } else {
                    Toast.makeText(this, "Станция отправления или прибытия не выбраны", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_help -> {
                prepareToolTip(false)
                return false
            }
            R.id.itemClearCash ->
                MyPreferenceFragment.MyTask(this).execute(MyPreferenceFragment.TASK_CLEAR_CASH)

        }
        return super.onOptionsItemSelected(item)
    }

    private fun initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(holder.bottomSheet)
        bottomSheetBehavior!!.peekHeight = resources
                .getDimensionPixelSize(R.dimen.sheet_bottom_height)
        bottomSheetBehavior!!.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    holder.fab.animate().scaleX(0f).scaleY(0f).setDuration(300).start()
                    holder.additionalSearch.animate().alpha(0f).setDuration(300).start()
                    holder.fabAdd.animate().scaleX(0f).scaleY(0f).setDuration(300).start()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.cardView.elevation = resources
                                .getDimensionPixelSize(R.dimen.sheet_bottom_elevation_off).toFloat()
                    }
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    holder.additionalSearch.visibility = View.INVISIBLE
                    holder.fab.animate().scaleX(1f).scaleY(1f).setDuration(300).start()
                    holder.fabAdd.animate().scaleX(0f).scaleY(0f).setDuration(300).start()
                    holder.fab.visibility = View.VISIBLE
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.cardView.elevation = resources
                                .getDimensionPixelSize(R.dimen.sheet_bottom_elevation_on).toFloat()
                    }
                    isBottomSheetExpanded = false
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    holder.additionalSearch.visibility = View.VISIBLE
                    holder.additionalSearch.animate().alpha(1f).setDuration(300).start()
                    holder.fab.visibility = View.INVISIBLE
                    holder.fabAdd.animate().scaleX(1f).scaleY(1f).setDuration(300).start()

                    isBottomSheetExpanded = true
                    val tooltipManager = App.tooltipManager
                    tooltipManager.setContext(TooltipManager.MAIN_BOTTOM_SHEET_ON_ITEM_LISTENER_GROUP, this@MainActivity)
                    tooltipManager.show(TooltipManager.MAIN_BOTTOM_SHEET_ON_LISTENER_GROUP,
                            TooltipManager.MAIN_BOTTOM_SHEET_ON_ITEM_LISTENER_GROUP)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                holder.imgArrow.rotation = slideOffset * 180
            }
        })
    }

    private fun createToolbar(): Toolbar {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        return toolbar
    }

// ================================ TOOLTIP ====================================================

    private fun prepareToolTip(isFirst: Boolean) {
        val tooltipManager = App.tooltipManager

        if (!isFirst)
            tooltipManager.resetGroup(TooltipManager.MAIN_GROUP)
        tooltipManager.addToolTip(findViewById(R.id.bStation), R.string.tooltip_main_content_bstation, Tooltip.Gravity.BOTTOM,
                TooltipManager.MAIN_GROUP, true, this)
        tooltipManager.addToolTip(findViewById(R.id.eStation), R.string.tooltip_main_content_estation, Tooltip.Gravity.BOTTOM,
                TooltipManager.MAIN_GROUP, true, this)
        tooltipManager.addToolTip(findViewById(R.id.seekbar), R.string.tooltip_main_content_seekbar, Tooltip.Gravity.BOTTOM,
                TooltipManager.MAIN_GROUP, true, this)
        tooltipManager.addToolTip(findViewById(R.id.bCalendarDate), R.string.tooltip_main_content_bcalendar, Tooltip.Gravity.BOTTOM,
                TooltipManager.MAIN_GROUP, true, this)
        tooltipManager.show(TooltipManager.MAIN_GROUP)

        if (!isBottomSheetExpanded) {
            if (!isFirst) {
                tooltipManager.resetGroup(TooltipManager.MAIN_BOTTOM_SHEET_OFF_GROUP,
                        TooltipManager.MAIN_BOTTOM_SHEET_ON_LISTENER_GROUP,
                        TooltipManager.MAIN_BOTTOM_SHEET_ON_ITEM_LISTENER_GROUP)
            }
            tooltipManager.addToolTip(findViewById(R.id.fab), R.string.tooltip_main_content_search, Tooltip.Gravity.LEFT,
                    TooltipManager.MAIN_BOTTOM_SHEET_OFF_GROUP, true, this)
        } else {
            if (!isFirst)
                tooltipManager.resetGroup(TooltipManager.MAIN_BOTTOM_SHEET_ON_GROUP,
                        TooltipManager.MAIN_BOTTOM_SHEET_ON_ITEM_GROUP)
            tooltipManager.addToolTip(findViewById(R.id.additionalSearch), R.string.tooltip_main_content_search, Tooltip.Gravity.LEFT,
                    TooltipManager.MAIN_BOTTOM_SHEET_ON_GROUP, true, this)
        }
    }

// ================================ DRAWER =====================================================

    private fun initialsNavigationDrawer(toolbar: Toolbar?) {

        val accountHeader = createAccountHeader()

        drawer = DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar!!)
                .withDisplayBelowStatusBar(true)
                .withActionBarDrawerToggleAnimated(true)
                .withAccountHeader(accountHeader)
                .addDrawerItems(
                        *initialDrawerItems()
                )
                .withOnDrawerItemClickListener { _, position, _ ->
                    var intent: Intent? = null
                    when (position) {
                        3 -> intent = Intent(this@MainActivity,
                                PrefActivity::class.java)
                        1 -> intent = Intent(this@MainActivity,
                                FavouritePreviewActivity::class.java)
                    }
                    if (intent != null) {
                        startActivity(intent)
                    }
                    true
                }
                .build()
    }

    private fun initialDrawerItems(): Array<IDrawerItem<*, *>> {
        return arrayOf(PrimaryDrawerItem()
                .withName("Маршруты")
                .withIdentifier(1)
                .withIcon(R.drawable.ic_drawer_favourite)
                .withSelectedTextColor(resources.getColor(R.color.primary_text)), DividerDrawerItem(), PrimaryDrawerItem()
                .withName("Настройки")
                .withTextColor(resources.getColor(R.color.primary_text))
                .withIdentifier(2)
                .withIcon(R.drawable.ic_drawer_setting)
                .withSelectable(false))
    }

    private fun createAccountHeader(): AccountHeader {
        return AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.drawer_background)
                .withOnAccountHeaderSelectionViewClickListener { view, profile ->
                    drawer!!.closeDrawer()
                    true
                }
                .build()
    }

    override fun onBackPressed() {
        if (drawer != null && drawer!!.isDrawerOpen) {
            drawer!!.closeDrawer()
        } else {
            super.onBackPressed()
        }
    }

// ================================== VIEW_HOLDER ==============================================

    class ViewHolder(v: View) {
        // Дата
        val bCalendarDate = v.findViewById(R.id.bCalendarDate)
        val today = v.findViewById(R.id.today) as TextView
        val tomorrow = v.findViewById(R.id.tomorrow) as TextView
        val bDate = v.findViewById(R.id.bDate) as TextView
        val progress = v.findViewById(R.id.seekbar) as CrystalRangeSeekbar
        val startTime = v.findViewById(R.id.startTime) as TextView
        val endTime = v.findViewById(R.id.endTime) as TextView
        val currentTime = v.findViewById(R.id.currentTime) as ImageView
        // Станции
        val bStation = v.findViewById(R.id.bStation) as TextView
        val eStation = v.findViewById(R.id.eStation) as TextView
        val swapStation = v.findViewById(R.id.swapStation) as ImageView

        val imgArrow = v.findViewById(R.id.imgArrow) as ImageView
        val additionalSearch = v.findViewById(R.id.additionalSearch) as ImageView
        val cardView = v.findViewById(R.id.cardView) as CardView

        val bottomSheet = v.findViewById(R.id.bottom_sheet)
        val fab = v.findViewById(R.id.fab) as FloatingActionButton
        val fabAdd = v.findViewById(R.id.fabAdd) as FloatingActionButton
    }

// ================================= ОБРАБОТЧИК НАЖАТИЯ НА КНОПКИ ==============================

    private fun hasStations() =
            !resources.getString(R.string.b_station_placeholder).equals(App.requestData.bStation!!.name, true)
                    && !resources.getString(R.string.e_station_placeholder).equals(App.requestData.eStation!!.name, true)


}