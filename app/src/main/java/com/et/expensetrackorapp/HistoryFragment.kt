package com.et.expensetrackorapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.et.expensetrackorapp.databinding.FragmentHistoryBinding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.et.expensetrackorapp.data.local.AppDatabase
import com.et.expensetrackorapp.data.repository.ExpenseRepository
import com.et.expensetrackorapp.viewmodel.ExpenseViewModel
import com.et.expensetrackorapp.viewmodel.ExpenseViewModelFactory
import com.et.expensetrackorapp.data.local.Expense
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExpenseViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).expenseDao()
        val repository = ExpenseRepository(dao)
        ExpenseViewModelFactory(repository)
    }

    private val adapter = HistoryAdaptor(emptyList()) { expense ->
        AddEditFragment.newInstance(expense).show(parentFragmentManager, "edit")
    }
    private var allItems: List<Expense> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHistory.adapter = adapter

        binding.editSearch.addTextChangedListener { text ->
            showFiltered(text.toString())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allExpenses.collect { expenses ->
                    allItems = expenses
                    showFiltered(binding.editSearch.text.toString())
                    val total = expenses.sumOf { it.amount }
                    binding.textHistoryTotal.text = "Rs. ${"%,.0f".format(total)}"
                }
            }
        }

    }
    private fun groupByDate(expenses: List<Expense>): List<HistoryGroup> {
        val grouped = expenses.groupBy { it.date }.toSortedMap(reverseOrder())
        return grouped.map { (date, list) ->
            HistoryGroup(formatDateLabel(date), list)
        }
    }
    private fun showFiltered(query: String) {
        val filtered = if (query.isBlank()) {
            allItems
        } else {
            allItems.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
            }
        }
        adapter.updateData(groupByDate(filtered))
    }
    private fun formatDateLabel(date: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsed = parser.parse(date)
            val formatter = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
            formatter.format(parsed!!).uppercase()
        } catch (e: Exception) {
            date
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}