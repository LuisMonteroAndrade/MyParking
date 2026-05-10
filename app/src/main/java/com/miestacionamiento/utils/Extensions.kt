package com.miestacionamiento.utils

import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.miestacionamiento.R

fun View.visible() { visibility = View.VISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun ImageView.loadUrl(url: String) {
    Glide.with(this.context)
        .load(url)
        .transition(DrawableTransitionOptions.withCrossFade())
        .placeholder(R.drawable.bg_placeholder)
        .error(R.drawable.bg_placeholder)
        .centerCrop()
        .into(this)
}

fun Double.toCurrencyString(): String = "$${"%.0f".format(this)}/h"

fun Float.toStarString(): String = "%.1f".format(this)
