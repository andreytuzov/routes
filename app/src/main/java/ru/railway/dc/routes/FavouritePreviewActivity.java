package ru.railway.dc.routes;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import ru.railway.dc.routes.database.utils.EventTableUtils;
import ru.railway.dc.routes.event.ManagerEvent;
import ru.railway.dc.routes.event.activity.AsyncListItemSchedule;
import ru.railway.dc.routes.event.activity.RecyclerAdapter;
import ru.railway.dc.routes.event.notification.NotificationTime;

public class FavouritePreviewActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Map<EventTableUtils.EventID, View>> {

    private RecyclerView rvMain;
    private List<EventTableUtils.EventID> eventIDs;
    private RecyclerAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        eventIDs = EventTableUtils.loadEventIDs(0);
        if (eventIDs == null || eventIDs.isEmpty()) {
            Toast.makeText(this, "Сохраненных маршрутов нет", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_favourite_preview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Маршруты");
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvMain = (RecyclerView) findViewById(R.id.rvMain);

        initSwipe();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rvMain.setLayoutManager(layoutManager);

        getSupportLoaderManager().initLoader(0, null, this).forceLoad();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initSwipe() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT ) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            private void removeNotification(int id) {
                EventTableUtils.update(id, false, null);
                ManagerEvent.remove(id, ManagerEvent.PREF_NOTIFICATION);
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel(id);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                // Диалог для подтверждения
                new AlertDialog.Builder(FavouritePreviewActivity.this)
                    .setTitle("Вы хотите удалить элемент ?")
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EventTableUtils.removeData(eventIDs.get(position).getId());
                            removeNotification(eventIDs.get(position).getId());
                            adapter.remove(position);
                            if (adapter.getItemCount() == 0) {
                                finish();
                            }
                        }
                    })
                    .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.notifyItemChanged(position);
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            adapter.notifyItemChanged(position);
                        }
                    })
                    .show();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvMain);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    // ======================= ПОЛУЧЕНИЕ АДАПТЕРА ==================================================

    @Override
    public Loader<Map<EventTableUtils.EventID, View>> onCreateLoader(int id, Bundle args) {
        return new AsyncListItemSchedule(this, eventIDs);
    }

    @Override
    public void onLoadFinished(Loader<Map<EventTableUtils.EventID, View>> loader,
                               Map<EventTableUtils.EventID, View> data) {
        adapter = new RecyclerAdapter(eventIDs, data, new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int eventID) {
                Intent intent = new Intent(FavouritePreviewActivity.this, FavouriteActivity.class);
                intent.putExtra(NotificationTime.PARAM_EVENT_ID, eventID);
                startActivity(intent);
            }
        });
        rvMain.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Map<EventTableUtils.EventID, View>> loader) {
    }
}
