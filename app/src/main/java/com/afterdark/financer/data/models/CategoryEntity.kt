package com.afterdark.financer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("profileId"),
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index(value = ["profileId","name"], unique = true)
    ]

)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id:Long = 0,

    val profileId:Long,
    val name: String,
    val currentExpense: Double = 0.0,
    val personalizedBudget:Double? = null,
    val createdAt:Long = System.currentTimeMillis()
)
