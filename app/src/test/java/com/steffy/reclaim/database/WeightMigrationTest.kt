package com.steffy.reclaim.database

import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WeightMigrationTest {
    @Test
    fun migrationPreservesProfileAndCreatesIndexedWeightHistory() {
        val context = RuntimeEnvironment.getApplication()
        val databaseName = "reclaim-v1-migration-test.db"
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                        database.execSQL(
                            "CREATE TABLE profile_configuration (" +
                                "profileId INTEGER NOT NULL PRIMARY KEY, name TEXT NOT NULL, " +
                                "age INTEGER NOT NULL, heightCm INTEGER NOT NULL, " +
                                "startingWeightKg REAL NOT NULL, currentWeightKg REAL NOT NULL, " +
                                "goalWeightKg REAL NOT NULL, goalDate TEXT NOT NULL, " +
                                "journeyStartEpochDay INTEGER NOT NULL, selectedPlan TEXT NOT NULL)",
                        )
                        database.execSQL(
                            "INSERT INTO profile_configuration VALUES " +
                                "(1, 'Maya', 26, 160, 82.0, 78.4, 54.0, '2027-06-30', 20600, 'NORMAL')",
                        )
                    }

                    override fun onUpgrade(
                        database: androidx.sqlite.db.SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int,
                    ) = error("The test fixture must remain at schema version 1 before migration")
                })
                .build(),
        )

        try {
            val database = helper.writableDatabase
            ReclaimDatabase.MIGRATION_1_2.migrate(database)

            database.query(
                "SELECT name, currentWeightKg, selectedPlan FROM profile_configuration WHERE profileId = 1",
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("Maya", cursor.getString(0))
                assertEquals(78.4, cursor.getDouble(1), 0.001)
                assertEquals("NORMAL", cursor.getString(2))
            }
            database.query("SELECT COUNT(*) FROM weight_entries").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(0, cursor.getInt(0))
            }
            database.query(
                "SELECT COUNT(*) FROM sqlite_master " +
                    "WHERE type = 'index' AND name = 'index_weight_entries_recordedAt'",
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(1, cursor.getInt(0))
            }
        } finally {
            helper.close()
            context.deleteDatabase(databaseName)
        }
    }
}