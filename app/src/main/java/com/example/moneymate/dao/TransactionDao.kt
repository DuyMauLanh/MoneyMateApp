package com.example.moneymate.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.moneymate.model.Transaction
import com.example.moneymate.model.TransactionWithCategory


@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Query("SELECT * FROM transactions WHERE user_id = :userId ORDER BY transaction_date DESC")
    suspend fun getAllByUser(userId: Int): List<Transaction>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Transaction?

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE transaction_date = :date")
    suspend fun getTransactionsByDate(date: String): List<Transaction>

    @androidx.room.Transaction
    @Query("SELECT * FROM transactions WHERE transaction_date = :date")
    suspend fun getTransactionsWithCategoryByDate(date: String): List<TransactionWithCategory>
}
