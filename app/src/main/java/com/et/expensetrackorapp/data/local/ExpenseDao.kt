package com.et.expensetrackorapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense:Expense)

    @Update
    suspend fun update(expense:Expense)

    @Delete
    suspend fun delete(expense:Expense)

    @Query("SELECT  * FROM Expenses ORDER BY id DESC" )
    fun getAllExpenses() : Flow<List<Expense>>

}