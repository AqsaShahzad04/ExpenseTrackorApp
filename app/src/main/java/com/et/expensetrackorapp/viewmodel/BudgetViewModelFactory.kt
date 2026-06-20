package com.et.expensetrackorapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.et.expensetrackorapp.data.repository.BudgetRepository

class BudgetViewModelFactory(
    private val repository: BudgetRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BudgetViewModel(repository) as T
    }
}