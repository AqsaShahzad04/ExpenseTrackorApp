package com.et.expensetrackorapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.et.expensetrackorapp.databinding.FragmentSettingsBinding


data class Currency(
    val name: String,     // "Pakistani Rupee"
    val code: String,     // "PKR"
    val symbol: String    // "Rs."
)

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val currencies = listOf(
        Currency("Pakistani Rupee", "PKR", "Rs."),
        Currency("US Dollar", "USD", "$"),
        Currency("Euro", "EUR", "€"),
        Currency("British Pound", "GBP", "£"),
        Currency("Indian Rupee", "INR", "₹"),
        Currency("UAE Dirham", "AED", "AED"),
        Currency("Saudi Riyal", "SAR", "SAR"),
        Currency("Japanese Yen", "JPY", "¥")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildCurrencyRow()
    }

    private fun buildCurrencyRow() {
        // read saved currency (fallback = Pakistani Rupee)
        val prefs = requireContext().getSharedPreferences("app_prefs", 0)
        val currency = prefs.getString("currency", "Pakistani Rupee (Rs.)")

        val row = layoutInflater.inflate(
            R.layout.item_setting_row,
            binding.currencyContainer,
            false
        )
        row.findViewById<TextView>(R.id.rowLabel).text = "Currency"
        row.findViewById<TextView>(R.id.rowValue).text = currency

        row.setOnClickListener {
            showCurrencyPicker()
        }
        binding.currencyContainer.addView(row)
    }

    private fun showCurrencyPicker() {
        // will show the bottom sheet in step 5
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}