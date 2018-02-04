package ru.railway.dc.routes.event.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ru.railway.dc.routes.R;
import ru.railway.dc.routes.event.activity.data.ItemSchedule;
import ru.railway.dc.routes.event.activity.data.item.DataItem;

import static ru.railway.dc.routes.display.ScheduleAdapter.getTypeTrainResource;

/**
 * Created by SQL on 07.01.2017.
 */

public class ActivityTime extends BaseExpandableListAdapter {

    private ItemSchedule itemSchedule;
    private Context context;
//    private long time;

    public ActivityTime(ItemSchedule itemSchedule, Context context) {
        this.itemSchedule = itemSchedule;
        this.context = context;
//        this.time = Calendar.getInstance().getTimeInMillis();
    }

    @Override
    public int getGroupCount() {
        return itemSchedule.getGroupCount();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return itemSchedule.getChildrenCount(groupPosition);
    }

    @Override
    public Object getGroup(int groupPosition) {
        return itemSchedule.getGroup(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return itemSchedule.getChild(groupPosition, childPosition);
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
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup parent) {
        ViewHolderGroup holder;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.adapter_favourite_group, null);
            // Holder
            holder = new ViewHolderGroup();
            holder.tvBStation = (TextView) view.findViewById(R.id.tvBStation);
            holder.tvBTime = (TextView) view.findViewById(R.id.tvBTime);
            holder.tvETime = (TextView) view.findViewById(R.id.tvETime);
            holder.tvStationTime = (TextView) view.findViewById(R.id.tvStationTime);
            holder.tvTrainTime = (TextView) view.findViewById(R.id.tvTrainTime);
            holder.separator = (ImageView) view.findViewById(R.id.separator);
            holder.ivTypeTrain = (ImageView) view.findViewById(R.id.ivTypeTrain);
            view.setTag(holder);
        } else {
            holder = (ViewHolderGroup) view.getTag();
        }

        DataItem dataItem = (DataItem) getGroup(groupPosition);
        holder.tvBStation.setText(dataItem.getStation());
        holder.tvBTime.setText(dataItem.getbTime());
        holder.tvETime.setText(dataItem.geteTime());
        holder.tvTrainTime.setText(dataItem.getTrainTimeGroup());

        // Устанавливаем картинку поезда
        Integer typeTrainResource = getTypeTrainResource(dataItem.getTypeTrainGroup());
        if (typeTrainResource != null) {
            holder.ivTypeTrain.setImageResource(typeTrainResource);
        }

        if (groupPosition != 0) {
            // Последний элемент
            if (groupPosition == getGroupCount() - 1) {
                holder.separator.setImageResource(R.drawable.img_favourite_group_last);
                // Остальные элементы
            } else {
                holder.tvStationTime.setText("(" + dataItem.getStationTime() + ")");
                holder.separator.setImageResource(R.drawable.img_favourite_group);
            }
            // Первый элемент
        } else {
            holder.separator.setImageResource(R.drawable.img_favourite_group_first);
        }


//        itemSchedule.addEffect(time, groupPosition, view);
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        ViewHolderChild holder;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.adapter_favourite_child, null);
            // Holder
            holder = new ViewHolderChild();
            holder.tvBStation = (TextView) view.findViewById(R.id.tvBStation);
            holder.tvBTime = (TextView) view.findViewById(R.id.tvBTime);
            holder.tvETime = (TextView) view.findViewById(R.id.tvETime);
//            holder.tvStationTime = (TextView) view.findViewById(R.id.tvStationTime);
//            holder.tvTrainTime = (TextView) view.findViewById(R.id.tvTrainTime);
            view.setTag(holder);
        } else {
            holder = (ViewHolderChild) view.getTag();
        }

        DataItem dataItem = (DataItem) getChild(groupPosition, childPosition);
        holder.tvBStation.setText(dataItem.getStation());
        holder.tvBTime.setText(dataItem.getbTime());
        holder.tvETime.setText(dataItem.geteTime());
//        holder.tvStationTime.setText("(" + dataItem.getStationTime() + ")");
//        holder.tvTrainTime.setText(dataItem.getTrainTime());

//        itemSchedule.addEffect(time, groupPosition, childPosition, view);
        return view;
    }

    // Буфер для компонентов списка
    static class ViewHolderGroup {
        TextView tvBStation;
        TextView tvBTime;
        TextView tvETime;
        TextView tvStationTime;
        TextView tvTrainTime;
        ImageView separator;
        ImageView ivTypeTrain;
    }

    static class ViewHolderChild {
        TextView tvBStation;
        TextView tvBTime;
        TextView tvETime;
//        TextView tvStationTime;
//        TextView tvTrainTime;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

//    // Обновление
//    @Override
//    public boolean update(long code) {
//        this.time = code;
//        // Код выполняется для UI-потока
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                notifyDataSetChanged();
//            }
//        });
//        return false;
//    }
}
