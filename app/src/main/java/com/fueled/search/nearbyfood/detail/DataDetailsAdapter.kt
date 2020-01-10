package com.fueled.search.nearbyfood.detail

import android.widget.ImageButton
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.fueled.search.nearbyfood.R


/**
 * Created by Kiran
 */
object DataDetailsAdapter {

    @JvmStatic
    @BindingAdapter("mapImageUrl")
    fun bindStaticMap(imageView: ImageView, url: String) {
        if (url.isNotEmpty()) {
            val circularProgressDrawable = CircularProgressDrawable(imageView.context)
            circularProgressDrawable.backgroundColor = R.color.colorAccent
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 60f
            circularProgressDrawable.start()

            Glide.with(imageView.context)
                .load(url)
                .placeholder(circularProgressDrawable)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)
        }
    }

    @JvmStatic
    @BindingAdapter("selected")
    fun bindFavoriteSate(imageButton: ImageButton, isSelected: Boolean) {
        imageButton.isSelected = isSelected
    }
}