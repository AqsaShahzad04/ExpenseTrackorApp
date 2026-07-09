package com.et.expensetrackorapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.et.expensetrackorapp.databinding.FragmentCurrencyPickerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CurrencyPickerFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCurrencyPickerBinding? = null
    private val binding get() = _binding!!

    // Same 8 currencies defined in SettingsFragment
    private val currencies = listOf(
        Currency("Pakistani Rupee", "PKR", "Rs."),
        Currency("US Dollar", "USD", "$"),
        Currency("Euro", "EUR", "€"),
        Currency("British Pound", "GBP", "£"),
        Currency("Indian Rupee", "INR", "₹"),
        Currency("UAE Dirham", "AED", "AED"),
        Currency("Japanese Yen", "JPY", "¥")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCurrencyPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // read currently-saved currency (fallback = Pakistani Rupee)
        val prefs = requireContext().getSharedPreferences("app_prefs", 0)
        val savedCurrency = prefs.getString("currency", "Pakistani Rupee (Rs.)")

        // close (✕) tap
        binding.btnClose.setOnClickListener { dismiss() }

        // build one row per currency
        for (c in currencies) {
            val row = layoutInflater.inflate(
                R.layout.item_currency_row,
                binding.currencyList,
                false
            )

            // fill in the text
            row.findViewById<TextView>(R.id.rowSymbol).text = c.symbol
            row.findViewById<TextView>(R.id.rowName).text = c.name
            row.findViewById<TextView>(R.id.rowCode).text = c.code

            // this is the display string we save and show back in Settings
            val displayValue = "${c.name} (${c.symbol})"

            // is this the currently-saved one?
            val isSelected = displayValue == savedCurrency
            if (isSelected) {
                row.setBackgroundResource(R.drawable.bg_currency_row_selected)
                row.findViewById<TextView>(R.id.rowName)
                    .setTextColor(ContextCompat.getColor(requireContext(), R.color.accent))
                row.findViewById<TextView>(R.id.rowCheck).visibility = View.VISIBLE
            }

            // row tap → save + notify Settings + dismiss
            row.setOnClickListener {
                prefs.edit()
                    .putString("currency", displayValue)
                    .putString("currency_symbol", c.symbol)
                    .apply()
                setFragmentResult(
                    "currency_result",
                    bundleOf("currency" to displayValue)
                )
                dismiss()
            }

            binding.currencyList.addView(row)

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