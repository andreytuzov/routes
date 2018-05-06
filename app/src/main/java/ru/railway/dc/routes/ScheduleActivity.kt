package ru.railway.dc.routes

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ExpandableListView
import com.kennyc.bottomsheet.BottomSheet
import com.kennyc.bottomsheet.BottomSheetListener
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.railway.dc.routes.database.utils.EventTableUtils
import ru.railway.dc.routes.display.ScheduleAdapter
import ru.railway.dc.routes.display.sort.TypeSortEnum
import ru.railway.dc.routes.search.core.IndexSearch
import ru.railway.dc.routes.search.model.Route
import ru.railway.dc.routes.search.model.Schedule
import ru.railway.dc.routes.search.parse.ManagerParseRailway
import ru.railway.dc.routes.tools.AppUtils
import ru.railway.dc.routes.utils.ToastUtils
import java.text.SimpleDateFormat
import java.util.*

class ScheduleActivity : AppCompatActivity() {

    private var adapter: ScheduleAdapter? = null
    private lateinit var elvMain: ExpandableListView
    private lateinit var schedule: Schedule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_load)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        toolbar.setTitle(R.string.search_title)
        setSupportActionBar(toolbar)

        startSearchSchedule()
    }

    private fun startSearchSchedule() {
        // Start loading
        loadScheduleObservable()
                .subscribe({ it ->
                    setContentView(R.layout.activity_schedule)

                    schedule = it

                    setSupportActionBar(findViewById(R.id.toolbar) as Toolbar)
                    supportActionBar!!.title = formatString(it.size())
                    supportActionBar!!.subtitle = "Запрос: " + SimpleDateFormat(Route.DATE_TIME_FORMAT)
                            .format(Calendar.getInstance().time)

                    elvMain = findViewById(R.id.elvMain) as ExpandableListView
                    elvMain.divider = null
                    // Если промежуточных станций нет
                    if (it.get(0).size() == 1) {
                        elvMain.setGroupIndicator(null)
                    }
                    adapter = ScheduleAdapter(this, it, ScheduleAdapter.OnHeaderLongClickListener { position -> showContextMenu(position) })
                    elvMain.setAdapter(adapter)
                }, {}, {
                    if (!AppUtils.hasConnection())
                        ToastUtils.show(this, R.string.connection_msg_not_found)
                    supportActionBar!!.setTitle(R.string.not_found_title)
                    findViewById<View>(R.id.btnRepeatSearch).visibility = View.VISIBLE
                    findViewById<View>(R.id.searchLayer).visibility = View.GONE
                })
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.btnRepeatSearch -> {
                startSearchSchedule()
                view.visibility = View.GONE
                findViewById<View>(R.id.searchLayer).visibility = View.VISIBLE
            }
        }
    }

    // ================================= Async Operations ==========================================

    private fun saveRoutesObservable(id: Int) =
            Single.create(SingleOnSubscribe<Boolean> {
                val listRoute = schedule.get(id)
                //  Save data in the favourite table
                val eDateTime = listRoute.get(listRoute.size() - 1).eTime.timeInMillis
                val isSuccess = EventTableUtils.saveDataWithLoadDetail(listRoute, false, false,
                        eDateTime)
                it.onSuccess(isSuccess)
            }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())


    private fun loadScheduleObservable() =
            Maybe.create(MaybeOnSubscribe<Schedule> {
                // Get data from the internet
                val listRoute = ManagerParseRailway().getListRoute()
                // Create a schedule
                val indexSearch = IndexSearch(listRoute)
                val schedule = indexSearch.getSchedule()
                if (schedule != null && schedule.size() != 0)
                    it.onSuccess(schedule)
                else
                    it.onComplete()
            }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())


    // ================================= MENU ======================================================

    private fun showContextMenu(id: Int) {
        BottomSheet.Builder(this, R.style.MyBottomSheetStyle)
                .setSheet(R.menu.activity_schedule_context)
                .setListener(object : BottomSheetListener {
                    override fun onSheetShown(bottomSheet: BottomSheet) {}

                    // Обработка нажатия кнопки
                    override fun onSheetItemSelected(bottomSheet: BottomSheet, menuItem: MenuItem) {
                        when (menuItem.itemId) {
                            R.id.itemAddFavourite ->
                                // Save routes in favourite table
                                saveRoutesObservable(id).subscribe { isSuccess ->
                                    ToastUtils.show(this@ScheduleActivity,
                                            if (isSuccess!!)
                                                R.string.schedule_msg_save_success
                                            else if (!AppUtils.hasConnection())
                                                R.string.connection_msg_not_found
                                            else
                                                R.string.schedule_msg_save_error)
                                }
                            R.id.itemShare -> {
                                val i = Intent(Intent.ACTION_SEND)
                                        .setType("text/plain")
                                        .putExtra(Intent.EXTRA_TEXT, schedule.get(id).toString())
                                startActivity(Intent.createChooser(i, resources.getString(R.string.share)))
                            }
                        }

                    }

                    override fun onSheetDismissed(bottomSheet: BottomSheet, i: Int) {}
                })
                .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (adapter != null) {
            menuInflater.inflate(R.menu.activity_schedule, menu)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            return true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        if (adapter != null) {
            val typeSortEnum = when (item.itemId) {
                R.id.itemSortDepartureTime -> TypeSortEnum.DEPARTURE_TIME
                R.id.itemSortArrivalTime -> TypeSortEnum.ARRIVAL_TIME
                R.id.itemSortTotalTime -> TypeSortEnum.TOTAL_TIME
                R.id.itemSortStationTime -> TypeSortEnum.STATION_TIME
                R.id.itemSortTrainTime -> TypeSortEnum.TRAIN_TIME
                else -> null
            }
            if (typeSortEnum != null) {
                adapter!!.sort(typeSortEnum)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Формат вывода подзаголовка
    private fun formatString(n: Int) = when {
        n % 10 == 1 -> "Найден $n вариант"
        n % 10 in 2..4 -> "Найдено $n варианта"
        n in 5..20 -> "Найдено $n вариантов"
        else -> "Найдено $n вариантов"
    }
}