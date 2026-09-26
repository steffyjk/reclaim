package com.steffy.reclaim.profile

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.steffy.reclaim.data.profile.RoomProfileRepository
import com.steffy.reclaim.database.ReclaimDatabase
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfilePersistenceTest {
    private lateinit var database: ReclaimDatabase
    private lateinit var repository: RoomProfileRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ReclaimDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RoomProfileRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun defaultsInsertOnceAndProfileCanBeReadBack() = runBlocking {
        val defaults = ProfileViewModel.developmentDefaults()
        val firstLoad = repository.loadOrCreateProfile(defaults)
        val secondLoad = repository.loadOrCreateProfile(defaults.copy(name = "Ignored default"))

        assertEquals(defaults, firstLoad)
        assertEquals(defaults, secondLoad)
        assertEquals(1, database.profileDao().profileCount())
    }

    @Test
    fun updateReplacesCurrentProfileAndRestoresSelectedPlan() = runBlocking {
        val defaults = ProfileViewModel.developmentDefaults()
        repository.loadOrCreateProfile(defaults)
        val updated = defaults.copy(
            name = "Ava",
            currentWeightKg = 79.5,
            goalWeightKg = 56.0,
            plan = PlanType.NORMAL,
        )

        repository.saveProfile(updated)

        assertEquals(updated, repository.loadOrCreateProfile(defaults))
        assertEquals(1, database.profileDao().profileCount())
        assertEquals(updated, withTimeout(5_000) { repository.observeProfile().filterNotNull().first() })
    }

    @Test
    fun viewModelRestoresAndPersistsProfileAcrossInstances() = runBlocking {
        val defaults = ProfileViewModel.developmentDefaults()
        val previouslySaved = defaults.copy(
            name = "Robin",
            currentWeightKg = 77.25,
            goalWeightKg = 54.0,
            plan = PlanType.NORMAL,
        )
        repository.loadOrCreateProfile(previouslySaved)

        val firstViewModel = ProfileViewModel(repository)
        val restored = withTimeout(5_000) {
            firstViewModel.uiState.first { !it.isLoading }
        }
        assertEquals(previouslySaved, restored.profile)

        firstViewModel.openEditor()
        firstViewModel.updateDraft(
            ProfileDraft.from(restored.profile).copy(
                name = "Jordan",
                currentWeightKg = "76.5",
                goalWeightKg = "53",
                plan = PlanType.ACCELERATED,
            ),
        )
        firstViewModel.saveDraft()

        val saved = withTimeout(5_000) {
            firstViewModel.uiState.first {
                !it.isSaving && it.draft == null && it.profile.name == "Jordan"
            }
        }
        assertEquals(PlanType.ACCELERATED, saved.profile.plan)
        assertEquals(76.5, saved.profile.currentWeightKg, 0.001)
        assertEquals(53.0, saved.profile.goalWeightKg, 0.001)

        val recreatedViewModel = ProfileViewModel(repository)
        val restoredAfterRecreation = withTimeout(5_000) {
            recreatedViewModel.uiState.first { !it.isLoading }
        }
        assertEquals("Jordan", restoredAfterRecreation.profile.name)
        assertEquals(PlanType.ACCELERATED, restoredAfterRecreation.profile.plan)
        assertEquals(76.5, restoredAfterRecreation.profile.currentWeightKg, 0.001)
        assertEquals(53.0, restoredAfterRecreation.profile.goalWeightKg, 0.001)
        assertNull(restoredAfterRecreation.draft)
    }
}
