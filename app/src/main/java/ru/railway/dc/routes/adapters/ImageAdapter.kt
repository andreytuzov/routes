package ru.railway.dc.routes.adapters

import android.content.Context
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import com.facebook.drawee.view.SimpleDraweeView
import com.stfalcon.frescoimageviewer.ImageViewer
import ru.railway.dc.routes.R
import ru.railway.dc.routes.database.assets.photos.Image

class ImageAdapter(val context: Context, var data: List<Image>) : BaseAdapter() {

    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getItem(position: Int) = data[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = data.size

    fun swapData(data: List<Image>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val holder: ViewHolder
        if (convertView == null) {
            view = inflater.inflate(R.layout.item_grid_image_station, null)
            holder = ViewHolder(view)
            view.tag = holder
        } else
            holder = convertView.tag as ViewHolder


        holder.image.setOnClickListener {
            ImageViewer.Builder(context, data.map { it.url.replace("_s", "") })
                    .setStartPosition(position)
                    .setImageChangeListener { Toast.makeText(context, data[it].description, Toast.LENGTH_LONG).show() }
                    .show()
        }

        val item = data[position]
        holder.image.setImageURI(item.url)
        holder.description.text = item.description
        return view!!
    }


    private class ViewHolder(view: View) {
        val image = view.findViewById(R.id.img) as SimpleDraweeView
        val description = view.findViewById(R.id.description) as TextView
    }

    companion object {

    }
}