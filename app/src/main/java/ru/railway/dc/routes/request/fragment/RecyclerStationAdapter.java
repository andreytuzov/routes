package ru.railway.dc.routes.request.fragment;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.sephiroth.android.library.tooltip.Tooltip;
import ru.railway.dc.routes.App;
import ru.railway.dc.routes.R;
import ru.railway.dc.routes.request.model.Station;
import ru.railway.dc.routes.utils.TooltipManager;

public class RecyclerStationAdapter
        extends RecyclerView.Adapter<RecyclerStationAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener listener;

    static final String ITEM_STATION = "station";
    static final String ITEM_DURATION = "duration";
    static final String ITEM_REGION = "region";

    private List<Station> stations;
    private Map<Station, Integer> mapDuration;
    private boolean isFirst = true;

    public RecyclerStationAdapter(Map<Station, Integer> map, OnItemClickListener listener) {
        this.mapDuration = map;
        this.listener = listener;
        stations = new ArrayList<>();

        for (Station station : map.keySet()) {
            stations.add(station);
        }

    }

    public void resetTooltips() {
        isFirst = true;
    }

    public Integer getTime(int position) {
        Station station = stations.get(position);
        return mapDuration.get(station);
    }

    public void changeStationTime(int position, int stationTime) {
        Station station = stations.get(position);
        mapDuration.put(station, stationTime);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        Log.d("", "position = " + position);
        Station station = stations.get(position);
        mapDuration.remove(station);
        stations.remove(position);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvStation;
        public TextView tvDuration;
        public TextView tvRegion;

        public ViewHolder(View v) {
            super(v);
            tvStation = (TextView) v.findViewById(R.id.tvStation);
            tvDuration = (TextView) v.findViewById(R.id.tvDuration);
            tvRegion = (TextView) v.findViewById(R.id.tvRegion);
        }
    }

    @Override
    public RecyclerStationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_main_item,
                parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerStationAdapter.ViewHolder holder, final int position) {
        Station station = stations.get(position);
        int duration = mapDuration.get(station);
        holder.tvStation.setText(station.getName());

        holder.tvDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(position);
            }
        });
        if (isFirst) {
            TooltipManager tooltipManager = App.Companion.getTooltipManager();
            tooltipManager.addToolTip(holder.tvDuration, R.string.main_bottom_sheet_on_item_time, Tooltip.Gravity.BOTTOM,
                    TooltipManager.MAIN_BOTTOM_SHEET_ON_ITEM_GROUP, false, null);
            tooltipManager.addToolTip(holder.tvDuration, R.string.main_bottom_sheet_on_item_time, Tooltip.Gravity.BOTTOM,
                    TooltipManager.MAIN_BOTTOM_SHEET_ON_ITEM_LISTENER_GROUP, false, null);
            isFirst = false;
        }
        holder.tvDuration.setText(String.valueOf(duration));
        holder.tvRegion.setText(station.getRegion());
    }

    @Override
    public int getItemCount() {
        return stations.size();
    }
}
