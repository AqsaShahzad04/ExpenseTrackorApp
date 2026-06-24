package com.et.expensetrackorapp
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.launch
import com.et.expensetrackorapp.data.local.AppDatabase
import com.et.expensetrackorapp.data.repository.BudgetRepository
import com.et.expensetrackorapp.databinding.FragmentBudgetBinding
import com.et.expensetrackorapp.viewmodel.BudgetViewModel
import com.et.expensetrackorapp.viewmodel.BudgetViewModelFactory
import com.et.expensetrackorapp.data.repository.ExpenseRepository
import com.et.expensetrackorapp.viewmodel.ExpenseViewModel
import com.et.expensetrackorapp.viewmodel.ExpenseViewModelFactory
import kotlinx.coroutines.flow.combine

class BudgetFragment: Fragment() {

    private var _binding: FragmentBudgetBinding?=null
    private val binding get()=_binding!!

    private val budgetViewModel: BudgetViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).budgetDao()
        val repository = BudgetRepository(dao)
        BudgetViewModelFactory(repository)
    }
    private val expenseViewModel: ExpenseViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).expenseDao()
        val repository = ExpenseRepository(dao)
        ExpenseViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val currentMonth = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
            .format(java.util.Date())


        val monthLabel = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
            .format(java.util.Date())
        binding.textBudgetMonth.text = monthLabel

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                combine(
                    expenseViewModel.allExpenses,
                    budgetViewModel.getBudgetForMonth(currentMonth)
                ) { expenses, budget ->
                    Pair(expenses, budget)
                }.collect { (expenses, budget) ->

                    // Spent this month, from the expenses stream
                    val spentThisMonth = expenses
                        .filter { it.date.startsWith(currentMonth) }
                        .sumOf { it.amount }

                    binding.textBudgetSpent.text = "Rs. ${"%,.0f".format(spentThisMonth)}"

                    if (budget != null) {
                        binding.textMonthlyLimit.text = "Rs. ${"%,.0f".format(budget.amount)}"

                        val remaining = budget.amount - spentThisMonth
                        binding.textBudgetRemaining.text = "Rs. ${"%,.0f".format(remaining)}"
                    } else {

                        binding.textMonthlyLimit.text = "Rs. 0"
                        binding.textBudgetRemaining.text = "Rs. 0"
                    }
                }
            }
        }
        binding.btnUpdateBudget.setOnClickListener {
            UpdateBudgetFragment().show(parentFragmentManager, "UpdateBudgetSheet")
        }
    }
}