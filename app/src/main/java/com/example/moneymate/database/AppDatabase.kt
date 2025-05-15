package com.example.moneymate.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.moneymate.dao.CategoryDao
import com.example.moneymate.dao.TransactionDao
import com.example.moneymate.dao.UserDao
import com.example.moneymate.model.Category
import com.example.moneymate.model.Transaction
import com.example.moneymate.model.User

@Database(entities = [User::class, Category::class, Transaction::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
}
