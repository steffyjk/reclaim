package com.steffy.reclaim.data.profile

import androidx.room.withTransaction
import com.steffy.reclaim.database.ProfileEntity
import com.steffy.reclaim.database.ReclaimDatabase
import com.steffy.reclaim.database.WeightEntryEntity
import com.steffy.reclaim.profile.PlanType
import com.steffy.reclaim.profile.UserProfile
import com.steffy.reclaim.weight.WeightEntry
import com.steffy.reclaim.weight.WeightTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface ProfileRepository {
    suspend fun loadOrCreateProfile(defaultProfile: UserProfile): UserProfile

    fun observeProfile(): Flow<UserProfile?>

    suspend fun saveProfile(profile: UserProfile)
}

interface WeightRepository {
    fun observeWeightHistory(): Flow<List<WeightEntry>>

    suspend fun saveTodaysWeight(weightKg: Double): WeightEntry

    suspend fun updateWeightEntry(id: String, weightKg: Double): WeightEntry?
}

class RoomProfileRepository(
    private val database: ReclaimDatabase,
) : ProfileRepository, WeightRepository {
    private val profileDao = database.profileDao()
    private val weightDao = database.weightDao()

    override suspend fun loadOrCreateProfile(defaultProfile: UserProfile): UserProfile =
        database.withTransaction {
            profileDao.insertIfMissing(defaultProfile.toEntity())
            requireNotNull(profileDao.loadProfile().toDomain())
        }

    override fun observeProfile(): Flow<UserProfile?> =
        profileDao.observeProfile().map { it.toDomain() }

    override suspend fun saveProfile(profile: UserProfile) {
        database.withTransaction {
            val latestEntry = weightDao.loadLatest()
            if (latestEntry != null && latestEntry.weightKg != profile.currentWeightKg) {
                upsertWeightForLocalDay(profile.currentWeightKg, System.currentTimeMillis())
            }
            val synchronizedWeight = weightDao.loadLatest()?.weightKg ?: profile.currentWeightKg
            profileDao.saveProfile(profile.copy(currentWeightKg = synchronizedWeight).toEntity())
        }
    }

    override fun observeWeightHistory(): Flow<List<WeightEntry>> =
        weightDao.observeHistory().map { entries -> entries.map(WeightEntryEntity::toDomain) }

    override suspend fun saveTodaysWeight(weightKg: Double): WeightEntry = database.withTransaction {
        val entry = upsertWeightForLocalDay(weightKg, System.currentTimeMillis())
        synchronizeCurrentProfileWeight()
        entry.toDomain()
    }

    override suspend fun updateWeightEntry(id: String, weightKg: Double): WeightEntry? =
        database.withTransaction {
            val existing = weightDao.loadById(id) ?: return@withTransaction null
            val corrected = existing.copy(weightKg = weightKg)
            if (weightDao.update(corrected) == 0) return@withTransaction null
            synchronizeCurrentProfileWeight()
            corrected.toDomain()
        }

    private suspend fun upsertWeightForLocalDay(weightKg: Double, recordedAt: Long): WeightEntryEntity {
        val window = WeightTime.localDayWindow(recordedAt)
        val todayEntry = weightDao.loadForLocalDay(window.startInclusive, window.endExclusive)
        if (todayEntry != null) {
            val updatedEntry = todayEntry.copy(weightKg = weightKg, recordedAt = recordedAt)
            weightDao.update(updatedEntry)
            return updatedEntry
        }

        val newEntry = WeightEntryEntity(
            id = UUID.randomUUID().toString(),
            weightKg = weightKg,
            recordedAt = recordedAt,
            createdAt = recordedAt,
        )
        weightDao.insert(newEntry)
        return newEntry
    }

    private suspend fun synchronizeCurrentProfileWeight() {
        val latestEntry = weightDao.loadLatest() ?: return
        val profile = profileDao.loadProfile() ?: return
        if (profile.currentWeightKg != latestEntry.weightKg) {
            profileDao.saveProfile(profile.copy(currentWeightKg = latestEntry.weightKg))
        }
    }
}

private fun UserProfile.toEntity() = ProfileEntity(
    profileId = ProfileEntity.CURRENT_PROFILE_ID,
    name = name,
    age = age,
    heightCm = heightCm,
    startingWeightKg = startingWeightKg,
    currentWeightKg = currentWeightKg,
    goalWeightKg = goalWeightKg,
    goalDate = goalDate,
    journeyStartEpochDay = journeyStartEpochDay,
    selectedPlan = plan.name,
)

private fun ProfileEntity?.toDomain(): UserProfile? = this?.let { entity ->
    UserProfile(
        name = entity.name,
        age = entity.age,
        heightCm = entity.heightCm,
        startingWeightKg = entity.startingWeightKg,
        currentWeightKg = entity.currentWeightKg,
        goalWeightKg = entity.goalWeightKg,
        goalDate = entity.goalDate,
        plan = PlanType.entries.firstOrNull { it.name == entity.selectedPlan } ?: PlanType.ACCELERATED,
        journeyStartEpochDay = entity.journeyStartEpochDay,
    )
}

private fun WeightEntryEntity.toDomain() = WeightEntry(
    id = id,
    weightKg = weightKg,
    recordedAt = recordedAt,
    createdAt = createdAt,
)