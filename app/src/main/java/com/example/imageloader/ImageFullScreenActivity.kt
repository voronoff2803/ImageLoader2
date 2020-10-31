package com.example.imageloader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_image_full_screen.*
import kotlinx.android.synthetic.main.loadable_image_view.*
import kotlinx.android.synthetic.main.loadable_image_view.view.*

class ImageFullScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_full_screen)

        val url = intent.getStringExtra(EXTRA_MESSAGE)

        val imageLoader = url?.let {
            ImageLoader({
                imageView.setImageBitmap(it)
                progressBar2.setVisibility(View.INVISIBLE)
            }, it)
        }

        imageLoader?.execute()
    }
}