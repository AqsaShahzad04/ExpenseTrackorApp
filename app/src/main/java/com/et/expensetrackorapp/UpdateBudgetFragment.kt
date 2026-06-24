package com.et.expensetrackorapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.et.expensetrackorapp.data.local.AppDatabase
import com.et.expensetrackorapp.data.local.Budget
import com.et.expensetrackorapp.data.repository.BudgetRepository
import com.et.expensetrackorapp.databinding.FragmentUpdateBudgetBinding
import com.et.expensetrackorapp.viewmodel.BudgetViewModel
import com.et.expensetrackorapp.viewmodel.BudgetViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class UpdateBudgetFragment: BottomSheetDialogFragment() {

    private var _binding: FragmentUpdateBudgetBinding?=null
    private val binding get()=_binding!!

    private val budgetViewModel: BudgetViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).budgetDao()
        val repository = BudgetRepository(dao)
        BudgetViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.chip50k.setOnClickListener { binding.editBudgetAmount.setText("50000") }
        binding.chip80k.setOnClickListener { binding.editBudgetAmount.setText("80000") }
        binding.chip120k.setOnClickListener { binding.editBudgetAmount.setText("120000") }
        binding.chip200k.setOnClickListener { binding.editBudgetAmount.setText("200000") }

        binding.btnCancelBudget.setOnClickListener {
            dismiss()
        }

        binding.btnSaveBudget.setOnClickListener {
            val amountText = binding.editBudgetAmount.text.toString()

            if (amountText.isBlank()) {
                Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val currentMonth = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
                .format(java.util.Date())

            val budget = Budget(
                month = currentMonth,
                amount = amountText.toDouble()
            )

            budgetViewModel.setBudget(budget)

            Toast.makeText(requireContext(), "Budget saved!", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}