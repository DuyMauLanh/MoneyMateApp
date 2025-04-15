package com.example.moneymate.dao
import androidx.room.*
import com.example.moneymate.model.Category

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: Category): Long

    @Query("SELECT * FROM categories WHERE user_id = :userId")
    suspend fun getAllByUser(userId: Int): List<Category>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Category?

    @Delete
    suspend fun delete(category: Category)

    @Update
    suspend fun update(category: Category)

    @Query("SELECT * FROM categories WHERE user_id = :userId")
    suspend fun getCategoriesByUser(userId: Int): List<Category>
}
