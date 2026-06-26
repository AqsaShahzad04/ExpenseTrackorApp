package com.et.expensetrackorapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.et.expensetrackorapp.databinding.ItemDateHeaderBinding
import com.et.expensetrackorapp.databinding.ItemExpenseBinding

class HistoryAdaptor(
    private var items: List<HistoryItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_HEADER = 0
    private val TYPE_EXPENSE = 1

    // 1. Tell the RecyclerView which kind each row is
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HistoryItem.Header -> TYPE_HEADER
            is HistoryItem.ExpenseRow -> TYPE_EXPENSE
        }
    }

    // 2. Two holders, one per layout
    inner class HeaderViewHolder(val binding: ItemDateHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ExpenseViewHolder(val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root)

    // 3. Inflate the right layout based on the type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(ItemDateHeaderBinding.inflate(inflater, parent, false))
        } else {
            ExpenseViewHolder(ItemExpenseBinding.inflate(inflater, parent, false))
        }
    }

    // 4. Fill the right holder based on the item
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is HeaderViewHolder && item is HistoryItem.Header) {
            holder.binding.textDateHeader.text = item.label
        } else if (holder is ExpenseViewHolder && item is HistoryItem.ExpenseRow) {
            val e = item.expense
            holder.binding.textTitle.text = e.title
            holder.binding.textCategory.text = "${e.category} · ${e.date}"
            holder.binding.textAmount.text = "- Rs. ${"%,.0f".format(e.amount)}"
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<HistoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}