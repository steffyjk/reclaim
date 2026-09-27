package com.steffy.reclaim.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeightMigrationTest {
    @get:Rule
    val migrationHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ReclaimDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migration1To2PreservesProfileAndCreatesWeightHistoryTable() {
        val databaseName = "reclaim-migration-test"
        migrationHelper.createDatabase(databaseName, 1).apply {
            execSQL(
                "INSERT INTO profile_configuration " +
                    "(profileId, name, age, heightCm, startingWeightKg, currentWeightKg, " +
                    "goalWeightKg, goalDate, journeyStartEpochDay, selectedPlan) " +
                    "VALUES (1, 'Maya', 26, 160, 82.0, 78.4, 54.0, '2027-06-30', 20600, 'NORMAL')",
            )
            close()
        }

        val migrated = migrationHelper.runMigrationsAndValidate(
            databaseName,
            2,
            true,
            ReclaimDatabase.MIGRATION_1_2,
        )
        migrated.query(
            "SELECT name, currentWeightKg, selectedPlan FROM profile_configuration WHERE profileId = 1",
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Maya", cursor.getString(0))
            assertEquals(78.4, cursor.getDouble(1), 0.001)
            assertEquals("NORMAL", cursor.getString(2))
        }
        migrated.query("SELECT COUNT(*) FROM weight_entries").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        migrated.close()
    }
}