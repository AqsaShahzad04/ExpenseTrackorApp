package com.et.expensetrackorapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.res.ResourcesCompat
import com.et.expensetrackorapp.data.local.Expense
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

object PdfReportBuilder {

    // A4 in points (1/72 inch)
    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 40f

    // dates: stored "yyyy-MM-dd", reference shows "M/d/yyyy"
    private val inFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val outFmt = SimpleDateFormat("M/d/yyyy", Locale.getDefault())

    private fun money(v: Double) = "Rs. " + "%,.0f".format(v)

    private fun prettyDate(raw: String): String = try {
        outFmt.format(inFmt.parse(raw)!!)
    } catch (e: Exception) { raw }

    fun buildPdf(
        context: Context,
        expenses: List<Expense>,
        budget: Double?,
        monthLabel: String            // e.g. "July 2026"
    ): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        // fonts: match the app (Fraunces serif for headings, default sans for body)
        val fraunces = ResourcesCompat.getFont(context, R.font.fraunces_bold)
        val sans = Typeface.SANS_SERIF

        val ink = Paint(Paint.ANTI_ALIAS_FLAG)   // one reusable pen
        var y = MARGIN + 20f

        // ---- Title: "Expense Report" ----
        ink.typeface = fraunces
        ink.color = Color.parseColor("#1F1F1F")
        ink.textSize = 30f
        canvas.drawText("Expense Report", MARGIN, y, ink)

        // ---- Subtitle: "July 2026 · 2 transactions" ----
        y += 22f
        val count = expenses.size
        val txnWord = if (count == 1) "transaction" else "transactions"
        ink.typeface = sans
        ink.color = Color.parseColor("#8A8172")
        ink.textSize = 12f
        canvas.drawText("$monthLabel  •  $count $txnWord", MARGIN, y, ink)

        // ---- Cream band: TOTAL SPENT (left) + BUDGET (right) ----
        y += 24f
        val bandTop = y
        val bandBottom = y + 70f
        ink.style = Paint.Style.FILL
        ink.color = Color.parseColor("#FBF8F2")
        canvas.drawRoundRect(
            RectF(MARGIN, bandTop, PAGE_W - MARGIN, bandBottom), 14f, 14f, ink
        )

        // caps labels
        ink.typeface = sans
        ink.color = Color.parseColor("#8A8172")
        ink.textSize = 10f
        canvas.drawText("TOTAL SPENT", MARGIN + 20f, bandTop + 26f, ink)
        val budgetCap = "BUDGET"
        val budgetCapW = ink.measureText(budgetCap)
        canvas.drawText(budgetCap, PAGE_W - MARGIN - 20f - budgetCapW, bandTop + 26f, ink)

        // big values
        val total = expenses.sumOf { it.amount }
        val budgetText = if (budget != null) money(budget) else "Not set"
        ink.typeface = fraunces
        ink.color = Color.parseColor("#1F1F1F")
        ink.textSize = 22f
        canvas.drawText(money(total), MARGIN + 20f, bandTop + 54f, ink)
        val budgetW = ink.measureText(budgetText)
        canvas.drawText(budgetText, PAGE_W - MARGIN - 20f - budgetW, bandTop + 54f, ink)

        // ---- Table header row ----
        y = bandBottom + 34f
        val colDate = MARGIN
        val colName = MARGIN + 90f
        val colCat = MARGIN + 270f
        val colAmtRight = PAGE_W - MARGIN     // amount is right-aligned

        ink.typeface = sans
        ink.color = Color.parseColor("#8A8172")
        ink.textSize = 10f
        canvas.drawText("DATE", colDate, y, ink)
        canvas.drawText("EXPENSE", colName, y, ink)
        canvas.drawText("CATEGORY", colCat, y, ink)
        val amtHead = "AMOUNT"
        canvas.drawText(amtHead, colAmtRight - ink.measureText(amtHead), y, ink)

        // underline under header
        y += 8f
        ink.color = Color.parseColor("#ECE6DA")
        ink.strokeWidth = 1f
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, ink)

        // ---- One row per expense ----
        ink.textSize = 13f
        for (e in expenses) {
            y += 26f

            ink.typeface = sans
            ink.color = Color.parseColor("#1F1F1F")
            canvas.drawText(prettyDate(e.date), colDate, y, ink)
            canvas.drawText(e.title, colName, y, ink)

            // category in orange, to match reference
            ink.color = Color.parseColor("#C77B3B")
            canvas.drawText(e.category, colCat, y, ink)

            // amount right-aligned, dark
            ink.color = Color.parseColor("#1F1F1F")
            val amtStr = money(e.amount)
            canvas.drawText(amtStr, colAmtRight - ink.measureText(amtStr), y, ink)

            // thin row divider
            y += 10f
            ink.color = Color.parseColor("#F1ECE2")
            canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, ink)
        }

        doc.finishPage(page)

        // save to app cache (we'll share/open it in the next step)
        val file = File(context.cacheDir, "expense-report.pdf")
        doc.writeTo(FileOutputStream(file))
        doc.close()
        return file
    }
}