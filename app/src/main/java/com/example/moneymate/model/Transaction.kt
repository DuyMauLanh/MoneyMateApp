package com.example.moneymate.model
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val user_id: Int,
    val category_id: Int,
    val amount: Double,
    val note: String?,
    val transaction_date: String,
    val created_at: String
)
