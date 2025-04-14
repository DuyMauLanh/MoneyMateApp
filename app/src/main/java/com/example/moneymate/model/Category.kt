package com.example.moneymate.model
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val user_id: Int,
    val name: String,
    val type: String, // "income" or "expense"
    val icon: Int?,
    val is_default: Boolean = false
)
