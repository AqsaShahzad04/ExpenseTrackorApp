package com.et.expensetrackorapp.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.et.expensetrackorapp.data.local.Expense
import com.et.expensetrackorapp.data.repository.ExpenseRepository
import kotlinx.coroutines.launch

class ExpenseViewModel(private val repository: ExpenseRepository): ViewModel() {

    val allExpenses=repository.allExpenses
    fun insert(expense: Expense){
        viewModelScope.launch{
          repository.insert(expense)
        }
    }

    fun update(expense: Expense){
        viewModelScope.launch{
            repository.update(expense)
        }
    }

    fun delete(expense: Expense){
        viewModelScope.launch{
            repository.delete(expense)
        }
    }
}