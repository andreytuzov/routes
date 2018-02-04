package ru.railway.dc.routes.request.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Map;

import it.sephiroth.android.library.tooltip.Tooltip;
import ru.railway.dc.routes.App;
import ru.railway.dc.routes.R;
import ru.railway.dc.routes.request.data.RequestDataSingleton;
import ru.railway.dc.routes.request.model.Station;
import ru.railway.dc.routes.utils.RUtils;
import ru.railway.dc.routes.utils.TooltipManager;

/**
 * Created by SQL on 13.12.2016.
 */

public class StationFragment extends BottomSheetDialogFragment
        implements RequestDataSingleton.OnChangeDataListener {

    private RecyclerView rvMain;
    private RecyclerStationAdapter adapter;
    private TextView tvInfo;
    private View fabAdd;

    private final int RV_MAIN_INDENT = 10;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.activity_main_station, container, false);

        tvInfo = (TextView) v.findViewById(R.id.tvStatusBar);
        rvMain = (RecyclerView) v.findViewById(R.id.rvMain);
        fabAdd = (View) v.findViewById(R.id.fabAdd);

        prepareRvMain();
        initSwipe();
        loadData();

        return v;
    }

    private void prepareToolTips() {
        TooltipManager tooltipManager = App.Companion.getTooltipManager();

        tooltipManager.addToolTip(tvInfo, R.string.tooltip_main_content_statusbar, Tooltip.Gravity.TOP,
                TooltipManager.MAIN_CONTENT_GROUP, false, getActivity());
        tooltipManager.addToolTip(fabAdd, R.string.tooltip_main_bottom_sheet_on_fabAdd, Tooltip.Gravity.TOP,
                TooltipManager.MAIN_BOTTOM_SHEET_ON_GROUP, false, getActivity());
    }

    private void prepareRvMain() {
        int height = RUtils.INSTANCE.getScreenHeight(getActivity().getWindowManager())
                - RUtils.INSTANCE.getDimenFromAttr(android.R.attr.actionBarSize, getActivity())
                - getResources().getDimensionPixelSize(R.dimen.sheet_bottom_height)
                - getResources().getDimensionPixelSize(R.dimen.main_content_height)
                - RUtils.INSTANCE.getDimenFromRes("status_bar_height", getActivity())
                - (int) RUtils.INSTANCE.convertDpToPixels(RV_MAIN_INDENT, getActivity());

        // Set height of content for bottom sheet
        rvMain.setMinimumHeight(height);

        ViewGroup.LayoutParams lp = rvMain.getLayoutParams();
        lp.height = height;
        rvMain.setLayoutParams(lp);

        LinearLayoutManager layout = new LinearLayoutManager(getContext());
        rvMain.setLayoutManager(layout);
    }

    private void initSwipe() {
        ItemTouchHelper.Callback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                adapter.remove(position);
                setDetailInfo(adapter.getItemCount());
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvMain);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RequestDataSingleton.getInstance().register(this);
        prepareToolTips();
    }

    // ========================== ОБРАБОТКА ИЗМЕНЕНИЙ ==============================================

    @Override
    public void changeData(String param, RequestDataSingleton rds) {
        if (param.equals(RequestDataSingleton.Param.I_STATION)) {
            loadData();
        }
    }

    // TODO некорректная работа при выборе станции Степянка (проблема возможно в получении Context)
    private void loadData() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        Map<Station, Integer> map = (Map<Station, Integer>) RequestDataSingleton.getInstance()
                .findDataByName(RequestDataSingleton.Param.I_STATION_DURATION);

        if (!map.isEmpty()) {
            TooltipManager tooltipManager = App.Companion.getTooltipManager();
            tooltipManager.setContext(TooltipManager.MAIN_BOTTOM_SHEET_ON_ITEM_TIME_GROUP, getActivity());
            tooltipManager.show(TooltipManager.MAIN_BOTTOM_SHEET_ON_ITEM_TIME_GROUP);
        }
        adapter = new RecyclerStationAdapter(map, onItemClickListener);

        // TODO освободить предыдущий адаптер
        rvMain.setAdapter(adapter);
        setDetailInfo(adapter.getItemCount());
    }

    RecyclerStationAdapter.OnItemClickListener onItemClickListener = new RecyclerStationAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(final int position) {
            AlertDialog adb = new AlertDialog.Builder(getContext())
                    .setView(R.layout.dialog_stationtime)
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Dialog dialog2 = Dialog.class.cast(dialog);
                            String text = ((TextView) dialog2.findViewById(R.id.etStationTime))
                                    .getText().toString();
                            adapter.changeStationTime(position, Integer.valueOf(text));
                        }
                    })
                    .setTitle("Время на станции")
                    .create();
            adb.show();
            adb.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            EditText etStationTime = (EditText) adb.getWindow().findViewById(R.id.etStationTime);
            etStationTime.setText(String.valueOf(adapter.getTime(position)));
            etStationTime.selectAll();
        }
    };


    private void setDetailInfo(int countItem) {
        if (countItem == 0) {
            tvInfo.setText("Станций нет");
            return;
        }
        int p = countItem % 10;
        String postfix = "и";
        if ((countItem >= 5 && countItem <= 20) || p == 0 || p >= 5) {
            postfix = "й";
        } else if (p == 1) {
            postfix = "я";
        }
        tvInfo.setText(countItem + " станци" + postfix);
    }


}
