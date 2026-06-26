package com.et.expensetrackorapp

import com.et.expensetrackorapp.data.local.Expense

sealed class HistoryItem {
    data class Header(val label: String) : HistoryItem()
    data class ExpenseRow(val expense: Expense) : HistoryItem()
}