package com.steffy.reclaim.weight

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.steffy.reclaim.data.profile.RoomProfileRepository
import com.steffy.reclaim.database.ReclaimDatabase
import com.steffy.reclaim.database.WeightEntryEntity
import com.steffy.reclaim.profile.ProfileCalculations
import com.steffy.reclaim.profile.ProfileUiState
import com.steffy.reclaim.profile.ProfileViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeightPersistenceTest {
    private lateinit var database: ReclaimDatabase
    private lateinit var repository: RoomProfileRepository

    @Before
    fun setUp() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            database = Room.inMemoryDatabaseBuilder(context, ReclaimDatabase::class.java)
                .allowMainThreadQueries()
                .build()
            repository = RoomProfileRepository(database)
            repository.loadOrCreateProfile(ProfileViewModel.developmentDefaults())
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun loggingAgainOnSameLocalDayUpdatesTheExistingEntry() = runBlocking {
        val first = repository.saveTodaysWeight(78.2)
        val updated = repository.saveTodaysWeight(78.7)
        val history = repository.observeWeightHistory().first()

        assertEquals(first.id, updated.id)
        assertEquals(1, history.size)
        assertEquals(78.7, history.single().weightKg, 0.001)
        assertEquals(first.createdAt, updated.createdAt)
        assertEquals(78.7, database.profileDao().loadProfile()?.currentWeightKg ?: 0.0, 0.001)
    }

    @Test
    fun historyIsReturnedInDescendingRecordedTimeOrder() = runBlocking {
        val now = System.currentTimeMillis()
        val dayMillis = 86_400_000L
        database.weightDao().insert(WeightEntryEntity("old", 81.0, now - dayMillis * 3, now - dayMillis * 3))
        database.weightDao().insert(WeightEntryEntity("middle", 80.0, now - dayMillis * 2, now - dayMillis * 2))
        database.weightDao().insert(WeightEntryEntity("recent", 79.0, now - dayMillis, now - dayMillis))

        val history = repository.observeWeightHistory().first()

        assertEquals(listOf("recent", "middle", "old"), history.map { it.id })
    }

    @Test
    fun correctingLatestAndOlderEntriesKeepsProfileSyncedToLatest() = runBlocking {
        val now = System.currentTimeMillis()
        val older = WeightEntryEntity("older-entry", 81.0, now - 86_400_000L, now - 86_400_000L)
        database.weightDao().insert(older)
        val latest = repository.saveTodaysWeight(77.7)
        assertEquals(77.7, database.profileDao().loadProfile()?.currentWeightKg ?: 0.0, 0.001)

        repository.updateWeightEntry(latest.id, 77.4)
        assertEquals(77.4, database.profileDao().loadProfile()?.currentWeightKg ?: 0.0, 0.001)

        repository.updateWeightEntry(older.id, 82.0)
        assertEquals(77.4, database.profileDao().loadProfile()?.currentWeightKg ?: 0.0, 0.001)
        assertEquals(82.0, database.weightDao().loadById(older.id)?.weightKg ?: 0.0, 0.001)
    }

    @Test
    fun profileEditorCurrentWeightChangeUpdatesTodaysHistoryWhenTrackingHasStarted() = runBlocking {
        val firstEntry = repository.saveTodaysWeight(78.0)
        val editedProfile = repository.loadOrCreateProfile(ProfileViewModel.developmentDefaults()).copy(
            currentWeightKg = 77.2,
            goalWeightKg = 53.0,
        )

        repository.saveProfile(editedProfile)

        val history = repository.observeWeightHistory().first()
        assertEquals(1, history.size)
        assertEquals(firstEntry.id, history.single().id)
        assertEquals(77.2, history.single().weightKg, 0.001)
        assertEquals(77.2, database.profileDao().loadProfile()?.currentWeightKg ?: 0.0, 0.001)
        assertEquals(53.0, repository.loadOrCreateProfile(editedProfile).goalWeightKg, 0.001)
    }

    @Test
    fun invalidViewModelInputIsRejectedWithoutWriting() = runBlocking {
        val profile = ProfileViewModel.developmentDefaults()
        val profileState = MutableStateFlow(
            ProfileUiState(
                profile = profile,
                metrics = ProfileCalculations.metrics(profile),
                isLoading = false,
            ),
        )
        val viewModel = WeightTrackingViewModel(repository, profileState)
        withTimeout(5_000) { viewModel.uiState.first { !it.isLoading } }
        viewModel.prepareTodayEntry()
        viewModel.onInputChanged("0")
        viewModel.saveWeight()

        val rejected = withTimeout(5_000) {
            viewModel.uiState.first { it.validationError == WeightInputError.OutsideSupportedRange }
        }

        assertNull(rejected.editingEntryId)
        assertEquals(0, database.weightDao().entryCount())
    }

    @Test
    fun validViewModelSaveExposesLatestAndUpdatedProfileWeight() = runBlocking {
        val profile = ProfileViewModel.developmentDefaults()
        val profileState = MutableStateFlow(
            ProfileUiState(profile = profile, metrics = ProfileCalculations.metrics(profile), isLoading = false),
        )
        val viewModel = WeightTrackingViewModel(repository, profileState)
        withTimeout(5_000) { viewModel.uiState.first { !it.isLoading } }
        viewModel.prepareTodayEntry()
        viewModel.onInputChanged("79.3")
        viewModel.saveWeight()

        val saved = withTimeout(5_000) {
            viewModel.uiState.first { it.saveSucceeded && !it.isSaving && it.history.isNotEmpty() }
        }

        assertEquals(79.3, saved.summary.currentWeightKg, 0.001)
        assertEquals(79.3, database.profileDao().loadProfile()?.currentWeightKg ?: 0.0, 0.001)
        assertNotEquals(profile.currentWeightKg, saved.summary.currentWeightKg)
        assertTrue(saved.todayEntryExists)
    }
}