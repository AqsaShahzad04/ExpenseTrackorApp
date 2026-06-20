package com.et.expensetrackorapp

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.et.expensetrackorapp.data.local.AppDatabase
import com.et.expensetrackorapp.data.local.Expense
import com.et.expensetrackorapp.data.repository.ExpenseRepository
import com.et.expensetrackorapp.databinding.FragmentAddEditBinding
import com.et.expensetrackorapp.viewmodel.ExpenseViewModel
import com.et.expensetrackorapp.viewmodel.ExpenseViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddEditFragment: BottomSheetDialogFragment() {

    private var _binding: FragmentAddEditBinding?=null

    private val binding get()=_binding!!

    private var selectedCategory: String? = null
    private var selectedCard: com.google.android.material.card.MaterialCardView? = null

    private val viewModel: ExpenseViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).expenseDao()
        val repository = ExpenseRepository(dao)
        ExpenseViewModelFactory(repository)
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
        ): View{
        _binding = FragmentAddEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAdd.setOnClickListener {

            val amount = binding.editAmount.text.toString()
            val name = binding.editName.text.toString()


            val category = "Food"
            val date = "2026-06-18"


            if (amount.isBlank() || name.isBlank() ||selectedCategory == null) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val expense = Expense(
                amount = amount.toDouble(),
                title = name,
                category =selectedCategory!!,
                date = date,
                note=null
            )


            viewModel.insert(expense)

            Toast.makeText(requireContext(), "Saved!", Toast.LENGTH_SHORT).show()
            // clear the form for the next entry
            binding.editAmount.text.clear()
            binding.editName.text.clear()
            binding.editDate.text.clear()

// un-highlight the selected category card and reset tracking
            selectedCard?.setCardBackgroundColor(Color.parseColor("#FFFDF9"))
            selectedCard = null
            selectedCategory = null
        }
        binding.cardFood.setOnClickListener {
            selectedCategory = "Food & Drink"
            selectedCard?.setCardBackgroundColor(Color.parseColor("#FFFDF9"))
            binding.cardFood.setCardBackgroundColor(Color.parseColor("#F6E0D8"))
            selectedCard = binding.cardFood
        }
        binding.cardTransport.setOnClickListener {
            selectedCategory = "Transport"
            selectedCard?.setCardBackgroundColor(Color.parseColor("#FFFDF9"))
            binding.cardTransport.setCardBackgroundColor(Color.parseColor("#F6E0D8"))
            selectedCard = binding.cardTransport
        }
        binding.cardHealth.setOnClickListener {
            selectedCategory = "Health"
            selectedCard?.setCardBackgroundColor(Color.parseColor("#FFFDF9"))
            binding.cardHealth.setCardBackgroundColor(Color.parseColor("#F6E0D8"))
            selectedCard = binding.cardHealth
        }
        binding.cardShopping.setOnClickListener {
            selectedCategory = "Shopping"
            selectedCard?.setCardBackgroundColor(Color.parseColor("#FFFDF9"))
            binding.cardShopping.setCardBackgroundColor(Color.parseColor("#F6E0D8"))
            selectedCard = binding.cardShopping
        }
        binding.cardBills.setOnClickListener {
            selectedCategory = "Bills"
            selectedCard?.setCardBackgroundColor(Color.parseColor("#FFFDF9"))
            binding.cardBills.setCardBackgroundColor(Color.parseColor("#F6E0D8"))
            selectedCard = binding.cardBills
        }
        binding.cardEntertainment.setOnClickListener {
            selectedCategory = "Entertainment"
            selectedCard?.setCardBackgroundColor(Color.parseColor("#FFFDF9"))
            binding.cardEntertainment.setCardBackgroundColor(Color.parseColor("#F6E0D8"))
            selectedCard = binding.cardEntertainment
        }
        binding.cardEducation.setOnClickListener {
            selectedCategory = "Education"
            selectedCard?.setCardBackgroundColor(Color.parseColor("#FFFDF9"))
            binding.cardEducation.setCardBackgroundColor(Color.parseColor("#F6E0D8"))
            selectedCard = binding.cardEducation
        }
        binding.cardCoffee.setOnClickListener {
            selectedCategory = "Coffee"
            selectedCard?.setCardBackgroundColor(Color.parseColor("#FFFDF9"))
            binding.cardCoffee.setCardBackgroundColor(Color.parseColor("#F6E0D8"))
            selectedCard = binding.cardCoffee
        }
        binding.cardOther.setOnClickListener {
            selectedCategory = "Others"
            selectedCard?.setCardBackgroundColor(Color.parseColor("#FFFDF9"))
            binding.cardOther.setCardBackgroundColor(Color.parseColor("#F6E0D8"))
            selectedCard = binding.cardOther
        }
        binding.editDate.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

            val picker = android.app.DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    // selectedMonth is 0-11, so add 1 for display
                    val formatted = String.format("%02d/%02d/%04d", selectedMonth + 1, selectedDay, selectedYear)
                    binding.editDate.setText(formatted)
                },
                year, month, day
            )
            picker.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding=null
    }

}