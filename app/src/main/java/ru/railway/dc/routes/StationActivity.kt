package ru.railway.dc.routes

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Point
import android.graphics.Region
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.widget.SimpleCursorAdapter
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import io.reactivex.Maybe
import io.reactivex.MaybeOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import it.sephiroth.android.library.tooltip.Tooltip
import ru.railway.dc.routes.database.AssetsDB
import ru.railway.dc.routes.database.assets.struct.CountryView
import ru.railway.dc.routes.request.data.RequestData
import ru.railway.dc.routes.request.model.Station
import ru.railway.dc.routes.utils.RUtils
import ru.railway.dc.routes.utils.TooltipManager
import java.util.HashMap

class StationActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    companion object {
        const val STATION_TYPE_PARAM = "station_type"
    }

    private var db: AssetsDB? = null
    private lateinit var stationType: String

    private var idIndex: Int = 0
    private var mFavourite: MutableMap<Int, Int> = HashMap()

    private lateinit var adapter: SimpleCursorAdapter
    private var isItemFirst = true
    private var isSearchExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station)


        stationType = intent.getStringExtra(STATION_TYPE_PARAM)
        initToolbar()

        // Устанавлием адаптер для списка
        val lvMain = findViewById(R.id.lvMain) as ListView
        adapter = createAdapter(this)
        lvMain.adapter = adapter
        lvMain.onItemClickListener = this

        // Инициализация loader для загрузки данных
        db = AssetsDB()
        db!!.open()
        startSearchStation()
    }

    private fun loadStationObservable(pattern: String?) =
            Maybe.create(MaybeOnSubscribe<Cursor> {
                it.onSuccess(db!!.getStationCursor(pattern))
            }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())

    private fun startSearchStation(pattern: String? = null) {
        loadStationObservable(pattern)
                .subscribe {
                    idIndex = it.getColumnIndex(CountryView.COLUMN_ID)
                    mFavourite.clear()
                    adapter.swapCursor(it)
                }
    }

    private fun prepareTooltip(isFirst: Boolean) {
        val tooltipManager = App.tooltipManager
        if (!isSearchExpanded) {
            if (!isFirst)
                tooltipManager.resetGroup(TooltipManager.STATION_GROUP)
            val p = Point(RUtils.getScreenWidth(getWindowManager()) - RUtils.convertDpToPixels(72, this).toInt(),
                    RUtils.getDimenFromAttr(android.R.attr.actionBarSize, this) / 2 + RUtils.getDimenFromRes("status_bar_height", this))
            tooltipManager.addToolTip(p, R.string.tooltip_station_search, Tooltip.Gravity.BOTTOM,
                    TooltipManager.STATION_GROUP, false, this)

            tooltipManager.show(TooltipManager.STATION_GROUP)
        }
        if (!isFirst)
            tooltipManager.resetGroup(TooltipManager.STATION_ITEM_GROUP)
        isItemFirst = true
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_station, menu)

        val actionSearchItem = menu.findItem(R.id.action_search)
        val searchView = actionSearchItem.actionView as SearchView
        searchView.setOnSearchClickListener { isSearchExpanded = true }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                isSearchExpanded = false
                actionSearchItem.collapseActionView()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                startSearchStation(newText)
                return true
            }
        })

        prepareTooltip(true)

        return super.onCreateOptionsMenu(menu)
    }

    private fun createAdapter(context: Context): SimpleCursorAdapter {
        val adapter = AdapterHelper(this).adapter
        adapter.viewBinder = SimpleCursorAdapter.ViewBinder { view, cursor, columnIndex ->
            // Если картинка
            if (view.id == R.id.imgStar) {
                // Adding tooltip for favourite button
                if (isItemFirst) {
                    val tooltipManager = App.tooltipManager
                    tooltipManager.addToolTip(view, R.string.tooltip_station_item_favourite, Tooltip.Gravity.BOTTOM,
                            TooltipManager.STATION_ITEM_GROUP, false, context)
                    tooltipManager.show(TooltipManager.STATION_ITEM_GROUP)
                    isItemFirst = false
                }

                // Получение данных
                val id = cursor.getInt(idIndex)
                val favourite: Int
                if (mFavourite.containsKey(id)) {
                    favourite = mFavourite[id]!!
                } else {
                    favourite = cursor.getInt(columnIndex)
                }
                // Обновление картинки
                val iv = view as ImageView
                if (favourite == 1) {
                    iv.setImageResource(android.R.drawable.star_big_on)
                } else {
                    iv.setImageResource(android.R.drawable.star_big_off)
                }
                view.setOnClickListener { v ->
                    var f: Int
                    if (mFavourite.containsKey(id)) {
                        f = mFavourite[id]!!
                    } else {
                        f = favourite
                    }
                    f = 1 - f
                    // Запоминаем состояние
                    db!!.setFavourite(id, f)
                    // Меняем картинку
                    mFavourite.put(id, f)
                    // Обновление картинки
                    val iv = v as ImageView
                    if (f == 1) {
                        iv.setImageResource(android.R.drawable.star_big_on)
                    } else {
                        iv.setImageResource(android.R.drawable.star_big_off)
                    }
                }
                return@ViewBinder true
            }
            false
        }
        return adapter
    }

    // ================================ TOOLBAR ====================================================

    private fun initToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        toolbar.title = when (stationType) {
            RequestData.B_STATION -> "Откуда"
            RequestData.E_STATION -> "Куда"
            else -> "Станции"
        }
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_help -> {
                prepareTooltip(false)
                return true
            }
            R.id.itemRegion ->
                startActivity(Intent(this, RegionActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    // ====================== ОБРАБОТЧИКИ НАЖАТИЙ ==================================================

    // Выбор элемента
    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {

        val tvName = view.findViewById(R.id.tvName) as TextView
        val tvRegion = view.findViewById(R.id.tvRegion) as TextView

        val name = tvName.text.toString()
        val region = tvRegion.text.toString()

        val station = Station(id, name, region)

        when (stationType) {
            RequestData.B_STATION -> {
                App.requestData.bStation = Station(0, name, region)
            }
            RequestData.E_STATION -> {
                App.requestData.eStation = Station(0, name, region)
            }
            RequestData.I_STATION -> {
                App.requestData.addStation(Station(0, name, region))
            }
        }

//        RequestDataObservable.getInstance().notification(param, station)
        finish()
    }

    override fun onDestroy() {
        App.tooltipManager.close(this)
        super.onDestroy()
        db?.close()
    }

    // ==================================== ADAPTER ================================================

    internal class AdapterHelper(private val context: Context) {

        private var layout = R.layout.adapter_station_item
        private var from = arrayOf(CountryView.COLUMN_NAME, CountryView.COLUMN_REGION, CountryView.COLUMN_FAVOURITE)
        private var to = intArrayOf(R.id.tvName, R.id.tvRegion, R.id.imgStar)

        val adapter: SimpleCursorAdapter
            get() = SimpleCursorAdapter(context, layout,
                    null, from, to, 0)
    }
}