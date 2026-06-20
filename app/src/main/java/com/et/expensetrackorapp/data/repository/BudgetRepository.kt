package com.et.expensetrackorapp.data.repository

import com.et.expensetrackorapp.data.local.Budget
import com.et.expensetrackorapp.data.local.BudgetDao
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {

    fun getBudgetForMonth(month: String): Flow<Budget?> {
        return budgetDao.getBudgetForMonth(month)
    }

    suspend fun setBudget(budget: Budget) {
        budgetDao.setBudget(budget)
    }
}