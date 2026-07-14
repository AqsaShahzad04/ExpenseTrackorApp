package com.et.expensetrackorapp.viewmodel

import android.graphics.Color

object CategoryColors {

    // One fixed color per category. Ring arcs AND legend pills both read this.
    val map: Map<String, Int> = linkedMapOf(
        "Shopping"      to Color.parseColor("#A04CEB"),
        "Bills"         to Color.parseColor("#5E6A7A"),
        "Food & Drink"  to Color.parseColor("#E46344"),
        "Entertainment" to Color.parseColor("#DF4187"),
        "Transport"     to Color.parseColor("#497DDC"),
        "Health"        to Color.parseColor("#2DAE7C"),
        "Education"     to Color.parseColor("#269EBA"),
        "Coffee"        to Color.parseColor("#AD7524"),
        "Other"         to Color.parseColor("#676766")
    )

    // Fallback color for any category not in the map
    fun colorFor(category: String): Int =
        map[category] ?: Color.parseColor("#676766")

    // Lightest tint of the category color — same hue, low alpha. Used for the icon circle bg.
    fun lightBgFor(category: String): Int {
        val base = colorFor(category)
        return Color.argb(20, Color.red(base), Color.green(base), Color.blue(base))
    }

    fun tileBgFor(category: String): Int {
        val base = colorFor(category)
        val ratio = 0.15f
        val r = (Color.red(base) * ratio + 255 * (1 - ratio)).toInt()
        val g = (Color.green(base) * ratio + 255 * (1 - ratio)).toInt()
        val b = (Color.blue(base) * ratio + 255 * (1 - ratio)).toInt()
        return Color.rgb(r, g, b)
    }
}