package ru.railway.dc.routes;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import org.apache.log4j.Logger;

import ru.railway.dc.routes.database.utils.EventTableUtils;
import ru.railway.dc.routes.event.ManagerEvent;
import ru.railway.dc.routes.event.ManagerEventUtils;
import ru.railway.dc.routes.event.activity.ActivityTime;
import ru.railway.dc.routes.event.activity.AsyncItemSchedule;
import ru.railway.dc.routes.event.activity.data.ItemSchedule;
import ru.railway.dc.routes.event.notification.NotificationTime;

/**
 * Created by SQL on 27.12.2016.
 */

public class FavouriteActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<ItemSchedule> {

    public static final Logger logger = Logger.getLogger(FavouriteActivity.class);

    private ExpandableListView elvMain;

    // Соответствие позиции и listRouteID
    private int eventID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        eventID = getIntent().getIntExtra(NotificationTime.PARAM_EVENT_ID, -1);
        elvMain = (ExpandableListView) findViewById(R.id.elvMain);
        elvMain.setDivider(null);
        elvMain.setGroupIndicator(null);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.route_title);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getSupportLoaderManager().initLoader(0, null, this).forceLoad();
    }

    // ================================ МЕНЮ =======================================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_favourite, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Not used
//        EventTableUtils.EventID eventID = EventTableUtils.loadEventID(this.eventID);
//        if (eventID.isNotification()) {
//            MenuItem menuItem = menu.getItem(0);
//            menuItem.setChecked(false);
//            menuItem.setIcon(android.R.drawable.ic_media_pause);
//        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        switch (item.getItemId()) {
            case R.id.itemRemove:
                // Удаляем уведомление
                removeNotification(eventID);
                // Чистим в БД
                EventTableUtils.removeData(eventID);
                // Удаляем из карты и проверка на empty
                startActivity(new Intent(this,
                        MainActivity.class));
                finish();
                break;
                // Not used
//            case R.id.itemAddNotification:
//                boolean isChecked = item.isChecked();
//                if (isChecked) {
//                    EventTableUtils.update(eventID, true, null);
//                    item.setChecked(!item.isChecked());
//                    // Добавляем уведомления
//                    ManagerEventUtils.addNotification(this, eventID);
//                } else {
//                    removeNotification(eventID);
//                }
//
//                item.setChecked(!isChecked);
//                item.setIcon(item.isChecked() ? android.R.drawable.ic_media_play :
//                        android.R.drawable.ic_media_pause);
//                break;
        }
        return true;
    }


    private void removeNotification(int id) {
        EventTableUtils.update(id, false, null);
        ManagerEvent.remove(id, ManagerEvent.PREF_NOTIFICATION);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(id);
    }


    // ============================= ПРЕОБРАЗОВАНИЕ ДАННЫХ ДЛЯ АДАПТЕРА ============================

    @Override
    public Loader<ItemSchedule> onCreateLoader(int id, Bundle args) {
        return new AsyncItemSchedule(this, eventID);
    }

    @Override
    public void onLoadFinished(Loader<ItemSchedule> loader, ItemSchedule data) {
        elvMain.setAdapter(new ActivityTime(data, this));
    }

    @Override
    public void onLoaderReset(Loader<ItemSchedule> loader) {
    }
}
