package com.et.expensetrackorapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.et.expensetrackorapp.data.local.Expense
import com.et.expensetrackorapp.databinding.ItemExpenseBinding

class ExpenseAdaptor(
    private var expenseList: List<Expense>,
    private val onItemClick: () -> Unit
) : RecyclerView.Adapter<ExpenseAdaptor.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(
        val binding: ItemExpenseBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]
        holder.binding.textTitle.text = expense.title
        holder.binding.textCategory.text = "${expense.category} · ${expense.date}"
        holder.binding.textAmount.text = "- Rs. ${expense.amount}"
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