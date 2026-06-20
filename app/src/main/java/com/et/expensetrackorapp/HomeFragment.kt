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

    private val adapter = ExpenseAdaptor(emptyList())

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

        binding.fabAdd.setOnClickListener {
            AddEditFragment().show(parentFragmentManager, "AddEditSheet")
        }

        binding.recyclerExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerExpenses.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allExpenses.collect { expenses ->
                    adapter.updateData(expenses.take(5))

                    val currentMonth = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
                        .format(java.util.Date())

                    val spentThisMonth = expenses
                        .filter { it.date.startsWith(currentMonth) }
                        .sumOf { it.amount }

                    binding.textSpentThisMonth.text = "Rs. ${"%,.0f".format(spentThisMonth)}"
                }
            }
        }


    }

}