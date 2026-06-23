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
    val normalized = normalizeImageUrl(url)
    Glide.with(this.context)
        .load(normalized.ifEmpty { null })
        .transition(DrawableTransitionOptions.withCrossFade())
        .placeholder(R.drawable.bg_placeholder)
        .error(R.drawable.bg_placeholder)
        .centerCrop()
        .into(this)
}

private const val SERVER_BASE = "http://161.153.192.177:3000"

fun normalizeImageUrl(url: String): String {
    if (url.isBlank()) return ""
    // URLs de Unsplash u otros dominios conocidos: se usan tal cual
    if (url.startsWith("https://")) return url
    // Caso: "undefined/uploads/..." — el backend devolvió undefined como base
    if (url.startsWith("undefined/")) {
        return "$SERVER_BASE/${url.removePrefix("undefined/")}"
    }
    // Caso: IP local de desarrollo "http://192.168.x.x:3000/uploads/..."
    if (url.startsWith("http://192.168.") || url.startsWith("http://10.")) {
        val path = url.substringAfter("/uploads/", "")
        return if (path.isNotEmpty()) "$SERVER_BASE/uploads/$path" else url
    }
    // Caso: ruta relativa "/uploads/..."
    if (url.startsWith("/uploads/")) return "$SERVER_BASE$url"
    // Caso: ruta sin barra inicial "uploads/..."
    if (url.startsWith("uploads/")) return "$SERVER_BASE/$url"
    // URL completa correcta
    return url
}

fun Double.toCurrencyString(): String = "$${"%.0f".format(this)}/h"

fun Float.toStarString(): String = "%.1f".format(this)
