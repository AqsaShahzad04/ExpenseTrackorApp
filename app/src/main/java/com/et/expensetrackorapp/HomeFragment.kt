package com.et.expensetrackorapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.et.expensetrackorapp.data.local.AppDatabase
import com.et.expensetrackorapp.data.repository.ExpenseRepository
import com.et.expensetrackorapp.databinding.FragmentHomeBinding
import com.et.expensetrackorapp.viewmodel.ExpenseViewModel
import com.et.expensetrackorapp.viewmodel.ExpenseViewModelFactory
import androidx.recyclerview.widget.LinearLayoutManager
import com.et.expensetrackorapp.ExpenseAdaptor
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.launch
import com.et.expensetrackorapp.data.local.Budget
import com.et.expensetrackorapp.data.repository.BudgetRepository
import com.et.expensetrackorapp.viewmodel.BudgetViewModel
import com.et.expensetrackorapp.viewmodel.BudgetViewModelFactory
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.combine
import android.widget.LinearLayout

class HomeFragment: Fragment() {

    private var _binding: FragmentHomeBinding?=null
    private val binding get()=_binding!!

    private val viewModel: ExpenseViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).expenseDao()
        val repository = ExpenseRepository(dao)
        ExpenseViewModelFactory(repository)
    }

    private val budgetViewModel: BudgetViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).budgetDao()
        val repository = BudgetRepository(dao)
        BudgetViewModelFactory(repository)
    }

    private val adapter = ExpenseAdaptor(emptyList()) {
        findNavController().navigate(R.id.action_home_to_history)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.cardBudget.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_budget)
        }
        binding.textSeeAll.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_history)
        }

        binding.recyclerExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerExpenses.adapter = adapter

        val currentMonth = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
            .format(java.util.Date())

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Combine the two live streams into one.
                // Fires whenever EITHER expenses or the budget changes.
                combine(
                    viewModel.allExpenses,
                    budgetViewModel.getBudgetForMonth(currentMonth)
                ) { expenses, budget ->
                    // Bundle both results together to handle below
                    Pair(expenses, budget)
                }.collect { (expenses, budget) ->

                    adapter.updateData(expenses.take(5))

                    val monthExpenses = expenses.filter { it.date.startsWith(currentMonth) }

                    val spentThisMonth = monthExpenses.sumOf { it.amount }
                    binding.textSpentThisMonth.text = "Rs. ${"%,.0f".format(spentThisMonth)}"

                    val txnCount = monthExpenses.size
                    binding.textTxns.text = txnCount.toString()
                    binding.textTxnsLabel.text = "$txnCount txns"

                    val categoryCount = monthExpenses.map { it.category }.distinct().size
                    binding.textCategories.text = categoryCount.toString()

                    val dayOfMonth = java.util.Calendar.getInstance()
                        .get(java.util.Calendar.DAY_OF_MONTH)
                    val dailyAvg = if (dayOfMonth > 0) spentThisMonth / dayOfMonth else 0.0
                    binding.textDailyAvg.text = "Rs. ${"%,.0f".format(dailyAvg)}"

                    // ---- Budget text + progress bar ----
                    val budgetAmount = budget?.amount ?: 0.0

                    if (budgetAmount > 0) {

                        binding.textBudgetValue.text = "Rs. ${"%,.0f".format(budgetAmount)}"

                        // fraction of budget used, capped at 1.0 (100%)
                        val fraction = (spentThisMonth / budgetAmount).coerceIn(0.0, 1.0)

                        val percent = (fraction * 100).toInt()
                        binding.textPercentOfBudget.text =
                            "$percent% of Rs. ${"%,.0f".format(budgetAmount)}"

                        val remaining = budgetAmount - spentThisMonth
                        binding.textRemaining.text = "Rs. ${"%,.0f".format(remaining)} left"

                        binding.viewProgressFill.layoutParams =
                            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, fraction.toFloat())
                        binding.viewProgressEmpty.layoutParams =
                            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, (1.0 - fraction).toFloat())
                    } else {
                        binding.textBudgetValue.text = "Not set"
                        binding.textPercentOfBudget.text = ""
                        binding.textRemaining.text = ""
                        // no budget → empty bar
                        binding.viewProgressFill.layoutParams =
                            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f)
                        binding.viewProgressEmpty.layoutParams =
                            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                    }
                }
            }
        }
    }

}