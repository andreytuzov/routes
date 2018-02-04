package ru.railway.dc.routes.event.activity;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;
import java.util.Map;

import ru.railway.dc.routes.R;
import ru.railway.dc.routes.database.utils.EventTableUtils;

/**
 * Created by SQL on 24.01.2017.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int eventID);
    }

    private OnItemClickListener listener;

    private List<EventTableUtils.EventID> eventIDs;
    private Map<EventTableUtils.EventID, View> map;

    public RecyclerAdapter(List<EventTableUtils.EventID> eventIDs,
                           Map<EventTableUtils.EventID, View> map, OnItemClickListener listener) {
        this.eventIDs = eventIDs;
        this.map = map;
        this.listener = listener;
    }

    public void remove(int position) {
        Log.d("", "position = " + position);
        EventTableUtils.EventID eventID = eventIDs.get(position);
        map.remove(eventID);
        eventIDs.remove(position);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout layout;

        public ViewHolder(View v) {
            super(v);
            layout = (LinearLayout) v.findViewById(R.id.layout);
        }
    }

    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_favourite_preview_cardview, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.ViewHolder holder, final int position) {
        EventTableUtils.EventID eventID = eventIDs.get(position);
        View view = map.get(eventID);
        holder.layout.removeAllViews();
        if (view.getParent() != null) {
            ((ViewGroup)view.getParent()).removeView(view);
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(eventIDs.get(position).getId());
            }
        });
        holder.layout.addView(view);
    }


    @Override
    public int getItemCount() {
        return eventIDs.size();
    }

}
