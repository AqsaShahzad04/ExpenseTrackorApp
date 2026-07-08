package com.et.expensetrackorapp

import com.et.expensetrackorapp.data.local.Expense

data class HistoryGroup(
    val label: String,
    val expenses: List<Expense>
)