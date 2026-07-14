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

        // Use the first expense's raw date in this group to decide Today / Yesterday.
        val rawDate = group.expenses.firstOrNull()?.date
        holder.binding.textDateHeader.text = headerLabel(rawDate, group.label)

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
            rowBinding.textCategory.text = "${e.category} · ${formatRowDate(e.date)}"
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
                lp.setMargins(0, 0, 0, 0)
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

    // Header: "Today" / "Yesterday" if it matches, otherwise the original label (e.g. "Monday, July 13").
    private fun headerLabel(rawDate: String?, fallbackLabel: String): String {
        val parsed = rawDate?.let { parseDate(it) } ?: return fallbackLabel

        val cal = java.util.Calendar.getInstance()
        val today = startOfDay(cal.time)

        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterday = startOfDay(cal.time)

        val target = startOfDay(parsed)

        return when (target) {
            today -> "TODAY"
            yesterday -> "YESTERDAY"
            else -> fallbackLabel
        }
    }

    // Row date: raw stored date -> "July 13"
    private fun formatRowDate(raw: String): String {
        val parsed = parseDate(raw) ?: return raw
        return java.text.SimpleDateFormat("MMMM d", java.util.Locale.getDefault()).format(parsed)
    }

    // Try parsing the raw stored date against known formats.
    private fun parseDate(raw: String): java.util.Date? {
        val patterns = listOf("yyyy-MM-dd", "MM/dd/yyyy hh:mm a", "MM/dd/yyyy", "yyyy-MM-dd HH:mm")
        for (p in patterns) {
            try {
                val d = java.text.SimpleDateFormat(p, java.util.Locale.getDefault()).parse(raw)
                if (d != null) return d
            } catch (_: Exception) { }
        }
        return null
    }

    // Zero out time so we compare dates only.
    private fun startOfDay(date: java.util.Date): Long {
        val c = java.util.Calendar.getInstance()
        c.time = date
        c.set(java.util.Calendar.HOUR_OF_DAY, 0)
        c.set(java.util.Calendar.MINUTE, 0)
        c.set(java.util.Calendar.SECOND, 0)
        c.set(java.util.Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
}