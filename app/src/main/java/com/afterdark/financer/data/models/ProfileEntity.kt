package com.afterdark.financer.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "profiles",
    indices = [Index(value = ["name"], unique = true)]
)
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id:Long = 0,
    val name: String,
    var budget:Double,
    val createdAt:Long = System.currentTimeMillis()
)