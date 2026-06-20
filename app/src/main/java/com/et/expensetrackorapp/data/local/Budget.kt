package com.et.expensetrackorapp.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Budgets",
    indices = [Index(value = ["month"], unique = true)]
)
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val month: String,
    val amount: Double
)