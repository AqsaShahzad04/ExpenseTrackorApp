package com.et.expensetrackorapp.util

import android.content.Context

// Formats money using the currency symbol saved in SharedPreferences.
// Amounts stay as-is (no conversion) — SRS says symbol swap only.
object CurrencyFormatter {

    fun symbol(context: Context): String {
        val prefs = context.getSharedPreferences("app_prefs", 0)
        return prefs.getString("currency_symbol", "Rs.") ?: "Rs."
    }

    // e.g. "$ 1,500" or "Rs. 1,500"
    fun format(context: Context, amount: Double): String {
        return "${symbol(context)} ${"%,.0f".format(amount)}"
    }
}