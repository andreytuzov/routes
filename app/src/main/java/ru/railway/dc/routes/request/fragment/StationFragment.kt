package ru.railway.dc.routes.request.fragment

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.*
import android.widget.EditText
import android.widget.TextView
import it.sephiroth.android.library.tooltip.Tooltip
import ru.railway.dc.routes.App
import ru.railway.dc.routes.R
import ru.railway.dc.routes.request.data.RequestData
import ru.railway.dc.routes.utils.RUtils
import ru.railway.dc.routes.utils.TooltipManager

class StationFragment : BottomSheetDialogFragment(), RequestData.OnChangeDataListener {

    companion object {
        private const val RV_MAIN_INDENT_21 = 10
        private const val RV_MAIN_INDENT = 20
    }

    private var rvMain: RecyclerView? = null
    private var adapter: RecyclerStationAdapter? = null
    private var tvInfo: TextView? = null
    private var fabAdd: View? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val v = inflater!!.inflate(R.layout.activity_main_station, container, false)

        tvInfo = v.findViewById(R.id.tvStatusBar) as TextView
        rvMain = v.findViewById(R.id.rvMain) as RecyclerView
        fabAdd = v.findViewById(R.id.fabAdd) as View

        prepareRvMain()
        initSwipe()
        loadData()

        return v
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_help -> {
                prepareToolTips()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun prepareToolTips() {
        val tooltipManager = App.tooltipManager

        tooltipManager.addToolTip(tvInfo!!, R.string.tooltip_main_content_statusbar, Tooltip.Gravity.TOP,
                TooltipManager.MAIN_BOTTOM_SHEET_OFF_GROUP, false, getActivity())

        // Adding tip for add button
        tooltipManager.addToolTip(fabAdd!!, R.string.tooltip_main_bottom_sheet_on_fabAdd, Tooltip.Gravity.TOP,
                TooltipManager.MAIN_BOTTOM_SHEET_ON_GROUP, false, getActivity())
        tooltipManager.addToolTip(fabAdd!!, R.string.tooltip_main_bottom_sheet_on_fabAdd, Tooltip.Gravity.TOP,
                TooltipManager.MAIN_BOTTOM_SHEET_ON_LISTENER_GROUP, false, getActivity())

        tooltipManager.show(TooltipManager.MAIN_BOTTOM_SHEET_ON_GROUP,
                TooltipManager.MAIN_BOTTOM_SHEET_OFF_GROUP)

        if (adapter!!.itemCount != 0) {
            adapter!!.resetTooltips()
            adapter!!.notifyDataSetChanged()
            tooltipManager.setContext(TooltipManager.MAIN_BOTTOM_SHEET_ON_ITEM_GROUP, getActivity())
            tooltipManager.show(TooltipManager.MAIN_BOTTOM_SHEET_ON_ITEM_GROUP)
        }
    }

    private fun prepareRvMain() {
        var height = (RUtils.getScreenHeight(activity.getWindowManager())
                - RUtils.getDimenFromAttr(android.R.attr.actionBarSize, getActivity())
                - resources.getDimensionPixelSize(R.dimen.sheet_bottom_height)
                - resources.getDimensionPixelSize(R.dimen.main_content_height)
                - RUtils.getDimenFromRes("status_bar_height", getActivity()))
        height -= if (Build.VERSION.SDK_INT >= 21) {
            RUtils.convertDpToPixels(RV_MAIN_INDENT_21, activity).toInt()
        } else {
            RUtils.convertDpToPixels(RV_MAIN_INDENT, activity).toInt()
        }

        // Set height of content for bottom sheet
        rvMain!!.minimumHeight = height

        val lp = rvMain!!.layoutParams
        lp.height = height
        rvMain!!.layoutParams = lp

        val layout = LinearLayoutManager(getContext())
        rvMain!!.layoutManager = layout
    }

    private fun initSwipe() {
        val simpleCallback = object : ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                adapter!!.remove(position)
                setDetailInfo(adapter!!.itemCount)
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(rvMain)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        App.requestData.register(this)
        prepareToolTips()
    }

    // ========================== ОБРАБОТКА ИЗМЕНЕНИЙ ==============================================

    override fun changeData(param: String) {
        when (param) {
            RequestData.I_STATION -> loadData()
        }
    }

    // TODO некорректная работа при выборе станции Степянка (проблема возможно в получении Context)
    private fun loadData() {

        val map = App.requestData.stationMap

        if (!map.isEmpty()) {
            val tooltipManager = App.tooltipManager
            tooltipManager.setContext(TooltipManager.MAIN_BOTTOM_SHEET_ON_ITEM_GROUP, getActivity())
            tooltipManager.show(TooltipManager.MAIN_BOTTOM_SHEET_ON_ITEM_GROUP)
        }
        adapter = RecyclerStationAdapter(map, onItemClickListener)

        // TODO освободить предыдущий адаптер
        rvMain!!.adapter = adapter
        setDetailInfo(adapter!!.itemCount)
    }

    private var onItemClickListener = RecyclerStationAdapter.OnItemClickListener { position ->
        val adb = AlertDialog.Builder(context)
                .setView(R.layout.dialog_stationtime)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Ок") { dialog, _ ->
                    val dialog2 = Dialog::class.java.cast(dialog)
                    val text = (dialog2.findViewById(R.id.etStationTime) as TextView)
                            .text.toString()
                    adapter!!.changeStationTime(position, Integer.valueOf(text)!!)
                }
                .setTitle("Время на станции")
                .create()
        adb.show()
        adb.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        val etStationTime = adb.window!!.findViewById(R.id.etStationTime) as EditText
        etStationTime.setText(adapter!!.getTime(position).toString())
        etStationTime.selectAll()
    }

    private fun setDetailInfo(countItem: Int) {
        if (countItem == 0) {
            tvInfo!!.text = "Станций нет"
            return
        }
        val p = countItem % 10
        var postfix = "и"
        if (countItem in 5..20 || p == 0 || p >= 5) {
            postfix = "й"
        } else if (p == 1) {
            postfix = "я"
        }
        tvInfo!!.text = countItem.toString() + " станци" + postfix
    }

}