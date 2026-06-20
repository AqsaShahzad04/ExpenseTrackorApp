package com.et.expensetrackorapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBudget(budget: Budget)

    @Query("SELECT * FROM Budgets WHERE month = :month LIMIT 1")
    fun getBudgetForMonth(month: String): Flow<Budget?>
}