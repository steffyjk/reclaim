package com.steffy.reclaim.data.profile

import androidx.room.withTransaction
import com.steffy.reclaim.database.ProfileEntity
import com.steffy.reclaim.database.ReclaimDatabase
import com.steffy.reclaim.profile.PlanType
import com.steffy.reclaim.profile.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ProfileRepository {
    suspend fun loadOrCreateProfile(defaultProfile: UserProfile): UserProfile

    fun observeProfile(): Flow<UserProfile?>

    suspend fun saveProfile(profile: UserProfile)
}

class RoomProfileRepository(
    private val database: ReclaimDatabase,
) : ProfileRepository {
    private val profileDao = database.profileDao()

    override suspend fun loadOrCreateProfile(defaultProfile: UserProfile): UserProfile =
        database.withTransaction {
            profileDao.insertIfMissing(defaultProfile.toEntity())
            requireNotNull(profileDao.loadProfile().toDomain())
        }

    override fun observeProfile(): Flow<UserProfile?> =
        profileDao.observeProfile().map { it.toDomain() }

    override suspend fun saveProfile(profile: UserProfile) {
        profileDao.saveProfile(profile.toEntity())
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