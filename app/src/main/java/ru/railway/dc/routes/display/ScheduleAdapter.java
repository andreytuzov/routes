package ru.railway.dc.routes.display;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ru.railway.dc.routes.R;
import ru.railway.dc.routes.display.model.ListRouteView;
import ru.railway.dc.routes.display.model.RouteView;
import ru.railway.dc.routes.display.model.ScheduleView;
import ru.railway.dc.routes.display.sort.ScheduleSort;
import ru.railway.dc.routes.display.sort.TypeSortEnum;
import ru.railway.dc.routes.search.model.Route;
import ru.railway.dc.routes.search.model.Schedule;
import ru.railway.dc.routes.utils.Utils;

/**
 * Created by SQL on 02.02.2017.
 */

public class ScheduleAdapter extends BaseExpandableListAdapter {

    public interface OnHeaderLongClickListener {
        void onHeaderSelected(int position);
    }

    private OnHeaderLongClickListener listener;

    private ScheduleView scheduleView;
    private LayoutInflater inflater;
    private Drawable groupBackground;

    public ScheduleAdapter(Context context, Schedule schedule, OnHeaderLongClickListener listener) {
        this.scheduleView = new ScheduleView(schedule);
        this.listener = listener;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void sort(TypeSortEnum typeSortEnum) {
        scheduleView = ScheduleSort.sort(scheduleView, typeSortEnum);
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return scheduleView.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        int size = scheduleView.getListRouteView(groupPosition).size();
        if (size == 1) {
            size = 0;
        }
        return size;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return scheduleView.getListRouteView(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return scheduleView.getListRouteView(groupPosition).getRouteView(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, View view, ViewGroup parent) {
        ViewHolderGroup holder;
        if (view == null) {
            view = inflater.inflate(R.layout.adapter_schedule_group, null);
            view.setBackground(groupBackground);
            holder = new ViewHolderGroup();
            holder.tvID = (TextView) view.findViewById(R.id.tvID);
            holder.tvBStation = (TextView) view.findViewById(R.id.tvBStation);
            holder.tvEStation = (TextView) view.findViewById(R.id.tvEStation);
            holder.tvBTime = (TextView) view.findViewById(R.id.tvBTime);
            holder.tvETime = (TextView) view.findViewById(R.id.tvETime);
            holder.tvTotalTime = (TextView) view.findViewById(R.id.tvTotalTime);
            holder.ivMenu = (ImageView) view.findViewById(R.id.ivMenu);
            view.setTag(holder);
        } else {
            holder = (ViewHolderGroup) view.getTag();
        }

        ListRouteView item = (ListRouteView) getGroup(groupPosition);
        RouteView externalRoute = item.getExternRoute();

        final int id = item.getId();
        holder.tvBStation.setText(Utils.getShortStation(externalRoute.getBStation()));
        holder.tvEStation.setText(Utils.getShortStation(externalRoute.getEStation()));
        holder.tvBTime.setText(externalRoute.getBTimeString(Route.TIME_FORMAT));
        holder.tvETime.setText(externalRoute.getETimeString(Route.TIME_FORMAT));
        holder.tvTotalTime.setText(Utils.getTextTime(externalRoute.getTotalTime()));
        holder.ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onHeaderSelected(id);
            }
        });

        return view;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        ViewHolderChild holder;
        if (view == null) {
            view = inflater.inflate(R.layout.adapter_schedule_child, null);
            holder = new ViewHolderChild();
            holder.tvBStation = (TextView) view.findViewById(R.id.tvBStation);
            holder.tvBTime = (TextView) view.findViewById(R.id.tvBTime);
            holder.tvETime = (TextView) view.findViewById(R.id.tvETime);
            holder.tvBDate = (TextView) view.findViewById(R.id.tvBDate);
            holder.tvEDate = (TextView) view.findViewById(R.id.tvEDate);
            holder.tvStationTime = (TextView) view.findViewById(R.id.tvStationTime);
            holder.tvTrainTime = (TextView) view.findViewById(R.id.tvTrainTime);
            holder.separator = (ImageView) view.findViewById(R.id.separator);
            holder.ivTypeTrain = (ImageView) view.findViewById(R.id.ivTypeTrain);

            view.setTag(holder);
        } else {
            holder = (ViewHolderChild) view.getTag();
        }

        RouteView routeView = (RouteView) getChild(groupPosition, childPosition);
        holder.tvBStation.setText(routeView.getEStation());
        holder.tvTrainTime.setText(Utils.getTextTime(routeView.getTrainTime()));
        holder.tvBTime.setText(routeView.getETimeString(Route.TIME_FORMAT));

        // Картинка типа поезда
        Integer typeTrainResource = getTypeTrainResource(routeView.getTypeTrain());
        holder.ivTypeTrain.setImageResource(typeTrainResource);

        // Для всех кроме последнего
        if (!isLastChild) {
            holder.tvETime.setText(scheduleView.getListRouteView(groupPosition)
                    .getRouteView(childPosition + 1).getBTimeString(Route.TIME_FORMAT));
            holder.tvStationTime.setText("(" + Utils.
                    getTextTime(routeView.getStationTime()) + ")");
            holder.separator.setImageResource(R.drawable.img_schedule_child);
        } else {
            holder.tvETime.setText("");
            holder.tvStationTime.setText("");
            holder.separator.setImageResource(R.drawable.img_schedule_child_last);
        }
        return view;
    }

    public static Integer getTypeTrainResource(String typeTrain) {
        Integer resource = null;
        Log.d("typeTrain", "typeTrain = " + typeTrain);
        if (typeTrain != null) {
            switch (typeTrain) {
                case "regional_economy":
                    resource = R.drawable.train_type_regional_economy;
                    break;
                case "city":
                    resource = R.drawable.train_type_city;
                    break;
                case "regional_business":
                    resource = R.drawable.train_type_regional_business;
                    break;
                case "interregional_economy":
                    resource = R.drawable.train_type_interregional_economy;
                    break;
                case "interregional_business":
                    resource = R.drawable.train_type_interregional_business;
                    break;
                case "international":
                    resource = R.drawable.train_type_international;
                    break;
                case "commercial":
                    resource = R.drawable.train_type_commercial;
                    break;
                case "airport":
                    resource = R.drawable.train_type_airport;
                    break;
            }
        } else {
            resource = R.drawable.train_type_empty;
        }
        return resource;
    }

    // Буфер для компонентов списка
    static class ViewHolderGroup {
        TextView tvID;
        TextView tvBStation;
        TextView tvEStation;
        TextView tvBTime;
        TextView tvETime;
        TextView tvTotalTime;
        ImageView ivMenu;
    }

    static class ViewHolderChild {
        TextView tvBStation;
        TextView tvBTime;
        TextView tvETime;
        TextView tvBDate;
        TextView tvEDate;
        TextView tvStationTime;
        TextView tvTrainTime;
        ImageView separator;
        ImageView ivTypeTrain;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
