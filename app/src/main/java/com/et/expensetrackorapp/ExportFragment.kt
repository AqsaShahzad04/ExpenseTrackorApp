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
import com.et.expensetrackorapp.data.repository.BudgetRepository
import com.et.expensetrackorapp.viewmodel.BudgetViewModel
import com.et.expensetrackorapp.viewmodel.BudgetViewModelFactory
import android.content.Intent
import androidx.core.content.FileProvider
import com.et.expensetrackorapp.util.CurrencyFormatter

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

    private var justExported = false
    private var exportedCount = 0
    // which format is picked (default: PDF)
    private var selectedFormat = "PDF"
    private val expenseViewModel: ExpenseViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).expenseDao()
        ExpenseViewModelFactory(ExpenseRepository(dao))
    }

    private val budgetViewModel: BudgetViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).budgetDao()
        BudgetViewModelFactory(BudgetRepository(dao))
    }

    // all expenses, kept fresh; summary filters from this
    private var allExpenses: List<Expense> = emptyList()
    private var currentBudget: Double? = null

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
        styleFormatCards()   // set initial PDF-selected look

        binding.cardPdf.setOnClickListener {
            selectedFormat = "PDF"
            styleFormatCards()
        }
        binding.cardCsv.setOnClickListener {
            selectedFormat = "CSV"
            styleFormatCards()
        }
        binding.btnExport.setOnClickListener {
            if (selectedFormat == "PDF") {
                exportPdf()
            }
            else{
                exportCsv()
            }

        }

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
                    updateSummary()
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
        val monthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                budgetViewModel.getBudgetForMonth(monthKey).collect { budget ->
                    currentBudget = budget?.amount
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
        binding.summaryTotal.text = CurrencyFormatter.format(requireContext(), total)
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

    private fun styleFormatCards() {
        if (selectedFormat == "PDF") {
            binding.cardPdf.setBackgroundResource(R.drawable.bg_card_selected)
            binding.cardCsv.setBackgroundResource(0)
        } else {
            binding.cardCsv.setBackgroundResource(R.drawable.bg_card_selected)
            binding.cardPdf.setBackgroundResource(0)
        }
        binding.btnExport.text = "Export $selectedFormat"
    }


    override fun getTheme(): Int = R.style.RoundedBottomSheet
    private fun exportPdf() {
        val list = filteredExpenses()
        val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        val file = PdfReportBuilder.buildPdf(requireContext(), list, currentBudget, monthLabel)

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "com.et.expensetrackorapp.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        justExported = true
        exportedCount = list.size
        startActivity(intent)
    }
    private fun exportCsv() {
        val list = filteredExpenses()
        val file = CsvReportBuilder.buildCsv(requireContext(), list)

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "com.et.expensetrackorapp.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "text/csv")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        justExported = true
        exportedCount = list.size
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
    override fun onResume() {
        super.onResume()
        if (justExported) {
            justExported = false
            ExportReadyFragment
                .newInstance(selectedFormat, exportedCount, selectedRange.label)
                .show(parentFragmentManager, "export_ready")
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}