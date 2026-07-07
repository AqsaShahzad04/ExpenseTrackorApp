package com.et.expensetrackorapp

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.et.expensetrackorapp.databinding.FragmentExportBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.et.expensetrackorapp.data.local.AppDatabase
import com.et.expensetrackorapp.data.local.Expense
import com.et.expensetrackorapp.data.repository.ExpenseRepository
import com.et.expensetrackorapp.viewmodel.ExpenseViewModel
import com.et.expensetrackorapp.viewmodel.ExpenseViewModelFactory

class ExportFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentExportBinding? = null
    private val binding get() = _binding!!

    // the 4 ranges, with the label shown on each pill
    enum class DateRange(val label: String) {
        THIS_MONTH("This month"),
        LAST_30("Last 30 days"),
        LAST_7("Last 7 days"),
        ALL_TIME("All time")
    }

    // which range is currently picked (default: this month)
    private var selectedRange = DateRange.THIS_MONTH
    private val expenseViewModel: ExpenseViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).expenseDao()
        ExpenseViewModelFactory(ExpenseRepository(dao))
    }

    // all expenses, kept fresh; summary filters from this
    private var allExpenses: List<Expense> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnClickListener { dismiss() }

        buildRangePills()

    }
    private fun buildRangePills() {
        binding.rangeContainer.removeAllViews()

        val density = resources.displayMetrics.density

        for (range in DateRange.values()) {
            val pill = TextView(requireContext()).apply {
                text = range.label
                textSize = 13f
                gravity = Gravity.CENTER

                // padding inside the pill (dp → px)
                val padH = (16 * density).toInt()
                val padV = (9 * density).toInt()
                setPadding(padH, padV, padH, padV)

                // spacing between pills
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.marginEnd = (8 * density).toInt()
                layoutParams = lp

                setOnClickListener {
                    selectedRange = range
                    styleRangePills()
                    setOnClickListener {
                        selectedRange = range
                        styleRangePills()
                        updateSummary()
                    }// restyle so only this one looks selected
                }
            }
            binding.rangeContainer.addView(pill)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                expenseViewModel.allExpenses.collect { list ->
                    allExpenses = list
                    updateSummary()
                }
            }
        }

        styleRangePills()   // set the initial selected look
    }

    // returns only the expenses inside the selected range
    private fun filteredExpenses(): List<Expense> {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance()

        return when (selectedRange) {
            DateRange.ALL_TIME -> allExpenses

            DateRange.THIS_MONTH -> {
                val key = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
                allExpenses.filter { it.date.startsWith(key) }
            }

            DateRange.LAST_7, DateRange.LAST_30 -> {
                val days = if (selectedRange == DateRange.LAST_7) 7 else 30
                val cutoff = (today.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, -days)
                }.time
                allExpenses.filter {
                    try { fmt.parse(it.date)?.after(cutoff) == true } catch (e: Exception) { false }
                }
            }
        }
    }

    private fun updateSummary() {
        val list = filteredExpenses()
        val total = list.sumOf { it.amount }
        binding.summaryCount.text = "${list.size} transactions"
        binding.summaryRange.text = selectedRange.label
        binding.summaryTotal.text = "Rs. " + "%,.0f".format(total)
    }

    // walks every pill and gives it the selected or unselected look
    private fun styleRangePills() {
        for (i in 0 until binding.rangeContainer.childCount) {
            val pill = binding.rangeContainer.getChildAt(i) as TextView
            val isSelected = pill.text == selectedRange.label
            if (isSelected) {
                pill.setBackgroundResource(R.drawable.bg_pill_range_selected)
                pill.setTextColor(resources.getColor(R.color.white, null))
            } else {
                pill.setBackgroundResource(R.drawable.bg_pill_range_unselected)
                pill.setTextColor(resources.getColor(R.color.black, null))
            }
        }
    }


    override fun getTheme(): Int = R.style.RoundedBottomSheet

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}