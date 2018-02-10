package ru.railway.dc.routes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.melnykov.fab.FloatingActionButton;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import it.sephiroth.android.library.tooltip.Tooltip;
import ru.railway.dc.routes.event.notification.NotificationTime;
import ru.railway.dc.routes.request.data.FillRequestData;
import ru.railway.dc.routes.request.data.RequestData;
import ru.railway.dc.routes.request.data.RequestDataSingleton;
import ru.railway.dc.routes.request.fragment.StationFragment;
import ru.railway.dc.routes.request.fragment.dialog.DateFragment;
import ru.railway.dc.routes.request.model.Station;
import ru.railway.dc.routes.utils.TooltipManager;

public class MainActivity extends AppCompatActivity implements RequestDataSingleton.OnChangeDataListener {

    private final String LOG_TAG = "mainActivityLog";

    public static final SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat FORMAT_SHORT_DATE = new SimpleDateFormat("dd.MM");
    public static final SimpleDateFormat FORMAT_TIME = new SimpleDateFormat("HH:mm");

    // Компоненты
    private ViewHolder holder;
    private Drawer drawer;
    private BottomSheetBehavior bottomSheetBehavior;
    private CardView cardView;
    private SharedPreferences sp;
    private boolean isEDateTime;
    private ImageView imgArrow;
    private ImageView additionalSearch;
    private boolean isBottomSheetExpanded = false;

    // =========================== ОБЪЯВЛЕНИЕ И ИНИЦИАЛИЗАЦИЯ ФРАГМЕНТОВ ===========================

    DateFragment bDateFragment;
    DateFragment eDateFragment;

    private void initFragment() {
        // Общие данные
        RequestDataSingleton rds = RequestDataSingleton.getInstance();
        rds.register(this);
        // Фрагменты
        bDateFragment = DateFragment.newInstance(RequestDataSingleton.Param.B_DATE);
        eDateFragment = DateFragment.newInstance(RequestDataSingleton.Param.E_DATE);
    }

    // ======================== ДЕЙСТВИЯ ПРИ ОБНОВЛЕНИИ ============================================

    @Override
    public void changeData(String param, RequestDataSingleton rds) {                                // Упростить метод
        switch (param) {
            case RequestDataSingleton.Param.E_DATE:
                String eDate = (String) rds.findDataByName(param);
                holder.eDate.setText(convertDateToShort(eDate));
                break;
            case RequestDataSingleton.Param.B_DATE:
                String date = (String) rds.findDataByName(param);
                holder.bDate.setText(convertDateToShort(date));

                Calendar c = Calendar.getInstance();
                String today = MainActivity.FORMAT_DATE.format(c.getTime());
                c.add(Calendar.DAY_OF_MONTH, 1);
                String tomorrow = MainActivity.FORMAT_DATE.format(c.getTime());

                if (date.equals(today)) {
                    selectToday(true);
                    selectTomorrow(false);
                } else {
                    if (date.equals(tomorrow)) {
                        selectTomorrow(true);
                        selectToday(false);
                    } else {
                        selectToday(false);
                        selectTomorrow(false);
                    }
                }
                break;
            case RequestDataSingleton.Param.B_STATION:
                Station bStation = (Station) rds.findDataByName(param);
                String bStationName = bStation.getName();
                App.Companion.getPref().edit().putString(FillRequestData.KEY_PREF_B_STATION, bStationName).apply();
                ((TextView) findViewById(R.id.bStation)).setText(bStationName);
                break;
            case RequestDataSingleton.Param.E_STATION:
                Station eStation = (Station) rds.findDataByName(param);
                String eStationName = eStation.getName();
                App.Companion.getPref().edit().putString(FillRequestData.KEY_PREF_E_STATION, eStationName).apply();
                ((TextView) findViewById(R.id.eStation)).setText(eStationName);
                break;
        }
    }

