package com.et.expensetrackorapp

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.setFragmentResult
import com.et.expensetrackorapp.databinding.FragmentFilterSortBinding
import com.et.expensetrackorapp.viewmodel.CategoryColors
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterSortFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterSortBinding? = null
    private val binding get() = _binding!!

    // current selection (start from what History passed in)
    private var selectedSort = "Newest"
    private var selectedRange = "All time"
    private var selectedCategory = "All"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterSortBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // read current choices History passed in (so the sheet opens showing them)
        selectedSort = arguments?.getString("sort") ?: "Newest"
        selectedRange = arguments?.getString("range") ?: "All time"
        selectedCategory = arguments?.getString("category") ?: "All"

        colorCategoryDots()
        wireSortPills()
        wireRangePills()
        wireCategoryPills()
        refreshAllPills()

        binding.btnClose.setOnClickListener { dismiss() }

        binding.btnReset.setOnClickListener {
            selectedSort = "Newest"
            selectedRange = "All time"
            selectedCategory = "All"
            refreshAllPills()
        }

        binding.btnApply.setOnClickListener {
            setFragmentResult(
                "filter_sort_result",
                bundleOf(
                    "sort" to selectedSort,
                    "range" to selectedRange,
                    "category" to selectedCategory
                )
            )
            dismiss()
        }
    }

    // map each category pill to its label + CategoryColors key, tint its dot
    private fun colorCategoryDots() {
        val pairs = listOf(
            binding.catFood to "Food & Drink",
            binding.catTransport to "Transport",
            binding.catHealth to "Health",
            binding.catShopping to "Shopping",
            binding.catBills to "Bills",
            binding.catEntertainment to "Entertainment",
            binding.catEducation to "Education",
            binding.catCoffee to "Coffee",
            binding.catOther to "Other"
        )
        pairs.forEach { (pill, key) ->
            TextViewCompat.setCompoundDrawableTintList(
                pill, ColorStateList.valueOf(CategoryColors.colorFor(key))
            )
        }
    }

    private fun wireSortPills() {
        val map = mapOf(
            binding.sortNewest to "Newest",
            binding.sortOldest to "Oldest",
            binding.sortHighest to "Highest",
            binding.sortLowest to "Lowest"
        )
        map.forEach { (pill, value) ->
            pill.setOnClickListener { selectedSort = value; refreshSort(map) }
        }
    }

    private fun wireRangePills() {
        val map = mapOf(
            binding.rangeAll to "All time",
            binding.rangeMonth to "This month",
            binding.range30 to "Last 30 days",
            binding.range7 to "Last 7 days"
        )
        map.forEach { (pill, value) ->
            pill.setOnClickListener { selectedRange = value; refreshRange(map) }
        }
    }

    private fun wireCategoryPills() {
        val map = mapOf(
            binding.catAll to "All",
            binding.catFood to "Food & Drink",
            binding.catTransport to "Transport",
            binding.catHealth to "Health",
            binding.catShopping to "Shopping",
            binding.catBills to "Bills",
            binding.catEntertainment to "Entertainment",
            binding.catEducation to "Education",
            binding.catCoffee to "Coffee",
            binding.catOther to "Other"
        )
        map.forEach { (pill, value) ->
            pill.setOnClickListener { selectedCategory = value; refreshCategory(map) }
        }
    }

    // --- background swap helpers ---
    private fun setPill(pill: TextView, selected: Boolean) {
        pill.setBackgroundResource(
            if (selected) R.drawable.bg_pill_range_selected
            else R.drawable.bg_pill_range_unselected
        )
        pill.setTextColor(
            if (selected) android.graphics.Color.parseColor("#FFFCF5")
            else android.graphics.Color.parseColor("#1A1612")
        )
    }

    private fun refreshSort(map: Map<TextView, String>) =
        map.forEach { (pill, value) -> setPill(pill, value == selectedSort) }

    private fun refreshRange(map: Map<TextView, String>) =
        map.forEach { (pill, value) -> setPill(pill, value == selectedRange) }

    private fun refreshCategory(map: Map<TextView, String>) =
        map.forEach { (pill, value) -> setPill(pill, value == selectedCategory) }

    private fun refreshAllPills() {
        refreshSort(
            mapOf(
                binding.sortNewest to "Newest",
                binding.sortOldest to "Oldest",
                binding.sortHighest to "Highest",
                binding.sortLowest to "Lowest"
            )
        )
        refreshRange(
            mapOf(
                binding.rangeAll to "All time",
                binding.rangeMonth to "This month",
                binding.range30 to "Last 30 days",
                binding.range7 to "Last 7 days"
            )
        )
        refreshCategory(
            mapOf(
                binding.catAll to "All",
                binding.catFood to "Food & Drink",
                binding.catTransport to "Transport",
                binding.catHealth to "Health",
                binding.catShopping to "Shopping",
                binding.catBills to "Bills",
                binding.catEntertainment to "Entertainment",
                binding.catEducation to "Education",
                binding.catCoffee to "Coffee",
                binding.catOther to "Other"
            )
        )
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

    companion object {
        fun newInstance(sort: String, range: String, category: String): FilterSortFragment {
            return FilterSortFragment().apply {
                arguments = bundleOf(
                    "sort" to sort,
                    "range" to range,
                    "category" to category
                )
            }
        }
    }
}