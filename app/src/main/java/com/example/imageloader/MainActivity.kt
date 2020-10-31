package com.example.imageloader

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_image_full_screen.view.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.loadable_image_view.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

const val EXTRA_MESSAGE = "com.example.URL"

class MainActivity : AppCompatActivity() {
    val url = "https://api.unsplash.com/photos/?client_id=cb0327cd21a9039aa6b7c5a2162b5819e031575ffe87873e4d23a847ab362103"

    var imageSrcList : MutableList<ImageSrc> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val onClick: (ImageSrc) -> Unit = {
            println(it.url)


            val intent = Intent(this, ImageFullScreenActivity::class.java).apply {
                putExtra(EXTRA_MESSAGE, it.url)
            }
            startActivity(intent)
        }

        images_list.adapter = ImageSrcAdapter(items = imageSrcList, ctx = this, onClick = onClick)

        getImagesSrc()
    }

    fun updateData(data: String?) {
        println(data!!)

        val json = JSONArray(data)

        for (i in 0 until json.length()) {
            val item = json.getJSONObject(i) as JSONObject

            val description = (item["user"] as JSONObject)["username"] as String
            val url = (item["urls"] as JSONObject)["regular"] as String

            val imageSrc = ImageSrc(url, description)

            imageSrcList.add(imageSrc)
        }

        images_list.adapter?.notifyDataSetChanged()
    }

    fun getImagesSrc() {
        class ImagesSrcLoader(activity: MainActivity): AsyncTask<String, Unit, String>() {
            private val activityRef = WeakReference(activity)
            override fun doInBackground(vararg params: String?): String {
                return URL(params[0])
                    .openConnection()
                    .getInputStream()
                    .reader()
                    .readText()
            }

            override fun onPostExecute(result: String?) {
                val activity = activityRef.get()
                activity?.updateData(result)
            }
        }

        ImagesSrcLoader(this).execute(url)
    }



    data class ImageSrc(val url: String, val description: String)

    class ImageSrcAdapter(items:List<ImageSrc>, ctx: Context, val onClick: (ImageSrc) -> Unit) : RecyclerView.Adapter<ImageSrcAdapter.ViewHolder>(){

        var list = items
        var context = ctx

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageSrcAdapter.ViewHolder {
            val holder = ViewHolder(LayoutInflater.from(context).inflate(R.layout.loadable_image_view,parent,false))
            holder.root.setOnClickListener {
                onClick(list[holder.adapterPosition])
            }
            return holder
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ImageSrcAdapter.ViewHolder, position: Int) {
            holder.bind(list[position])
        }

        class ViewHolder(val root: View) : RecyclerView.ViewHolder(root) {
            fun bind(imageSrc: ImageSrc) {
                with(root) {
                    val loadableImage = root as LoadableImageView
                    loadableImage.setup(imageSrc)
                }
            }
        }

    }
}

class LoadableImageView: RelativeLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var url = ""
    var imageLoader = ImageLoader({},"")

    fun setup(imageSrc: MainActivity.ImageSrc) {
        textViewCell.setText(imageSrc.description)

        imageLoader.cancel(true)

        progressBar.setVisibility(View.VISIBLE)
        imageView2.setImageResource(0)

        this.url = imageSrc.url

        println(this.url)

        this.imageLoader = ImageLoader({
            imageView2.setImageBitmap(it)
            progressBar.setVisibility(View.INVISIBLE)
        }, this.url)

        imageLoader.execute()
    }

}

class ImageLoader(val callback: (Bitmap) -> Unit, val url: String): AsyncTask<Void, Void, Bitmap>() {
    override fun doInBackground(vararg params: Void?): Bitmap {
        val urlForConnection = URL(url)
        val conncetion  = urlForConnection.openConnection() as HttpURLConnection
        conncetion.doInput = true
        conncetion.connect()

        val inputStream = conncetion.inputStream

        SystemClock.sleep(TimeUnit.SECONDS.toMillis(1))

        return BitmapFactory.decodeStream(inputStream)
    }

    override fun onPostExecute(result: Bitmap?) {
        if (result != null) {
            callback(result)
        }
    }
}