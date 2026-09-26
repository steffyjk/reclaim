package com.steffy.reclaim.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_configuration")
data class ProfileEntity(
    @PrimaryKey val profileId: Int,
    val name: String,
    val age: Int,
    val heightCm: Int,
    val startingWeightKg: Double,
    val currentWeightKg: Double,
    val goalWeightKg: Double,
    val goalDate: String,
    val journeyStartEpochDay: Long,
    val selectedPlan: String,
) {
    companion object {
        const val CURRENT_PROFILE_ID = 1
    }
}