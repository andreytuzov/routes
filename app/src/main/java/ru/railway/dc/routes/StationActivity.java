package ru.railway.dc.routes;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import it.sephiroth.android.library.tooltip.Tooltip;
import ru.railway.dc.routes.database.AssetsDB;
import ru.railway.dc.routes.database.assets.struct.CountryView;
import ru.railway.dc.routes.request.data.RequestDataSingleton;
import ru.railway.dc.routes.request.model.Station;
import ru.railway.dc.routes.utils.RUtils;
import ru.railway.dc.routes.utils.TooltipManager;

/**
 * Created by SQL on 14.01.2017.
 */

public class StationActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {

    private static final int STATION_LOADER_ID = 1;
    private String param;

    public static final String PARAM = "param";

    private AssetsDB db;
    private MyLoader loader;

    private int idIndex;
    private Map<Integer, Integer> mFavourite;

    private SimpleCursorAdapter adapter;
    private boolean isItemFirst = true;
    private boolean isSearchExpanded = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station);
        mFavourite = new HashMap<>();
        param = getIntent().getStringExtra(PARAM);

        initToolbar();

        // Устанавлием адаптер для списка
        ListView lvMain = (ListView) findViewById(R.id.lvMain);
        adapter = createAdapter(this);
        lvMain.setAdapter(adapter);
        lvMain.setOnItemClickListener(this);

        // Инициализация loader для загрузки данных
        db = new AssetsDB();
        db.open();
        getSupportLoaderManager().initLoader(STATION_LOADER_ID, null, this);
        loader.forceLoad();
    }

    private void prepareTooltip(boolean isFirst) {
        TooltipManager tooltipManager = App.Companion.getTooltipManager();
        if (!isSearchExpanded) {
            if (!isFirst)
                tooltipManager.resetGroup(TooltipManager.STATION_GROUP);
            Point p = new Point(RUtils.INSTANCE.getScreenWidth(getWindowManager())
                    - (int) RUtils.INSTANCE.convertDpToPixels(72, this),
                    RUtils.INSTANCE.getDimenFromAttr(android.R.attr.actionBarSize, this) / 2
                            + RUtils.INSTANCE.getDimenFromRes("status_bar_height", this));
            tooltipManager.addToolTip(p, R.string.tooltip_station_search, Tooltip.Gravity.BOTTOM,
                    TooltipManager.STATION_GROUP, false, this);

            tooltipManager.show(TooltipManager.STATION_GROUP);
        }
        if (!isFirst)
            tooltipManager.resetGroup(TooltipManager.STATION_ITEM_GROUP);
        isItemFirst = true;
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_station, menu);

        final MenuItem actionSearchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) actionSearchItem.getActionView();
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSearchExpanded = true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                isSearchExpanded = false;
                actionSearchItem.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loader.setPattern(newText);
                loader.forceLoad();
                return true;
            }
        });

        prepareTooltip(true);

        return super.onCreateOptionsMenu(menu);
    }

    private SimpleCursorAdapter createAdapter(final Context context) {
        final SimpleCursorAdapter adapter = new AdapterHelper(this).getAdapter();
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, final int columnIndex) {
                // Если картинка
                if (view.getId() == R.id.imgStar) {
                    // Adding tooltip for favourite button
                    if (isItemFirst) {
                        TooltipManager tooltipManager = App.Companion.getTooltipManager();
                        tooltipManager.addToolTip(view, R.string.tooltip_station_item_favourite, Tooltip.Gravity.BOTTOM,
                                TooltipManager.STATION_ITEM_GROUP, false, context);
                        tooltipManager.show(TooltipManager.STATION_ITEM_GROUP);
                        isItemFirst = false;
                    }

                    // Получение данных
                    final int id = cursor.getInt(idIndex);
                    final int favourite;
                    if (mFavourite.containsKey(id)) {
                        favourite = mFavourite.get(id);
                    } else {
                        favourite = cursor.getInt(columnIndex);
                    }
                    // Обновление картинки
                    ImageView iv = (ImageView) view;
                    if (favourite == 1) {
                        iv.setImageResource(android.R.drawable.star_big_on);
                    } else {
                        iv.setImageResource(android.R.drawable.star_big_off);
                    }
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int f;
                            if (mFavourite.containsKey(id)) {
                                f = mFavourite.get(id);
                            } else {
                                f = favourite;
                            }
                            f = 1 - f;
                            // Запоминаем состояние
                            db.setFavourite(id, f);
                            // Меняем картинку
                            mFavourite.put(id, f);
                            // Обновление картинки
                            ImageView iv = (ImageView) v;
                            if (f == 1) {
                                iv.setImageResource(android.R.drawable.star_big_on);
                            } else {
                                iv.setImageResource(android.R.drawable.star_big_off);
                            }
                        }
                    });
                    return true;
                }
                return false;
            }
        });
        return adapter;
    }

    // ================================ TOOLBAR ====================================================

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (param.equals(RequestDataSingleton.Param.B_STATION)) {
            toolbar.setTitle("Откуда");
        } else if (param.equals(RequestDataSingleton.Param.E_STATION)) {
            toolbar.setTitle("Куда");
        } else {
            toolbar.setTitle("Станции");
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_help:
                prepareTooltip(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ====================== ОБРАБОТЧИКИ НАЖАТИЙ ==================================================

    // Выбор элемента
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        TextView tvName = (TextView) view.findViewById(R.id.tvName);
        TextView tvRegion = (TextView) view.findViewById(R.id.tvRegion);

        String name = tvName.getText().toString();
        String region = tvRegion.getText().toString();

        Station station = new Station(id, name, region);
        RequestDataSingleton.getInstance().notification(param, station);
        finish();
    }

    @Override
    protected void onDestroy() {
        App.Companion.getTooltipManager().close(this);
        super.onDestroy();
        db.close();
    }

    // ===================================== LOADER ================================================

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        loader = new MyLoader(this, db);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        this.idIndex = data.getColumnIndex(CountryView.COLUMN_ID);
        mFavourite.clear();
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    static class MyLoader extends CursorLoader {

        AssetsDB db;
        String pattern;

        public MyLoader(Context context, AssetsDB db) {
            super(context);
            this.db = db;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        @Override
        protected Cursor onLoadInBackground() {
            return db.getStationCursor(pattern);
        }
    }


    // ==================================== ADAPTER ================================================

    static class AdapterHelper {

        int layout = R.layout.adapter_station_item;
        String[] from = new String[] {CountryView.COLUMN_NAME, CountryView.COLUMN_REGION,
                CountryView.COLUMN_FAVOURITE};
        int[] to = new int[] {R.id.tvName, R.id.tvRegion, R.id.imgStar};

        private Context context;

        public AdapterHelper(Context context) {
            this.context = context;
        }

        SimpleCursorAdapter getAdapter() {
            return new SimpleCursorAdapter(context, layout,
                    null, from, to, 0);
        }
    }
}
