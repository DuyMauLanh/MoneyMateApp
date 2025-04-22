package com.example.moneymate.model

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionWithCategory(
    @Embedded val transaction: Transaction,

    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val category: Category
)
