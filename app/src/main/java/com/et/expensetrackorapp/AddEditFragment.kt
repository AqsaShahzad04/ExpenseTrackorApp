package com.et.expensetrackorapp

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.et.expensetrackorapp.data.local.AppDatabase
import com.et.expensetrackorapp.data.local.Expense
import com.et.expensetrackorapp.data.repository.ExpenseRepository
import com.et.expensetrackorapp.databinding.FragmentAddEditBinding
import com.et.expensetrackorapp.viewmodel.CategoryColors
import com.et.expensetrackorapp.viewmodel.ExpenseViewModel
import com.et.expensetrackorapp.viewmodel.ExpenseViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.toString

class AddEditFragment: BottomSheetDialogFragment() {

    private var _binding: FragmentAddEditBinding?=null

    private val binding get()=_binding!!

    private var selectedCategory: String? = null
    private var selectedCard: com.google.android.material.card.MaterialCardView? = null
    private var selectedIcon: android.widget.ImageView? = null
    private var selectedText: android.widget.TextView? = null
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
            val iconToSelect = when (editingExpense!!.category) {
                "Food & Drink" -> binding.food
                "Transport" -> binding.car
                "Health" -> binding.health
                "Shopping" -> binding.shopping
                "Bills" -> binding.bills
                "Entertainment" -> binding.entertainment
                "Education" -> binding.education
                "Coffee" -> binding.coffee
                else -> binding.other
            }
            val textToSelect = when (editingExpense!!.category) {
                "Food & Drink" -> binding.textFood
                "Transport" -> binding.textTransport
                "Health" -> binding.textHealth
                "Shopping" -> binding.textShopping
                "Bills" -> binding.textBills
                "Entertainment" -> binding.textEntertainment
                "Education" -> binding.textEducation
                "Coffee" -> binding.textCoffee
                else -> binding.textOther
            }
            val color = CategoryColors.colorFor(editingExpense!!.category)
            cardToSelect.setCardBackgroundColor(CategoryColors.tileBgFor(editingExpense!!.category))
            cardToSelect.strokeColor = color
            cardToSelect.strokeWidth = 3
            iconToSelect.setColorFilter(color)
            textToSelect.setTextColor(color)
            selectedCard = cardToSelect
            selectedIcon = iconToSelect
            selectedText = textToSelect
            binding.textTitle2.text = "Edit expense"
            binding.btnAdd.text = "Save"
            binding.btnAdd.setBackgroundColor(Color.parseColor("#000000"))
        }
        else {
            // Add mode: pre-select Food & Drink by default
            selectedCategory = "Food & Drink"
            binding.cardFood.setCardBackgroundColor(Color.parseColor("#F6E0D8"))
            selectedCard = binding.cardFood
            binding.cardFood.setCardBackgroundColor(CategoryColors.tileBgFor("Food & Drink"))
            binding.cardFood.strokeColor = CategoryColors.colorFor("Food & Drink")
            binding.cardFood.strokeWidth = 3
            binding.food.setColorFilter(CategoryColors.colorFor("Food & Drink"))
            binding.textFood.setTextColor(CategoryColors.colorFor("Food & Drink"))
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
            selectedCard?.strokeWidth = 0
            selectedIcon?.setColorFilter(Color.parseColor("#000000"))
            selectedText?.setTextColor(Color.parseColor("#5A5249"))
            selectedCard = null
            selectedIcon = null
            selectedText = null
            selectedCategory = null
        }
        binding.cardFood.setOnClickListener {
            selectedCategory = "Food & Drink"
            selectedCard?.setCardBackgroundColor(Color.parseColor("#FFFDF9"))
            selectedCard?.strokeWidth = 0
            selectedIcon?.setColorFilter(Color.parseColor("#000000"))
            selectedText?.setTextColor(Color.parseColor("#5A5249"))
            binding.cardFood.setCardBackgroundColor(CategoryColors.tileBgFor("Food & Drink"))
            binding.cardFood.strokeColor = CategoryColors.colorFor("Food & Drink")
            binding.cardFood.strokeWidth = 3
            binding.food.setColorFilter(CategoryColors.colorFor("Food & Drink"))
            binding.textFood.setTextColor(CategoryColors.colorFor("Food & Drink"))
            selectedCard = binding.cardFood
            selectedIcon = binding.food
            selectedText = binding.textFood
        }
        binding.cardTransport.setOnClickListener {
            selectedCategory = "Transport"
            selectedCard?.setCardBackgroundColor(Color.parseColor("#FFFDF9"))
            selectedCard?.strokeWidth = 0
            selectedIcon?.setColorFilter(Color.parseColor("#000000"))
            selectedText?.setTextColor(Color.parseColor("#5A5249"))
            binding.cardTransport.setCardBackgroundColor(CategoryColors.tileBgFor("Transport"))
            binding.cardTransport.strokeColor = CategoryColors.colorFor("Transport")
            binding.cardTransport.strokeWidth = 3
            binding.car.setColorFilter(CategoryColors.colorFor("Transport"))
            binding.textTransport.setTextColor(CategoryColors.colorFor("Transport"))
            selectedCard = binding.cardTransport
            selectedIcon = binding.car
            selectedText = binding.textTransport
        }
        binding.cardHealth.setOnClickListener {
            selectedCategory = "Health"
            selectedCard?.setCardBackgroundColor(Color.parseColor("#FFFDF9"))
            selectedCard?.strokeWidth = 0
            selectedIcon?.setColorFilter(Color.parseColor("#000000"))
            selectedText?.setTextColor(Color.parseColor("#5A5249"))
            binding.cardHealth.setCardBackgroundColor(CategoryColors.tileBgFor("Health"))
            binding.cardHealth.strokeColor = CategoryColors.colorFor("Health")
            binding.cardHealth.strokeWidth = 3
            binding.health.setColorFilter(CategoryColors.colorFor("Health"))
            binding.textHealth.setTextColor(CategoryColors.colorFor("Health"))
            selectedCard = binding.cardHealth
            selectedIcon = binding.health
            selectedText = binding.textHealth
        }
        binding.cardShopping.setOnClickListener {
            selectedCategory = "Shopping"
            selectedCard?.setCardBackgroundColor(Color.parseColor("#FFFDF9"))
            selectedCard?.strokeWidth = 0
            selectedIcon?.setColorFilter(Color.parseColor("#000000"))
            selectedText?.setTextColor(Color.parseColor("#5A5249"))
            binding.cardShopping.setCardBackgroundColor(CategoryColors.tileBgFor("Shopping"))
            binding.cardShopping.strokeColor = CategoryColors.colorFor("Shopping")
            binding.cardShopping.strokeWidth = 3
            binding.shopping.setColorFilter(CategoryColors.colorFor("Shopping"))
            binding.textShopping.setTextColor(CategoryColors.colorFor("Shopping"))
            selectedCard = binding.cardShopping
            selectedIcon = binding.shopping
            selectedText = binding.textShopping
        }
        binding.cardBills.setOnClickListener {
            selectedCategory = "Bills"
            selectedCard?.setCardBackgroundColor(Color.parseColor("#FFFDF9"))
            selectedCard?.strokeWidth = 0
            selectedIcon?.setColorFilter(Color.parseColor("#000000"))
            selectedText?.setTextColor(Color.parseColor("#5A5249"))
            binding.cardBills.setCardBackgroundColor(CategoryColors.tileBgFor("Bills"))
            binding.cardBills.strokeColor = CategoryColors.colorFor("Bills")
            binding.cardBills.strokeWidth = 3
            binding.bills.setColorFilter(CategoryColors.colorFor("Bills"))
            binding.textBills.setTextColor(CategoryColors.colorFor("Bills"))
            selectedCard = binding.cardBills
            selectedIcon = binding.bills
            selectedText = binding.textBills
        }
        binding.cardEntertainment.setOnClickListener {
            selectedCategory = "Entertainment"
            selectedCard?.setCardBackgroundColor(Color.parseColor("#FFFDF9"))
            selectedCard?.strokeWidth = 0
            selectedIcon?.setColorFilter(Color.parseColor("#000000"))
            selectedText?.setTextColor(Color.parseColor("#5A5249"))
            binding.cardEntertainment.setCardBackgroundColor(CategoryColors.tileBgFor("Entertainment"))
            binding.cardEntertainment.strokeColor = CategoryColors.colorFor("Entertainment")
            binding.cardEntertainment.strokeWidth = 3
            binding.entertainment.setColorFilter(CategoryColors.colorFor("Entertainment"))
            binding.textEntertainment.setTextColor(CategoryColors.colorFor("Entertainment"))
            selectedCard = binding.cardEntertainment
            selectedIcon = binding.entertainment
            selectedText = binding.textEntertainment
        }
        binding.cardEducation.setOnClickListener {
            selectedCategory = "Education"
            selectedCard?.setCardBackgroundColor(Color.parseColor("#FFFDF9"))
            selectedCard?.strokeWidth = 0
            selectedIcon?.setColorFilter(Color.parseColor("#000000"))
            selectedText?.setTextColor(Color.parseColor("#5A5249"))
            binding.cardEducation.setCardBackgroundColor(CategoryColors.tileBgFor("Education"))
            binding.cardEducation.strokeColor = CategoryColors.colorFor("Education")
            binding.cardEducation.strokeWidth = 3
            binding.education.setColorFilter(CategoryColors.colorFor("Education"))
            binding.textEducation.setTextColor(CategoryColors.colorFor("Education"))
            selectedCard = binding.cardEducation
            selectedIcon = binding.education
            selectedText = binding.textEducation
        }
        binding.cardCoffee.setOnClickListener {
            selectedCategory = "Coffee"
            selectedCard?.setCardBackgroundColor(Color.parseColor("#FFFDF9"))
            selectedCard?.strokeWidth = 0
            selectedIcon?.setColorFilter(Color.parseColor("#000000"))
            selectedText?.setTextColor(Color.parseColor("#5A5249"))
            binding.cardCoffee.setCardBackgroundColor(CategoryColors.tileBgFor("Coffee"))
            binding.cardCoffee.strokeColor = CategoryColors.colorFor("Coffee")
            binding.cardCoffee.strokeWidth = 3
            binding.coffee.setColorFilter(CategoryColors.colorFor("Coffee"))
            binding.textCoffee.setTextColor(CategoryColors.colorFor("Coffee"))
            selectedCard = binding.cardCoffee
            selectedIcon = binding.coffee
            selectedText = binding.textCoffee
        }
        binding.cardOther.setOnClickListener {
            selectedCategory = "Others"
            selectedCard?.setCardBackgroundColor(Color.parseColor("#FFFDF9"))
            selectedCard?.strokeWidth = 0
            selectedIcon?.setColorFilter(Color.parseColor("#000000"))
            selectedText?.setTextColor(Color.parseColor("#5A5249"))
            binding.cardOther.setCardBackgroundColor(CategoryColors.tileBgFor("Others"))
            binding.cardOther.strokeColor = CategoryColors.colorFor("Others")
            binding.cardOther.strokeWidth = 3
            binding.other.setColorFilter(CategoryColors.colorFor("Others"))
            binding.textOther.setTextColor(CategoryColors.colorFor("Others"))
            selectedCard = binding.cardOther
            selectedIcon = binding.other
            selectedText = binding.textOther
        }
        binding.editDate.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()

            val datePicker = android.app.DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->

                    // date is picked — now ask for time
                    val timePicker = android.app.TimePickerDialog(
                        requireContext(),
                        { _, selectedHour, selectedMinute ->

                            // both picked — combine and format
                            val picked = java.util.Calendar.getInstance().apply {
                                set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute)
                            }
                            val formatted = java.text.SimpleDateFormat(
                                "MM/dd/yyyy hh:mm a",
                                java.util.Locale.getDefault()
                            ).format(picked.time)

                            binding.editDate.setText(formatted)
                        },
                        calendar.get(java.util.Calendar.HOUR_OF_DAY),
                        calendar.get(java.util.Calendar.MINUTE),
                        false   // false = 12-hour with AM/PM; true = 24-hour
                    )
                    timePicker.show()
                },
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnClose.setOnClickListener {
            dismiss()
        }
        binding.btnDelete.setOnClickListener {
            if (editingExpense != null) {
                viewModel.delete(editingExpense!!)
                Toast.makeText(requireContext(), "Deleted!", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
        val watcher = object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { updateAddButtonState() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        binding.editAmount.addTextChangedListener(watcher)
        binding.editName.addTextChangedListener(watcher)
        updateAddButtonState()
    }

    override fun onStart() {
        super.onStart()

        // --- Expand the sheet fully ---
        val bottomSheetDialog = dialog as? com.google.android.material.bottomsheet.BottomSheetDialog
        val sheet = bottomSheetDialog?.findViewById<android.view.View>(
            com.google.android.material.R.id.design_bottom_sheet
        )
        sheet?.let {
            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }

        // --- Blur the background behind the sheet ---
        val window = bottomSheetDialog?.window
        if (window != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                    WindowManager.LayoutParams.FLAG_BLUR_BEHIND
                )
                val params = window.attributes
                params.blurBehindRadius = 40
                window.attributes = params
            }
            window.setDimAmount(0.2f)
        }
    }
    override fun getTheme(): Int = R.style.RoundedBottomSheet

    override fun onDestroyView() {
        super.onDestroyView()

        _binding=null
    }
    private fun updateAddButtonState() {
        val allFilled = binding.editAmount.text.toString().isNotBlank() &&
                binding.editName.text.toString().isNotBlank()

        val color = if (allFilled) "#000000" else "#9E9A92"  // black when ready, grey otherwise
        binding.btnAdd.backgroundTintList =
            android.content.res.ColorStateList.valueOf(Color.parseColor(color))
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