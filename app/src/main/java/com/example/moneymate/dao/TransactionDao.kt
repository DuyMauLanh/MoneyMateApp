package com.example.moneymate.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.moneymate.model.CategoryReport
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
    @Query("SELECT * FROM transactions WHERE transaction_date = :date  AND user_id = :userId")
    suspend fun getTransactionsWithCategoryByDate(
        date: String,
        userId: Int
    ): List<TransactionWithCategory>


    @androidx.room.Transaction
    @Query(
        """
    SELECT c.labelKey AS categoryName, 
           c.icon AS categoryIcon, 
           SUM(t.amount) AS totalAmount
    FROM transactions t
    INNER JOIN categories c ON t.category_id = c.id
    WHERE c.type = :type
      AND t.user_id = :userId
        AND t.transaction_date LIKE :datePrefix || '%'
    GROUP BY c.id
"""
    )
    suspend fun getMonthlyReport(
        userId: Int,
        type: String,
        datePrefix: String
    ): List<CategoryReport>

    @androidx.room.Transaction
    @Query(
        """
    SELECT c.labelKey AS categoryName, 
           c.icon AS categoryIcon, 
           SUM(t.amount) AS totalAmount
    FROM transactions t
    INNER JOIN categories c ON t.category_id = c.id
    WHERE c.type = :type
      AND t.user_id = :userId
        AND t.transaction_date LIKE :yearPrefix || '%'
    GROUP BY c.id
"""
    )
    suspend fun getYearlyReport(userId: Int, type: String, yearPrefix: String): List<CategoryReport>

}
