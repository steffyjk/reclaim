package com.steffy.reclaim.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weight_entries",
    indices = [Index(value = ["recordedAt"])],
)
data class WeightEntryEntity(
    @PrimaryKey val id: String,
    val weightKg: Double,
    val recordedAt: Long,
    val createdAt: Long,
)