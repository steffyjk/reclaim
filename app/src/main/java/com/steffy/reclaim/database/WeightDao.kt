package com.steffy.reclaim.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_entries ORDER BY recordedAt DESC, createdAt DESC, id DESC")
    fun observeHistory(): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries ORDER BY recordedAt DESC, createdAt DESC, id DESC")
    suspend fun loadHistory(): List<WeightEntryEntity>

    @Query("SELECT * FROM weight_entries ORDER BY recordedAt DESC, createdAt DESC, id DESC LIMIT 1")
    suspend fun loadLatest(): WeightEntryEntity?

    @Query("SELECT * FROM weight_entries WHERE id = :id LIMIT 1")
    suspend fun loadById(id: String): WeightEntryEntity?

    @Query(
        "SELECT * FROM weight_entries " +
            "WHERE recordedAt >= :startInclusive AND recordedAt < :endExclusive " +
            "ORDER BY recordedAt DESC, createdAt DESC, id DESC LIMIT 1",
    )
    suspend fun loadForLocalDay(startInclusive: Long, endExclusive: Long): WeightEntryEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: WeightEntryEntity)

    @Update
    suspend fun update(entry: WeightEntryEntity): Int

    @Query("SELECT COUNT(*) FROM weight_entries")
    suspend fun entryCount(): Int
}