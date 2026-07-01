package com.et.expensetrackorapp.viewmodel

import android.graphics.Color

object CategoryColors {

    // One fixed color per category. Ring arcs AND legend pills both read this.
    val map: Map<String, Int> = linkedMapOf(
        "Shopping"      to Color.parseColor("#0a5747"),
        "Bills"         to Color.parseColor("#3A3A3A"),
        "Food & Drink"  to Color.parseColor("#E8722C"),
        "Entertainment" to Color.parseColor("#D14F8B"),
        "Transport"     to Color.parseColor("#3F9BD1"),
        "Health"        to Color.parseColor("#3a5c2f"),
        "Education"     to Color.parseColor("#cfcf5b"),
        "Coffee"        to Color.parseColor("#9C6B3F"),
        "Other"         to Color.parseColor("#9A9387")
    )

    // Fallback color for any category not in the map
    fun colorFor(category: String): Int =
        map[category] ?: Color.parseColor("#9A9387")
}