package com.et.expensetrackorapp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.et.expensetrackorapp.databinding.FragmentNotificationsBinding
import com.et.expensetrackorapp.databinding.ItemNotifRowBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NotificationsFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spent = arguments?.getDouble("spent") ?: 0.0
        val budget = arguments?.getDouble("budget") ?: 0.0
        val biggestName = arguments?.getString("biggestName")
        val biggestAmount = arguments?.getDouble("biggestAmount") ?: 0.0

        binding.btnClose.setOnClickListener { dismiss() }

        val inflater = LayoutInflater.from(requireContext())

        // 1) Over budget (only if a budget is set AND spent exceeds it)
        if (budget > 0 && spent > budget) {
            val over = spent - budget
            addRow(
                inflater,
                R.drawable.ic_bell,
                "Over budget",
                "You're Rs. ${"%,.0f".format(over)} past your monthly limit.",
                danger = true
            )
        }

        // 2) Biggest expense this month (only if there is one)
        if (biggestName != null) {
            addRow(
                inflater,
                R.drawable.ic_trend_up,
                "Biggest expense this month",
                "$biggestName · Rs. ${"%,.0f".format(biggestAmount)}",
                danger = false
            )
        }

        // 3) Tip (always)
        addRow(
            inflater,
            R.drawable.ic_tip,
            "Tip",
            "Tap any transaction to edit or delete it.",
            danger = false
        )
    }

    private fun addRow(
        inflater: LayoutInflater,
        iconRes: Int,
        title: String,
        subtitle: String,
        danger: Boolean
    ) {
        val row = ItemNotifRowBinding.inflate(inflater, binding.notifContainer, false)
        row.notifIcon.setImageResource(iconRes)
        row.notifTitle.text = title
        row.notifSubtitle.text = subtitle

        if (danger) {
            row.root.setBackgroundResource(R.drawable.bg_notif_row_danger)
            val red = Color.parseColor("#C73E1D")
            row.notifIcon.setColorFilter(red)
            row.notifTitle.setTextColor(red)
            row.notifSubtitle.setTextColor(red)
        }

        // spacing between rows
        val lp = android.widget.LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.bottomMargin = (10 * resources.displayMetrics.density).toInt()
        row.root.layoutParams = lp

        binding.notifContainer.addView(row.root)
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
        fun newInstance(
            spent: Double,
            budget: Double,
            biggestName: String?,
            biggestAmount: Double
        ): NotificationsFragment {
            return NotificationsFragment().apply {
                arguments = bundleOf(
                    "spent" to spent,
                    "budget" to budget,
                    "biggestName" to biggestName,
                    "biggestAmount" to biggestAmount
                )
            }
        }
    }
}