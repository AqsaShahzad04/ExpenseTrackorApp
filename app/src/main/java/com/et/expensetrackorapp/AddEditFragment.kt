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
    private var editingExpense: Expense? = null

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

        binding.textAmountSymbol.text =
            com.et.expensetrackorapp.util.CurrencyFormatter.symbol(requireContext())

        val args = arguments
        if (args != null && args.containsKey("arg_id")) {
            editingExpense = Expense(
                id = args.getInt("arg_id"),
                title = args.getString("arg_title") ?: "",
                amount = args.getDouble("arg_amount"),
                category = args.getString("arg_category") ?: "",
                date = args.getString("arg_date") ?: "",
                note = args.getString("arg_note")
            )
        }
        val today = java.util.Calendar.getInstance()
        val todayFormatted = String.format(
            "%04d-%02d-%02d",
            today.get(java.util.Calendar.YEAR),
            today.get(java.util.Calendar.MONTH) + 1,
            today.get(java.util.Calendar.DAY_OF_MONTH)
        )
        binding.editDate.setText(todayFormatted)
        if (editingExpense != null) {
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            binding.btnDelete.visibility = View.GONE
        }
        if (editingExpense != null) {
            binding.editName.setText(editingExpense!!.title)
            binding.editAmount.setText(editingExpense!!.amount.toString())
            binding.editDate.setText(editingExpense!!.date)
            selectedCategory = editingExpense!!.category

            val cardToSelect = when (editingExpense!!.category) {
                "Food & Drink" -> binding.cardFood
                "Transport" -> binding.cardTransport
                "Health" -> binding.cardHealth
                "Shopping" -> binding.cardShopping
                "Bills" -> binding.cardBills
                "Entertainment" -> binding.cardEntertainment
                "Education" -> binding.cardEducation
                "Coffee" -> binding.cardCoffee
                else -> binding.cardOther
            }
            cardToSelect.setCardBackgroundColor(Color.parseColor("#F6E0D8"))
            selectedCard = cardToSelect
            binding.textTitle2.text = "Edit expense"
            binding.btnAdd.text = "Update"
        }
        binding.btnAdd.setOnClickListener {

            val amount = binding.editAmount.text.toString()
            val name = binding.editName.text.toString()


            val date = binding.editDate.text.toString()



            if (amount.isBlank() || name.isBlank() || date.isBlank() || selectedCategory == null) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val expense = Expense(
                id = editingExpense?.id ?: 0,
                amount = amount.toDouble(),
                title = name,
                category =selectedCategory!!,
                date = date,
                note=null
            )
            if (editingExpense != null) {
                viewModel.update(expense)
            } else {
                viewModel.insert(expense)
            }


            Toast.makeText(requireContext(), "Saved!", Toast.LENGTH_SHORT).show()
            dismiss()

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
                    val formatted = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    binding.editDate.setText(formatted)
                },
                year, month, day
            )
            picker.show()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnDelete.setOnClickListener {
            if (editingExpense != null) {
                viewModel.delete(editingExpense!!)
                Toast.makeText(requireContext(), "Deleted!", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? com.google.android.material.bottomsheet.BottomSheetDialog
        val sheet = dialog?.findViewById<android.view.View>(
            com.google.android.material.R.id.design_bottom_sheet
        )
        sheet?.let {
            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()

        _binding=null
    }

    companion object {
        private const val ARG_ID = "arg_id"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_AMOUNT = "arg_amount"
        private const val ARG_CATEGORY = "arg_category"
        private const val ARG_DATE = "arg_date"
        private const val ARG_NOTE = "arg_note"

        fun newInstance(expense: Expense): AddEditFragment {
            val fragment = AddEditFragment()
            val args = Bundle()
            args.putInt(ARG_ID, expense.id)
            args.putString(ARG_TITLE, expense.title)
            args.putDouble(ARG_AMOUNT, expense.amount)
            args.putString(ARG_CATEGORY, expense.category)
            args.putString(ARG_DATE, expense.date)
            args.putString(ARG_NOTE, expense.note)
            fragment.arguments = args
            return fragment
        }
    }

}