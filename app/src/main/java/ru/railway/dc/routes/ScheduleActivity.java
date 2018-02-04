package ru.railway.dc.routes;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import com.github.mrengineer13.snackbar.SnackBar;
import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;

import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ru.railway.dc.routes.database.utils.EventTableUtils;
import ru.railway.dc.routes.display.ScheduleAdapter;
import ru.railway.dc.routes.display.sort.TypeSortEnum;
import ru.railway.dc.routes.search.ScheduleAsyncLoader;
import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.model.Route;
import ru.railway.dc.routes.search.model.Schedule;
import ru.railway.dc.routes.tools.AppUtils;

/**
 * Created by SQL on 27.12.2016.
 */

public class ScheduleActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Schedule> {

    private static final Logger logger = Logger.getLogger(ScheduleActivity.class);

    public static final int SCHEDULE_LOADER_ID = 1;

    private ScheduleAdapter adapter;
    private ExpandableListView elvMain;
    private Schedule schedule;

    // Атрибуты групп
    private static final String ATTR_GROUP_B_TIME = "bTime";
    private static final String ATTR_GROUP_E_TIME = "eTime";
    private static final String ATTR_GROUP_B_STATION = "bStation";
    private static final String ATTR_GROUP_E_STATION = "eStation";
    private static final String ATTR_GROUP_TOTAL_TIME = "totalTime";
    private static final String ATTR_GROUP_ID = "_id";

    // Атрибуты child
    private static final String ATTR_CHILD_B_TIME = "bTime";
    private static final String ATTR_CHILD_E_TIME = "eTime";
    private static final String ATTR_CHILD_B_STATION = "bStation";
    private static final String ATTR_CHILD_E_STATION = "eStation";
    private static final String ATTR_CHILD_TRAIN_TIME = "trainTime";
    private static final String ATTR_CHILD_STATION_TIME = "stationTime";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_schedule_load);

        if (!AppUtils.hasConnection()) {
            new SnackBar.Builder(ScheduleActivity.this)
                    .withMessage("Используется только кэш")
                    .withDuration(SnackBar.MED_SNACK)
                    .show();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Поиск маршрута");
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Инициализация Loader
        getLoaderManager().initLoader(SCHEDULE_LOADER_ID, null, this);
        // Запускаем на выполнение
        Loader<Schedule> loader = getLoaderManager().getLoader(SCHEDULE_LOADER_ID);
        loader.forceLoad();
    }


    // ================================= МЕНЮ ======================================================

    private void showContextMenu(final int id) {
        new BottomSheet.Builder(this, R.style.MyBottomSheetStyle)
                .setSheet(R.menu.activity_schedule_context)
                .setListener(new BottomSheetListener() {
                    @Override
                    public void onSheetShown(@NonNull BottomSheet bottomSheet) {
                    }

                    // Обработка нажатия кнопки
                    @Override
                    public void onSheetItemSelected(@NonNull BottomSheet bottomSheet, MenuItem menuItem) {
                        // Запускаем задачу на выполнение
                        if (!AppUtils.hasConnection()) {
                            new SnackBar.Builder(ScheduleActivity.this)
                                    .withMessage("Используется только кэш")
                                    .withDuration(SnackBar.MED_SNACK)
                                    .show();
                        }
                        MyTask myTask = new MyTask(ScheduleActivity.this);
                        myTask.execute(new ListRoute[]{schedule.get(id)});
                    }

                    @Override
                    public void onSheetDismissed(@NonNull BottomSheet bottomSheet, @DismissEvent int i) {
                    }
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_schedule, menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        if (adapter == null) {
            return super.onOptionsItemSelected(item);
        }
        TypeSortEnum typeSortEnum = null;
        switch(item.getItemId()) {
            case R.id.itemSortDepartureTime:
                typeSortEnum = TypeSortEnum.DEPARTURE_TIME;
                break;
            case R.id.itemSortArrivalTime:
                typeSortEnum = TypeSortEnum.ARRIVAL_TIME;
                break;
            case R.id.itemSortTotalTime:
                typeSortEnum = TypeSortEnum.TOTAL_TIME;
                break;
            case R.id.itemSortStationTime:
                typeSortEnum = TypeSortEnum.STATION_TIME;
                break;
            case R.id.itemSortTrainTime:
                typeSortEnum = TypeSortEnum.TRAIN_TIME;
                break;
        }
        if (typeSortEnum != null) {
            adapter.sort(typeSortEnum);
        }
        return super.onOptionsItemSelected(item);
    }

    // Формат вывода подзаголовка
    private String formatString(int n) {
        if (n >= 5 && n <= 20) {
            return "Найдено " + n + " вариантов";
        } else if (n % 10 == 1) {
            return "Найден " + n + " вариант";
        } else if (n % 10 >= 2 && n % 10 <= 4) {
            return "Найдено " + n + " варианта";
        }
        return "Найдено " + n + " вариантов";
    }

    // =================== РЕАЛИЗАЦИЯ ИНТЕРФЕЙСА LOADER_CALLBACKS ==================================

    @Override
    public Loader<Schedule> onCreateLoader(int id, Bundle args) {
        Loader<Schedule> loader = null;
        if (id == SCHEDULE_LOADER_ID) {
            loader = new ScheduleAsyncLoader(this);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Schedule> loader, Schedule data) {
        setContentView(R.layout.activity_schedule);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat(Route.DATE_TIME_FORMAT);
        getSupportActionBar().setSubtitle("Запрос: " + format.format(calendar.getTime()));


        // Если данных нет
        if (data == null || data.size() == 0) {
            new SnackBar.Builder(this)
                    .withMessage("Подходящего расписания нет")
                    .withDuration(SnackBar.MED_SNACK)
                    .withBackgroundColorId(R.color.primary_dark)
                    .show();
            return;
        }

        toolbar.setTitle(formatString(data.size()));




        schedule = data;
        elvMain = (ExpandableListView) findViewById(R.id.elvMain);
        elvMain.setDivider(null);
        // Если промежуточных станций нет
        if (data.get(0).size() == 1) {
            elvMain.setGroupIndicator(null);
        }
        adapter = new ScheduleAdapter(this, data, new ScheduleAdapter.OnHeaderLongClickListener() {
            @Override
            public void onHeaderSelected(int position) {
                showContextMenu(position);
            }
        });
        logger.info("Установка адаптера");
        elvMain.setAdapter(adapter);
        logger.info("Адаптер установлен");
    }

    @Override
    public void onLoaderReset(Loader<Schedule> loader) {

    }

    // ============================== АСИНХРОННАЯ ЗАГРУЗКА ДАННЫХ ==================================

    // Добавление в избранное
    static class MyTask extends AsyncTask<ListRoute, Void, Boolean> {


        private Activity activity;

        public MyTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected Boolean doInBackground(ListRoute... params) {
            ListRoute listRoute = params[0];
            //  Сохраняем данные в таблицу избранного
            long eDateTime = listRoute.get(listRoute.size() - 1).getETime().getTimeInMillis();
            boolean result = EventTableUtils.saveDataWithLoadDetail(listRoute, false, false,
                    eDateTime);
            return result;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            if (isSuccess) {
                new SnackBar.Builder(activity)
                        .withMessage("Расписание сохранено")
                        .withDuration(SnackBar.MED_SNACK)
                        .show();
            } else {
                new SnackBar.Builder(activity)
                        .withMessage("Не найдены маршруты")
                        .show();
            }
        }
    }

}
