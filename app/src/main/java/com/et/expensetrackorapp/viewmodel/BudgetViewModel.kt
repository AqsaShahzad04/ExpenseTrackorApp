package com.et.expensetrackorapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.et.expensetrackorapp.data.local.Budget
import com.et.expensetrackorapp.data.repository.BudgetRepository
import kotlinx.coroutines.launch

class BudgetViewModel(private val repository: BudgetRepository) : ViewModel() {

    fun getBudgetForMonth(month: String) = repository.getBudgetForMonth(month)

    fun setBudget(budget: Budget) {
        viewModelScope.launch {
            repository.setBudget(budget)
        }
    }
}