package com.et.expensetrackorapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.et.expensetrackorapp.data.local.AppDatabase
import com.et.expensetrackorapp.data.local.Expense
import com.et.expensetrackorapp.data.repository.ExpenseRepository
import com.et.expensetrackorapp.databinding.FragmentStatsBinding
import com.et.expensetrackorapp.viewmodel.CategoryColors
import com.et.expensetrackorapp.viewmodel.ExpenseViewModel
import com.et.expensetrackorapp.viewmodel.ExpenseViewModelFactory
import com.et.expensetrackorapp.viewmodel.StatsCalculator
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.launch
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private var scope = Scope.MONTH          // which pill is selected
    private var latestExpenses: List<Expense> = emptyList()  // remember data for taps

    private enum class Scope { WEEK, MONTH }

    private val viewModel: ExpenseViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).expenseDao()
        val repository = ExpenseRepository(dao)
        ExpenseViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPieChart()
        setupBarChart()

        // pill taps: change scope, update look, redraw
        binding.pill7Days.setOnClickListener {
            scope = Scope.WEEK
            updatePillLook()
            refresh(latestExpenses)
        }
        binding.pillMonth.setOnClickListener {
            scope = Scope.MONTH
            updatePillLook()
            refresh(latestExpenses)
        }
        updatePillLook()  // set initial selected look

        // live data: remember it, then draw
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allExpenses.collect { allExpenses ->
                    latestExpenses = allExpenses
                    refresh(allExpenses)
                    drawBarChart(allExpenses)
                }
            }
        }
    }

    // draws the whole screen from a full expense list (filters by scope inside)
    private fun refresh(allExpenses: List<Expense>) {
        val expenses = filterByScope(allExpenses)
        val stats = StatsCalculator.categoryStats(expenses)

        val entries = stats.map { PieEntry(it.total.toFloat(), it.category) }
        val colors = stats.map { CategoryColors.colorFor(it.category) }
        buildLegend(stats.map { it.category })

        val grandTotal = expenses.sumOf { it.amount }
        binding.tvTotal.text = "Rs. " + "%,.0f".format(grandTotal)
        binding.tvTxnCount.text = "${expenses.size} transactions"

        val dataSet = PieDataSet(entries, "").apply {
            setColors(colors)
            setDrawValues(false)
            sliceSpace = 2f
        }
        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.centerText = "CATS\n${stats.size}"
        binding.pieChart.animateY(800)
    }

    private fun drawBarChart(allExpenses: List<Expense>) {
        val days = StatsCalculator.last14Days(allExpenses)
        val barEntries = days.mapIndexed { index, day ->
            BarEntry(index.toFloat(), day.total.toFloat())
        }
        val barSet = BarDataSet(barEntries, "").apply {
            color = android.graphics.Color.parseColor("#C9C2B5")
            setDrawValues(false)
        }
        binding.barChart.data = BarData(barSet).apply { barWidth = 0.9f }

        // label only the days that have spending; blank for empty days
        val labels = days.map { if (it.total > 0) it.label else "" }
        binding.barChart.xAxis.apply {
            binding.barChart.xAxis.axisMinimum = -0.5f
            binding.barChart.xAxis.axisMaximum = days.size - 0.5f

            binding.tvStartDate.text = days.first().label
            binding.tvEndDate.text = days.last().label
        }
        binding.barChart.setFitBars(true)

        val peak = days.maxOfOrNull { it.total } ?: 0.0
        binding.tvPeak.text = "peak Rs. " + "%,.0f".format(peak)

        binding.barChart.invalidate()
        binding.barChart.animateY(600)
    }
    // dark = selected, plain = not
    private fun updatePillLook() {
        val unselected = android.graphics.Color.parseColor("#9A9387")
        if (scope == Scope.WEEK) {
            binding.pill7Days.setTextColor(android.graphics.Color.WHITE)
            binding.pill7Days.setBackgroundResource(R.drawable.bg_pill_selected)
            binding.pillMonth.setTextColor(unselected)
            binding.pillMonth.setBackgroundResource(R.drawable.bg_legend_pill)
        } else {
            binding.pillMonth.setTextColor(android.graphics.Color.WHITE)
            binding.pillMonth.setBackgroundResource(R.drawable.bg_pill_selected)
            binding.pill7Days.setTextColor(unselected)
            binding.pill7Days.setBackgroundResource(R.drawable.bg_legend_pill)
        }
    }
    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 70f
            transparentCircleRadius = 0f
            setHoleColor(android.graphics.Color.TRANSPARENT)
            setEntryLabelColor(android.graphics.Color.TRANSPARENT)
            setCenterTextSize(16f)

        }
    }
    private fun setupBarChart() {
        binding.barChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setScaleEnabled(false)
            setTouchEnabled(false)
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(true)
            xAxis.axisLineColor = android.graphics.Color.parseColor("#E5DED2")
            xAxis.axisLineWidth = 1f
            xAxis.setDrawLabels(false)        // keep labels off (we use our own date TextViews)
            xAxis.textColor = android.graphics.Color.parseColor("#9A9387")
            xAxis.textSize = 9f
            setExtraBottomOffset(10f)
            xAxis.setDrawLabels(false)
        }
    }

    private fun buildLegend(categories: List<String>) {
        val strip = binding.legendStrip
        strip.removeAllViews()

        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        for (category in categories) {
            val pill = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                background = androidx.core.content.ContextCompat
                    .getDrawable(requireContext(), R.drawable.bg_legend_pill)
                setPadding(dp(12), dp(6), dp(12), dp(6))
                val lp = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.marginEnd = dp(8)
                layoutParams = lp
            }

            val dot = android.view.View(requireContext()).apply {
                val size = dp(10)
                val lp = android.widget.LinearLayout.LayoutParams(size, size)
                lp.marginEnd = dp(6)
                layoutParams = lp
                val circle = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(CategoryColors.colorFor(category))
                }
                background = circle
            }

            val label = android.widget.TextView(requireContext()).apply {
                text = category
                textSize = 12f
                setTextColor(android.graphics.Color.parseColor("#3A3A3A"))
            }

            pill.addView(dot)
            pill.addView(label)
            strip.addView(pill)
        }
    }

    private fun filterByScope(expenses: List<Expense>): List<Expense> {
        val keyFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

        return when (scope) {
            Scope.MONTH -> {
                val monthPrefix = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
                    .format(java.util.Calendar.getInstance().time)
                expenses.filter { it.date.startsWith(monthPrefix) }
            }
            Scope.WEEK -> {
                val cutoff = java.util.Calendar.getInstance()
                cutoff.add(java.util.Calendar.DAY_OF_YEAR, -6)
                val cutoffKey = keyFormat.format(cutoff.time)
                expenses.filter { it.date >= cutoffKey }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}