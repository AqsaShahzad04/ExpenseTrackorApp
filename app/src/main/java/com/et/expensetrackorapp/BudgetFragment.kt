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
                        val percent = if (budget.amount > 0)
                            (spentThisMonth / budget.amount * 100).toInt()
                        else 0

                        binding.budgetRing.progress = percent.coerceAtMost(100)
                        binding.textBudgetPercent.text = percent.coerceAtMost(100).toString()
                        binding.textBudgetRemaining.text = "Rs. ${"%,.0f".format(remaining)}"

                        val calendar = java.util.Calendar.getInstance()
                        val dayOfMonth = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                        binding.textPacingLabel.text = "By day $dayOfMonth of $daysInMonth, expected"

                        val expected = budget.amount * dayOfMonth / daysInMonth
                        binding.textPacingExpected.text = "Rs. ${"%,.0f".format(expected)}"
                        val delta = expected - spentThisMonth

                        if (delta >= 0) {
                            binding.textPacingStatus.text = "Under pace"
                            binding.textPacingStatus.setTextColor(android.graphics.Color.parseColor("#D9822B"))
                            binding.textPacingDelta.text = "-Rs. ${"%,.0f".format(delta)}"
                        } else {
                            binding.textPacingStatus.text = "Over pace"
                            binding.textPacingStatus.setTextColor(android.graphics.Color.parseColor("#D32F2F"))
                            binding.textPacingDelta.text = "+Rs. ${"%,.0f".format(-delta)}"
                        }

                        if (spentThisMonth > budget.amount) {
                            binding.textOverBudgetBanner.visibility = View.VISIBLE
                        } else {
                            binding.textOverBudgetBanner.visibility = View.GONE
                        }

                    } else {

                        binding.textMonthlyLimit.text = "Rs. 0"
                        binding.textBudgetRemaining.text = "Rs. 0"
                        binding.budgetRing.progress = 0
                        binding.textBudgetPercent.text = "0"
                        binding.textPacingLabel.text = "By day —, expected"
                        binding.textPacingExpected.text = "Rs. 0"
                        binding.textPacingStatus.text = ""
                        binding.textPacingDelta.text = "Rs. 0"
                        binding.textOverBudgetBanner.visibility = View.GONE
                    }
                }
            }
        }
        binding.btnUpdateBudget.setOnClickListener {
            UpdateBudgetFragment().show(parentFragmentManager, "UpdateBudgetSheet")
        }
    }
}