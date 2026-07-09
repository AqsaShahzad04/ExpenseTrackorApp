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

    // current filter/sort choices (defaults)
    private var currentSort = "Newest"
    private var currentRange = "All time"
    private var currentCategory = "All"

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

        // open the filter/sort sheet, passing current choices
        binding.btnFilter.setOnClickListener {
            FilterSortFragment.newInstance(currentSort, currentRange, currentCategory)
                .show(parentFragmentManager, "filter_sort")
        }

        // receive Apply result → store choices → refresh list
        parentFragmentManager.setFragmentResultListener(
            "filter_sort_result", viewLifecycleOwner
        ) { _, bundle ->
            currentSort = bundle.getString("sort") ?: "Newest"
            currentRange = bundle.getString("range") ?: "All time"
            currentCategory = bundle.getString("category") ?: "All"
            showFiltered(binding.editSearch.text.toString())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allExpenses.collect { expenses ->
                    allItems = expenses
                    showFiltered(binding.editSearch.text.toString())
                    val total = expenses.sumOf { it.amount }
                    binding.textHistoryTotal.text = com.et.expensetrackorapp.util.CurrencyFormatter.format(requireContext(), total)
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
        var list = allItems

        // 1) search text
        if (query.isNotBlank()) {
            list = list.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
            }
        }

        // 2) category
        if (currentCategory != "All") {
            list = list.filter { it.category == currentCategory }
        }

        // 3) date range
        list = filterByRange(list, currentRange)

        // 4) sort
        list = when (currentSort) {
            "Oldest"  -> list.sortedBy { it.date }
            "Highest" -> list.sortedByDescending { it.amount }
            "Lowest"  -> list.sortedBy { it.amount }
            else      -> list.sortedByDescending { it.date } // Newest
        }

        adapter.updateData(groupByDate(list))
    }

    // keep only expenses whose date falls in the chosen window
    private fun filterByRange(list: List<Expense>, range: String): List<Expense> {
        if (range == "All time") return list
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = java.util.Calendar.getInstance()

        val start = java.util.Calendar.getInstance()
        when (range) {
            "This month" -> {
                start.set(java.util.Calendar.DAY_OF_MONTH, 1)
            }
            "Last 30 days" -> start.add(java.util.Calendar.DAY_OF_YEAR, -30)
            "Last 7 days"  -> start.add(java.util.Calendar.DAY_OF_YEAR, -7)
            else -> return list
        }

        return list.filter {
            try {
                val d = fmt.parse(it.date) ?: return@filter false
                val cal = java.util.Calendar.getInstance().apply { time = d }
                !cal.before(start) && !cal.after(today)
            } catch (e: Exception) {
                false
            }
        }
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