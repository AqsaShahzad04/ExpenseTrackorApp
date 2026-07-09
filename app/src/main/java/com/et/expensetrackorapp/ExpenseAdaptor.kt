package com.et.expensetrackorapp

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.et.expensetrackorapp.data.local.Expense
import com.et.expensetrackorapp.databinding.ItemExpenseRowBinding
import com.et.expensetrackorapp.viewmodel.CategoryColors
import com.et.expensetrackorapp.viewmodel.CategoryIcons

class ExpenseAdaptor(
    private var expenseList: List<Expense>,
    private val onItemClick: () -> Unit
) : RecyclerView.Adapter<ExpenseAdaptor.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(
        val binding: ItemExpenseRowBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExpenseViewHolder {
        val binding = ItemExpenseRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]
        holder.binding.imageIcon.setImageResource(CategoryIcons.iconFor(expense.category))
        holder.binding.imageIcon.setColorFilter(CategoryColors.colorFor(expense.category))
        holder.binding.iconCircle.backgroundTintList =
            ColorStateList.valueOf(CategoryColors.lightBgFor(expense.category))
        holder.binding.textTitle.text = expense.title
        holder.binding.textCategory.text = "${expense.category} · ${expense.date}"
        holder.binding.textAmount.text = "- ${com.et.expensetrackorapp.util.CurrencyFormatter.format(holder.itemView.context, expense.amount)}"
        holder.itemView.setOnClickListener { onItemClick() }
    }

    override fun getItemCount(): Int {
        return expenseList.size
    }

    fun updateData(newList: List<Expense>) {
        expenseList = newList
        notifyDataSetChanged()
    }
}