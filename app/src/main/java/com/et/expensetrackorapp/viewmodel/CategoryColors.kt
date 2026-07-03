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
}