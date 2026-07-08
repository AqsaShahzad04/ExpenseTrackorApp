package com.et.expensetrackorapp

import android.content.Context
import com.et.expensetrackorapp.data.local.Expense
import java.io.File
import java.io.FileOutputStream

object CsvReportBuilder {

    // commas inside a value would break columns, so swap them for spaces
    private fun clean(s: String) = s.replace(",", " ")

    fun buildCsv(
        context: Context,
        expenses: List<Expense>
    ): File {
        val sb = StringBuilder()

        // header row
        sb.append("DATE,EXPENSE,CATEGORY,AMOUNT\n")

        // one line per expense
        for (e in expenses) {
            sb.append("${e.date},${clean(e.title)},${clean(e.category)},${e.amount}\n")
        }

        // save to app cache (opened/shared via FileProvider, same as the PDF)
        val file = File(context.cacheDir, "expense-report.csv")
        FileOutputStream(file).use { it.write(sb.toString().toByteArray()) }
        return file
    }
}