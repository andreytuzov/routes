package ru.railway.dc.routes

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.GridView
import io.reactivex.Maybe
import io.reactivex.MaybeOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.railway.dc.routes.adapters.ImageAdapter
import ru.railway.dc.routes.database.assets.photos.AssetsPhotoDB
import ru.railway.dc.routes.database.assets.photos.Image
import ru.railway.dc.routes.utils.ToastUtils

class ImageActivity : AppCompatActivity() {

    private lateinit var assetsPhotoDB: AssetsPhotoDB
    private lateinit var adapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_activity)
        initToolbar()
        // Get name of station
        val gridView = findViewById(R.id.gridView) as GridView
        adapter = ImageAdapter(this, mutableListOf())
        gridView.adapter = adapter

        // Get information
        assetsPhotoDB = AssetsPhotoDB(this)
        assetsPhotoDB.open()

        val stationName = intent.getStringExtra(PARAM_STATION_NAME)
        if (stationName != null)
            startSearchImage(stationName)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun loadImageObservable(pattern: String) =
            Maybe.create(MaybeOnSubscribe<List<Image>?> {
                val data = assetsPhotoDB.getPhotoList(pattern)
                if (data != null)
                    it.onSuccess(data)
            }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())

    private fun startSearchImage(pattern: String) {
        loadImageObservable(pattern)
                .subscribe {
                    if (it != null)
                        adapter.swapData(it)
                }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_image, menu)

        val actionSearchItem = menu.findItem(R.id.action_search)
        val searchView = actionSearchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                actionSearchItem.collapseActionView()
                startSearchImage(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun initToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        assetsPhotoDB.close()
    }

    companion object {
        const val PARAM_STATION_NAME = "stationName"
    }

}