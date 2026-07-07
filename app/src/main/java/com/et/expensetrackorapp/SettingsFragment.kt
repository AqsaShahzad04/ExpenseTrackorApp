package com.et.expensetrackorapp
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.et.expensetrackorapp.databinding.FragmentSettingsBinding
import androidx.fragment.app.viewModels
import com.et.expensetrackorapp.data.local.AppDatabase
import com.et.expensetrackorapp.data.repository.BudgetRepository
import com.et.expensetrackorapp.viewmodel.BudgetViewModel
import com.et.expensetrackorapp.viewmodel.BudgetViewModelFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.et.expensetrackorapp.data.repository.ExpenseRepository
import com.et.expensetrackorapp.viewmodel.ExpenseViewModel
import com.et.expensetrackorapp.viewmodel.ExpenseViewModelFactory
import android.widget.ImageView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.first

data class Currency(
    val name: String,
    val code: String,
    val symbol: String
)

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val budgetViewModel: BudgetViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).budgetDao()
        val repository = BudgetRepository(dao)
        BudgetViewModelFactory(repository)
    }

    private val expenseViewModel: ExpenseViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).expenseDao()
        val repository = ExpenseRepository(dao)
        ExpenseViewModelFactory(repository)
    }
    // keeps the current transaction count live, so the delete sheet can show it
    private var currentTxnCount = 0

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
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildCurrencyRow()
        buildBudgetRow()
        buildDataRows()

        // listen for the currency the picker sends back
        parentFragmentManager.setFragmentResultListener(
            "currency_result",
            viewLifecycleOwner
        ) { _, bundle ->
            val newCurrency = bundle.getString("currency")
            // update the row's right-side value text
            binding.currencyContainer
                .findViewById<TextView>(R.id.rowValue)
                ?.text = newCurrency
        }
        // keep the live transaction count updated
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                expenseViewModel.allExpenses.collect { expenses ->
                    currentTxnCount = expenses.size
                }
            }
        }

        // listen for the delete-all confirmation from the bottom sheet
        parentFragmentManager.setFragmentResultListener(
            "delete_all_result",
            viewLifecycleOwner
        ) { _, bundle ->
            if (bundle.getBoolean("confirmed")) {
                expenseViewModel.deleteAll()
                Toast.makeText(requireContext(), "All transactions deleted", Toast.LENGTH_SHORT).show()
            }
        }
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
    private fun buildBudgetRow() {
        // current month key, same format the budget sheet saves with
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            .format(Date())

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                budgetViewModel.getBudgetForMonth(currentMonth).collect { budget ->

                    // build the row fresh each time the budget changes
                    binding.budgetContainer.removeAllViews()

                    val row = layoutInflater.inflate(
                        R.layout.item_setting_row,
                        binding.budgetContainer,
                        false
                    )

                    row.findViewById<TextView>(R.id.rowLabel).text = "Monthly budget"
                    row.setOnClickListener {
                        findNavController().navigate(R.id.action_settings_to_budget)
                    }

                    // budget?.amount is null if no budget set this month
                    val valueText = if (budget != null) {
                        "Rs. " + "%,.0f".format(budget.amount)
                    } else {
                        "Not set"
                    }
                    row.findViewById<TextView>(R.id.rowValue).text = valueText

                    binding.budgetContainer.addView(row)
                }
            }
        }
    }


    private fun buildDataRows() {
        binding.dataContainer.removeAllViews()

        // --- Row 1: Export PDF report ---
        val exportRow = layoutInflater.inflate(
            R.layout.item_setting_row, binding.dataContainer, false
        )
        exportRow.findViewById<ImageView>(R.id.rowIcon).apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_download)
        }
        exportRow.findViewById<TextView>(R.id.rowLabel).text = "Export PDF report"
        exportRow.findViewById<TextView>(R.id.rowValue).text = ""
        exportRow.setOnClickListener {
            exportPdf()
        }
        binding.dataContainer.addView(exportRow)

        // --- Row 2: Delete all transactions (red) ---
        val deleteRow = layoutInflater.inflate(
            R.layout.item_setting_row, binding.dataContainer, false
        )
        deleteRow.findViewById<ImageView>(R.id.rowIcon).apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_delete)
            setColorFilter(ContextCompat.getColor(requireContext(), R.color.danger))
        }
        val deleteLabel = deleteRow.findViewById<TextView>(R.id.rowLabel)
        deleteLabel.text = "Delete all transactions"
        deleteLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.danger))
        deleteRow.findViewById<TextView>(R.id.rowValue).text = ""
        deleteRow.setOnClickListener { confirmDeleteAll() }
        binding.dataContainer.addView(deleteRow)
    }

    private fun exportPdf() {
        ExportFragment().show(parentFragmentManager, "export")
    }

    private fun confirmDeleteAll() {
        DeleteConfirmFragment.newInstance(currentTxnCount)
            .show(parentFragmentManager, "delete_confirm")
    }

    private fun showCurrencyPicker() {
        CurrencyPickerFragment().show(parentFragmentManager, "currency")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}