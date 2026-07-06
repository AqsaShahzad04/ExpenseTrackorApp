package com.et.expensetrackorapp.data.repository

import com.et.expensetrackorapp.data.local.Expense
import com.et.expensetrackorapp.data.local.ExpenseDao
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    suspend fun insert(expense: Expense) {
        expenseDao.insert(expense)
    }

    suspend fun update(expense: Expense) {
        expenseDao.update(expense)
    }

    suspend fun delete(expense: Expense) {
        expenseDao.delete(expense)
    }

    suspend fun deleteAll() {
        expenseDao.deleteAll()
    }
}