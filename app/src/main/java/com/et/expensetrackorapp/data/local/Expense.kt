package com.et.expensetrackorapp.data.local
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName="Expenses")
data class Expense (
    @PrimaryKey(autoGenerate=true)
    val id:Int=0,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String,
    val note: String?


)