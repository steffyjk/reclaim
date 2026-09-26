package com.steffy.reclaim.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile_configuration WHERE profileId = :profileId LIMIT 1")
    fun observeProfile(profileId: Int = ProfileEntity.CURRENT_PROFILE_ID): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile_configuration WHERE profileId = :profileId LIMIT 1")
    suspend fun loadProfile(profileId: Int = ProfileEntity.CURRENT_PROFILE_ID): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(profile: ProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: ProfileEntity)

    @Query("SELECT COUNT(*) FROM profile_configuration")
    suspend fun profileCount(): Int
}