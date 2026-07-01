package com.et.expensetrackorapp.viewmodel

import com.et.expensetrackorapp.R

object CategoryIcons {

    // One drawable per category. Row icons AND the Add/Edit grid both read this.
    val map: Map<String, Int> = linkedMapOf(
        "Shopping"      to R.drawable.ic_shopping,
        "Bills"         to R.drawable.ic_bills,
        "Food & Drink"  to R.drawable.ic_foodanddrink,
        "Entertainment" to R.drawable.ic_entertainment,
        "Transport"     to R.drawable.ic_transport,
        "Health"        to R.drawable.ic_health,
        "Education"     to R.drawable.ic_education,
        "Coffee"        to R.drawable.ic_coffee,
        "Other"         to R.drawable.ic_other
    )

    // Fallback icon if a category isn't found
    fun iconFor(category: String): Int =
        map[category] ?: R.drawable.ic_other
}