    private String convertDateToShort(String date) {
        String shortDate = null;
        try {
            shortDate = FORMAT_SHORT_DATE.format(FORMAT_DATE.parse(date).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return shortDate;
    }

    // ======================== ON_CREATE ==========================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "onCreate 1");

        EventService.clearFlag();
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        Log.d(LOG_TAG, "onCreate 2");

        int stationTime = Integer.valueOf(sp.getString(getString(R.string.pref_name_stationtime),
                getString(R.string.pref_value_stationtime)));
        RequestData.setDefaultDuration(stationTime);

        // Если activity вызвано уведомлением
        Intent intent = getIntent();
        int eventID = intent.getIntExtra(NotificationTime.PARAM_EVENT_ID, -1);
        if (eventID != -1) {
        } else {
            RequestDataSingleton.loadData(FillRequestData.Companion.newInstance(this));
        }

        Log.d(LOG_TAG, "onCreate 3");

        initFragment();

        setContentView(R.layout.activity_main);

        Toolbar toolbar = createToolbar();
        initialsNavigationDrawer(toolbar);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        FloatingActionButton fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        initBottomSheet(bottomSheet, fab, fabAdd);

        cardView = (CardView) findViewById(R.id.cardView);

        holder = new ViewHolder(findViewById(android.R.id.content));

        imgArrow = (ImageView) findViewById(R.id.imgArrow);
        additionalSearch = (ImageView) findViewById(R.id.additionalSearch);

        // Обновляем все данные
        RequestDataSingleton.getInstance().updateData();
        prepareToolTip(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                prepareToolTip(false);
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume 1");

        isEDateTime = sp.getBoolean(getString(R.string.pref_name_edatetime),
                Boolean.valueOf(getString(R.string.pref_value_edatetime)));
        if (isEDateTime) {
            holder.eCalendarDate.setVisibility(View.VISIBLE);
        } else {
            holder.eCalendarDate.setVisibility(View.INVISIBLE);
        }
    }

    private void initBottomSheet(View bottomSheet, final FloatingActionButton fab,
                                 final FloatingActionButton fabAdd) {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(getResources()
                .getDimensionPixelSize(R.dimen.sheet_bottom_height));
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    fab.animate().scaleX(0).scaleY(0).setDuration(300).start();
                    additionalSearch.animate().alpha(0F).setDuration(300).start();
                    fabAdd.animate().scaleX(0).scaleY(0).setDuration(300).start();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        cardView.setElevation(getResources()
                                .getDimensionPixelSize(R.dimen.sheet_bottom_elevation_off));
                    }
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    additionalSearch.setVisibility(View.INVISIBLE);
                    fab.animate().scaleX(1).scaleY(1).setDuration(300).start();
                    fabAdd.animate().scaleX(0).scaleY(0).setDuration(300).start();
                    fab.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        cardView.setElevation(getResources()
                                .getDimensionPixelSize(R.dimen.sheet_bottom_elevation_on));
                    }
                    isBottomSheetExpanded = false;
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    additionalSearch.setVisibility(View.VISIBLE);
                    additionalSearch.animate().alpha(1F).setDuration(300).start();
                    fab.setVisibility(View.INVISIBLE);
                    fabAdd.animate().scaleX(1).scaleY(1).setDuration(300).start();

                    isBottomSheetExpanded = true;
                    TooltipManager tooltipManager = App.Companion.getTooltipManager();
                    tooltipManager.setContext(TooltipManager.MAIN_BOTTOM_SHEET_ON_ITEM_LISTENER_GROUP, MainActivity.this);
                    tooltipManager.show(TooltipManager.MAIN_BOTTOM_SHEET_ON_LISTENER_GROUP,
                            TooltipManager.MAIN_BOTTOM_SHEET_ON_ITEM_LISTENER_GROUP);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                imgArrow.setRotation(slideOffset * 180);
            }
        });
    }

    @Nullable
    private Toolbar createToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        return toolbar;
    }

    // ================================ TOOLTIP ====================================================


    private void prepareToolTip(boolean isFirst) {
        TooltipManager tooltipManager = App.Companion.getTooltipManager();

        if (!isFirst)
            tooltipManager.resetGroup(TooltipManager.MAIN_GROUP);
        tooltipManager.addToolTip(findViewById(R.id.bStation), R.string.tooltip_main_content_bstation, Tooltip.Gravity.BOTTOM,
                TooltipManager.MAIN_GROUP, true, this);
        tooltipManager.addToolTip(findViewById(R.id.eStation), R.string.tooltip_main_content_estation, Tooltip.Gravity.BOTTOM,
                TooltipManager.MAIN_GROUP, true, this);
        tooltipManager.addToolTip(findViewById(R.id.bCalendarDate), R.string.tooltip_main_content_bcalendar, Tooltip.Gravity.BOTTOM,
                TooltipManager.MAIN_GROUP, true, this);
        tooltipManager.show(TooltipManager.MAIN_GROUP);

        if (!isBottomSheetExpanded) {
            if (!isFirst) {
                tooltipManager.resetGroup(TooltipManager.MAIN_BOTTOM_SHEET_OFF_GROUP,
                        TooltipManager.MAIN_BOTTOM_SHEET_ON_LISTENER_GROUP,
                        TooltipManager.MAIN_BOTTOM_SHEET_ON_ITEM_LISTENER_GROUP);
            }
            tooltipManager.addToolTip(findViewById(R.id.fab), R.string.tooltip_main_content_search, Tooltip.Gravity.LEFT,
                    TooltipManager.MAIN_BOTTOM_SHEET_OFF_GROUP, true, this);
        } else {
            if (!isFirst)
                tooltipManager.resetGroup(TooltipManager.MAIN_BOTTOM_SHEET_ON_GROUP,
                        TooltipManager.MAIN_BOTTOM_SHEET_ON_ITEM_GROUP);
            tooltipManager.addToolTip(findViewById(R.id.additionalSearch), R.string.tooltip_main_content_search, Tooltip.Gravity.LEFT,
                    TooltipManager.MAIN_BOTTOM_SHEET_ON_GROUP, true, this);
        }
    }


    // ================================ DRAWER =====================================================

    private void initialsNavigationDrawer(Toolbar toolbar) {

        AccountHeader accountHeader = createAccountHeader();

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withDisplayBelowStatusBar(true)
                .withActionBarDrawerToggleAnimated(true)
                .withAccountHeader(accountHeader)
                .addDrawerItems(
                        initialDrawerItems()
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Intent intent = null;
                        switch (position) {
                            case 3:
                                intent = new Intent(MainActivity.this,
                                        PrefActivity.class);
                                break;
                            case 1:
                                intent = new Intent(MainActivity.this,
                                        FavouritePreviewActivity.class);
                                break;
                        }
                        if (intent != null) {
                            startActivity(intent);
                        }
                        return true;
                    }
                })
                .build();


    }

    @NonNull
    private IDrawerItem[] initialDrawerItems() {
        return new IDrawerItem[]{new PrimaryDrawerItem()
                .withName("Маршруты")
                .withIdentifier(1)
                .withIcon(R.drawable.ic_drawer_favourite)
                .withSelectedTextColor(getResources().getColor(R.color.primary_text)),
                new DividerDrawerItem(),
                new PrimaryDrawerItem()
                        .withName("Настройки")
                        .withTextColor(getResources().getColor(R.color.primary_text))
                        .withIdentifier(2)
                        .withIcon(R.drawable.ic_drawer_setting)
                        .withSelectable(false)};
    }

    private AccountHeader createAccountHeader() {
        return new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.drawer_background)
                .withOnAccountHeaderSelectionViewClickListener(new AccountHeader
                        .OnAccountHeaderSelectionViewClickListener() {
                    @Override
                    public boolean onClick(View view, IProfile profile) {
                        drawer.closeDrawer();
                        return true;
                    }
                })
                .build();
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    // ================================== VIEW_HOLDER ==============================================

    class ViewHolder {
        // Дата
        public View bCalendarDate;
        public View eCalendarDate;
        public TextView today;
        public TextView tomorrow;
        public TextView bDate;
        public TextView eDate;
        // Станции
        public TextView bStation;
        public TextView eStation;
        public ImageView swapStation;

        public ViewHolder(View v) {
            // Дата
            bCalendarDate = v.findViewById(R.id.bCalendarDate);
            eCalendarDate = v.findViewById(R.id.eCalendarDate);
            today = (TextView) v.findViewById(R.id.today);
            tomorrow = (TextView) v.findViewById(R.id.tomorrow);
            bDate = (TextView) v.findViewById(R.id.bDate);
            eDate = (TextView) v.findViewById(R.id.eDate);
            // Станции
            bStation = (TextView) v.findViewById(R.id.bStation);
            eStation = (TextView) v.findViewById(R.id.eStation);
            swapStation = (ImageView) v.findViewById(R.id.swapStation);
        }
    }

    // ================================= ОБРАБОТЧИК НАЖАТИЯ НА КНОПКИ ==============================

    private void selectToday(boolean isToday) {
        if (isToday) {
            holder.today.setTextSize(18);
            holder.today.setTextColor(Color.BLACK);
        } else {
            holder.today.setTextSize(15);
            holder.today.setTextColor(Color.GRAY);
        }
    }

    private void selectTomorrow(boolean isTomorrow) {
        if (isTomorrow) {
            holder.tomorrow.setTextSize(18);
            holder.tomorrow.setTextColor(Color.BLACK);
        } else {
            holder.tomorrow.setTextSize(15);
            holder.tomorrow.setTextColor(Color.GRAY);
        }
    }

    public void onChangeDate(View view) {
        int id = view.getId();

        if (id == R.id.bCalendarDate) {
            bDateFragment.show(getSupportFragmentManager(), "bDate");
        } else if (id == R.id.eCalendarDate) {
            eDateFragment.show(getSupportFragmentManager(), "eDate");
        } else if (id == R.id.today || id == R.id.tomorrow) {
            Calendar c = Calendar.getInstance();
            if (id == R.id.tomorrow) {
                c.add(Calendar.DAY_OF_MONTH, 1);
            }
            String date = MainActivity.FORMAT_DATE.format(c.getTime());
            RequestDataSingleton.getInstance().notification(RequestDataSingleton.Param.B_DATE, date);
            RequestDataSingleton.getInstance().notification(RequestDataSingleton.Param.E_DATE, date);
        }
    }

    public void onChoiceStation(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.fabAdd:
                intent = new Intent(this, StationActivity.class);
                intent.putExtra(StationActivity.PARAM,
                        RequestDataSingleton.Param.I_STATION);
                startActivity(intent);
                break;
            case R.id.swapStation:
                if (hasStations()) {
                    RequestDataSingleton rds = RequestDataSingleton.getInstance();
                    Station bStation = (Station) rds.findDataByName(RequestDataSingleton.Param.B_STATION);
                    Station eStation = (Station) rds.findDataByName(RequestDataSingleton.Param.E_STATION);
                    if (!bStation.equals(eStation)) {
                        rds.notification(RequestDataSingleton.Param.B_STATION, eStation);
                        rds.notification(RequestDataSingleton.Param.E_STATION, bStation);
                    }
                }
                break;
            case R.id.bStation:
                intent = new Intent(this, StationActivity.class);
                intent.putExtra(StationActivity.PARAM,
                        RequestDataSingleton.Param.B_STATION);
                startActivity(intent);
                break;
            case R.id.eStation:
                intent = new Intent(this, StationActivity.class);
                intent.putExtra(StationActivity.PARAM,
                        RequestDataSingleton.Param.E_STATION);
                startActivity(intent);
                break;
        }
    }

    private boolean hasStations() {
        RequestDataSingleton rds = RequestDataSingleton.getInstance();
        if (getResources().getString(R.string.search_placeholder_bString).equalsIgnoreCase(((Station) rds.findDataByName(RequestDataSingleton.Param.B_STATION)).getName())
                || getResources().getString(R.string.search_placeholder_eString).equalsIgnoreCase(((Station) rds.findDataByName(RequestDataSingleton.Param.E_STATION)).getName()))
            return false;
        return true;
    }


    // ============================= ПОСЫЛКА ЗАПРОСА ===============================================

    public void onClickResult(View view) {
        if (hasStations()) {
            RequestDataSingleton rds = RequestDataSingleton.getInstance();
            String bStation = ((Station) rds.findDataByName(RequestDataSingleton.Param.B_STATION)).getName();
            String eStation = ((Station) rds.findDataByName(RequestDataSingleton.Param.E_STATION)).getName();
            App.Companion.getPref().edit().putString(FillRequestData.KEY_PREF_B_STATION, bStation)
                    .putString(FillRequestData.KEY_PREF_E_STATION, eStation).apply();
            startActivity(new Intent(this, ScheduleActivity.class));
        } else {
            Toast.makeText(this, "Станция отправления или прибытия не выбраны", Toast.LENGTH_SHORT).show();
        }
    }


    // =============================================================================================
}
