package com.steffy.reclaim.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ProfileEntity::class, WeightEntryEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class ReclaimDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun weightDao(): WeightDao

    companion object {
        const val DATABASE_NAME = "reclaim_database"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `weight_entries` " +
                        "(`id` TEXT NOT NULL, `weightKg` REAL NOT NULL, " +
                        "`recordedAt` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_weight_entries_recordedAt` " +
                        "ON `weight_entries` (`recordedAt`)",
                )
            }
        }

        @Volatile
        private var instance: ReclaimDatabase? = null

        fun getInstance(context: Context): ReclaimDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                ReclaimDatabase::class.java,
                DATABASE_NAME,
            ).addMigrations(MIGRATION_1_2)
                .build().also { instance = it }
        }
    }
}