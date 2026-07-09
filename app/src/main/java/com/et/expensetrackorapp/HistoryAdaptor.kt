package com.et.expensetrackorapp

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.et.expensetrackorapp.databinding.ItemHistoryGroupBinding
import com.et.expensetrackorapp.databinding.ItemExpenseRowBinding
import com.et.expensetrackorapp.data.local.Expense
import com.et.expensetrackorapp.viewmodel.CategoryColors
import com.et.expensetrackorapp.viewmodel.CategoryIcons

class HistoryAdaptor(
    private var items: List<HistoryGroup>,
    private val onExpenseClick: (Expense) -> Unit
) : RecyclerView.Adapter<HistoryAdaptor.GroupViewHolder>() {

    inner class GroupViewHolder(val binding: ItemHistoryGroupBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return GroupViewHolder(ItemHistoryGroupBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = items[position]
        holder.binding.textDateHeader.text = group.label

        val container = holder.binding.groupContainer
        container.removeAllViews()
        val inflater = LayoutInflater.from(container.context)

        group.expenses.forEachIndexed { index, e ->
            val rowBinding = ItemExpenseRowBinding.inflate(inflater, container, false)
            rowBinding.imageIcon.setImageResource(CategoryIcons.iconFor(e.category))
            rowBinding.imageIcon.setColorFilter(CategoryColors.colorFor(e.category))
            rowBinding.iconCircle.backgroundTintList =
                ColorStateList.valueOf(CategoryColors.lightBgFor(e.category))
            rowBinding.textTitle.text = e.title
            rowBinding.textCategory.text = "${e.category} · ${e.date}"
            rowBinding.textAmount.text = "- ${com.et.expensetrackorapp.util.CurrencyFormatter.format(container.context, e.amount)}"
            rowBinding.root.setOnClickListener { onExpenseClick(e) }
            container.addView(rowBinding.root)

            // thin divider between rows, not after the last
            if (index < group.expenses.size - 1) {
                val divider = android.view.View(container.context)
                val lp = android.widget.LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (1 * container.resources.displayMetrics.density).toInt()
                )
                val side = (16 * container.resources.displayMetrics.density).toInt()
                lp.setMargins(side, 0, side, 0)
                divider.layoutParams = lp
                divider.setBackgroundColor(android.graphics.Color.parseColor("#E0D7C8"))
                container.addView(divider)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<HistoryGroup>) {
        items = newItems
        notifyDataSetChanged()
    }
}