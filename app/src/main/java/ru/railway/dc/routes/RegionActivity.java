package ru.railway.dc.routes;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import ru.railway.dc.routes.database.AssetsDB;

/**
 * Created by SQL on 29.01.2017.
 */

public class RegionActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {

    private static final int REGION_LOADER_ID = 1;
    private static final int COUNT_REGION = 131;

    private ListView lvMain;
    private Toolbar toolbar;

    private AssetsDB db;
    private MyLoader loader;
    private SimpleCursorAdapter adapter;
    private int idIndex;

    private List<Long> list;
    // Для первоначального заполнения
    private boolean isInit;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region);

        list = new ArrayList<>();

        initToolbar();

        // Устанавлием адаптер для списка
        lvMain = (ListView) findViewById(R.id.lvMain);
        lvMain.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        lvMain.setOnItemClickListener(this);
        adapter = createAdapter();
        lvMain.setAdapter(adapter);

        // Инициализация loader для загрузки данных
        db = new AssetsDB();
        db.open();
        getSupportLoaderManager().initLoader(REGION_LOADER_ID, null, this);
        loader.forceLoad();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    private SimpleCursorAdapter createAdapter() {
        SimpleCursorAdapter adapter = new AdapterHelper(this).getAdapter();
        return adapter;
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Выбор региона");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        boolean value = lvMain.getCheckedItemPositions().get(position);
        if (value) {
            list.add(id);
        } else {
            list.remove(id);
        }
        setSubTitle();
    }

    private void setSubTitle() {
        if (list.isEmpty()) {
            toolbar.setSubtitle("");
        } else {
            toolbar.setSubtitle(list.size() + "/ " + COUNT_REGION);
        }
    }

    public void onClick(View view) {
        if (!list.isEmpty()) {
            db.updateTableRegion(list);
        }
        finish();
    }

    // ================================== МЕНЮ =====================================================


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_region, menu);

        final MenuItem actionSearchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) actionSearchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
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

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    // ===================================== LOADER ================================================

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        loader = new MyLoader(this, db);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        this.idIndex = data.getColumnIndex("_id");
        adapter.swapCursor(data);
        if (!isInit) {
            initListChoice();
        }
        initChoiceItem();
    }

    // Инициализируем список первыми значениями
    private void initListChoice() {
        for (int i = 0; i < lvMain.getCount(); i++) {
            list.add(lvMain.getItemIdAtPosition(i));
        }
        setSubTitle();
        isInit = true;
    }

    // Разукрашиваем элементы, которые были выбраны раньше
    private void initChoiceItem() {
        for (int i = 0; i < lvMain.getCount(); i++) {
            if (list.contains(lvMain.getItemIdAtPosition(i))) {
                lvMain.setItemChecked(i, true);
            } else {
                lvMain.setItemChecked(i, false);
            }
        }
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
            return db.getRegionCursor(pattern);
        }
    }


    // ==================================== ADAPTER ================================================

    class AdapterHelper {

        int layout = R.layout.adapter_region_item;
        String[] from = new String[] {"region", "country"};
        int[] to = new int[] {R.id.tvRegion, R.id.tvCountry};

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
