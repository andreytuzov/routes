package ru.railway.dc.routes.event.activity.data;

import android.graphics.Color;
import android.view.View;

import java.util.List;

import ru.railway.dc.routes.event.activity.data.item.DataItem;

/**
 * Created by SQL on 07.01.2017.
 */

public class ItemSchedule {

    private List<List<ItemTime>> data;

    public ItemSchedule(List<List<ItemTime>> data) {
        this.data = data;
    }

    public DataItem getGroup(int groupPosition) {
        return data.get(groupPosition).get(0).dataItem;
    }

    public DataItem getChild(int groupPosition, int childPosition) {
        return data.get(groupPosition).get(childPosition + 1).dataItem;
    }

    public void addEffect(long time, int groupPosition, int childPosition, View view) {
        addEffect(time, data.get(groupPosition).get(childPosition + 1), view);
    }

    public void addEffect(long time, int groupPosition, View view) {
        addEffect(time, data.get(groupPosition).get(0), view);
    }

    private void addEffect(long time, ItemTime itemTime, View view) {
        if (time > itemTime.eTime) {
            view.setBackgroundColor(Color.parseColor("#11000000"));
        } else if (time > itemTime.sTime) {
            view.setBackgroundColor(Color.parseColor("#330000FF"));
        } else {
            view.setBackgroundColor(Color.WHITE);
        }
    }


    public int getGroupCount() {
        return data.size();
    }

    public int getChildrenCount(int group) {
        return data.get(group).size() - 1;
    }


    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < data.get(i).size(); j++) {
                str.append("\ndata[" + i + ", " + j + "] = " + data.get(i).get(j));
            }
        }
        return str.toString();
    }

    public static class ItemTime {
        private long sTime;
        private long eTime;
        private boolean isCompleted;
        private DataItem dataItem;


        public ItemTime(long sTime, long eTime, DataItem dataItem) {
            this.sTime = sTime;
            this.eTime = eTime;
            this.dataItem = dataItem;
        }

        public long getSTime() {
            return sTime;
        }

        public long getETime() {
            return eTime;
        }

        public boolean checkCompleted(long time) {
            if (time > eTime) {
                isCompleted = true;
            }
            return isCompleted;
        }

        public DataItem getDataItem() {
            return dataItem;
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("sTime = " + sTime);
            str.append(", eTime = " + eTime);
            str.append(", dataItem = " + dataItem);
            return "\n" + str.toString();
        }
    }
}
