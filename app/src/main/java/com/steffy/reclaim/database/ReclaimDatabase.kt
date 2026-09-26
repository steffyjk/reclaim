package com.steffy.reclaim.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ProfileEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class ReclaimDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao

    companion object {
        const val DATABASE_NAME = "reclaim_database"

        @Volatile
        private var instance: ReclaimDatabase? = null

        fun getInstance(context: Context): ReclaimDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                ReclaimDatabase::class.java,
                DATABASE_NAME,
            ).build().also { instance = it }
        }
    }
}