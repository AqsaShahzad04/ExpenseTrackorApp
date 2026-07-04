package com.et.expensetrackorapp.viewmodel

import com.et.expensetrackorapp.data.local.Expense
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// One row in the "By category" list
data class CategoryStat(
    val category: String,
    val total: Double,
    val percent: Int
)

// One bar in the "Last 14 days" chart
data class DayStat(
    val label: String,   // e.g. "Jun 17"
    val total: Double
)

// One bar in the "Month over month" chart
data class MonthStat(
    val label: String,   // e.g. "May"
    val total: Double
)
object StatsCalculator {

    // Summary 1 + 2: total per category, sorted big to small, with %
    fun categoryStats(expenses: List<Expense>): List<CategoryStat> {
        val grandTotal = expenses.sumOf { it.amount }
        if (grandTotal == 0.0) return emptyList()

        return expenses
            .groupBy { it.category }
            .map { (category, list) ->
                val total = list.sumOf { it.amount }
                val percent = Math.round(total / grandTotal * 100).toInt()
                CategoryStat(category, total, percent)
            }
            .sortedByDescending { it.total }
    }

    // Summary 3: total per day for the last 14 days (oldest first)
    fun last14Days(expenses: List<Expense>): List<DayStat> {
        val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val labelFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val result = mutableListOf<DayStat>()

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -13) // 13 days ago + today = 14 days

        repeat(14) {
            val key = keyFormat.format(calendar.time)
            val label = labelFormat.format(calendar.time)
            val dayTotal = expenses
                .filter { it.date == key }
                .sumOf { it.amount }
            result.add(DayStat(label, dayTotal))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return result
    }

    // Totals for the last 3 months, oldest first (e.g. May, Jun, Jul)
    fun last3Months(expenses: List<Expense>): List<MonthStat> {
        val keyFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val labelFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val result = mutableListOf<MonthStat>()

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -2) // 2 months ago + this month = 3 months

        repeat(3) {
            val key = keyFormat.format(calendar.time)     // "2026-05"
            val label = labelFormat.format(calendar.time) // "May"
            val monthTotal = expenses
                .filter { it.date.startsWith(key) }
                .sumOf { it.amount }
            result.add(MonthStat(label, monthTotal))
            calendar.add(Calendar.MONTH, 1)
        }
        return result
    }
}