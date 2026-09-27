package com.steffy.reclaim.profile

import androidx.compose.runtime.Immutable
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

enum class PlanType {
    NORMAL,
    ACCELERATED,
}

enum class ProfileField {
    Name,
    Age,
    HeightCm,
    StartingWeightKg,
    CurrentWeightKg,
    GoalWeightKg,
    GoalDate,
}

enum class ValidationIssue {
    Required,
    AgeRange,
    HeightRange,
    WeightRange,
    GoalBelowStartingWeight,
    InvalidDate,
}

@Immutable
data class UserProfile(
    val name: String,
    val age: Int,
    val heightCm: Int,
    val startingWeightKg: Double,
    val currentWeightKg: Double,
    val goalWeightKg: Double,
    val goalDate: String,
    val plan: PlanType,
    val journeyStartEpochDay: Long,
)

@Immutable
data class ProfileDraft(
    val name: String,
    val age: String,
    val heightCm: String,
    val startingWeightKg: String,
    val currentWeightKg: String,
    val goalWeightKg: String,
    val goalDate: String,
    val plan: PlanType,
) {
    companion object {
        fun from(profile: UserProfile) = ProfileDraft(
            name = profile.name,
            age = profile.age.toString(),
            heightCm = profile.heightCm.toString(),
            startingWeightKg = profile.startingWeightKg.toInputString(),
            currentWeightKg = profile.currentWeightKg.toInputString(),
            goalWeightKg = profile.goalWeightKg.toInputString(),
            goalDate = profile.goalDate,
            plan = profile.plan,
        )
    }
}

@Immutable
data class ProfileMetrics(
    val remainingWeightKg: Double,
    val progressFraction: Float,
    val progressPercent: Int,
    val daysRemaining: Long,
    val journeyDay: Long,
)

@Immutable
data class ProfileUiState(
    val profile: UserProfile,
    val metrics: ProfileMetrics,
    val draft: ProfileDraft? = null,
    val validationIssues: Map<ProfileField, ValidationIssue> = emptyMap(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val persistenceError: Boolean = false,
)

object ProfileDates {
    private const val MILLIS_PER_DAY = 86_400_000L

    fun todayEpochDay(): Long {
        val localDate = Calendar.getInstance()
        val utcDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US).apply {
            clear()
            set(
                localDate.get(Calendar.YEAR),
                localDate.get(Calendar.MONTH),
                localDate.get(Calendar.DAY_OF_MONTH),
            )
        }
        return utcDate.timeInMillis / MILLIS_PER_DAY
    }

    fun parseEpochDay(value: String): Long? {
        if (!value.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return null
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val position = ParsePosition(0)
        val parsed: Date = format.parse(value, position) ?: return null
        if (position.index != value.length) return null
        if (format.format(parsed) != value) return null
        return parsed.time / MILLIS_PER_DAY
    }

    fun formatEpochDay(epochDay: Long): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return format.format(Date(epochDay * MILLIS_PER_DAY))
    }
}

object ProfileCalculations {
    fun metrics(profile: UserProfile, todayEpochDay: Long = ProfileDates.todayEpochDay()): ProfileMetrics {
        val weightRange = profile.startingWeightKg - profile.goalWeightKg
        val progress = if (weightRange > 0.0) {
            ((profile.startingWeightKg - profile.currentWeightKg) / weightRange)
                .coerceIn(0.0, 1.0)
                .toFloat()
        } else {
            0f
        }
        return ProfileMetrics(
            remainingWeightKg = (profile.currentWeightKg - profile.goalWeightKg).coerceAtLeast(0.0),
            progressFraction = progress,
            progressPercent = (progress * 100f).roundToInt(),
            daysRemaining = ProfileDates.parseEpochDay(profile.goalDate)
                ?.minus(todayEpochDay)
                ?: 0L,
            journeyDay = (todayEpochDay - profile.journeyStartEpochDay + 1L).coerceAtLeast(1L),
        )
    }
}

object ProfileValidation {
    fun validate(draft: ProfileDraft): Map<ProfileField, ValidationIssue> {
        val issues = linkedMapOf<ProfileField, ValidationIssue>()

        if (draft.name.isBlank()) issues[ProfileField.Name] = ValidationIssue.Required

        val age = draft.age.toIntOrNull()
        if (age == null || age !in 13..120) {
            issues[ProfileField.Age] = ValidationIssue.AgeRange
        }

        val height = draft.heightCm.toIntOrNull()
        if (height == null || height !in 50..275) {
            issues[ProfileField.HeightCm] = ValidationIssue.HeightRange
        }

        val startingWeight = parseWeight(draft.startingWeightKg)
        val currentWeight = parseWeight(draft.currentWeightKg)
        val goalWeight = parseWeight(draft.goalWeightKg)
        if (startingWeight == null) issues[ProfileField.StartingWeightKg] = ValidationIssue.WeightRange
        if (currentWeight == null) issues[ProfileField.CurrentWeightKg] = ValidationIssue.WeightRange
        if (goalWeight == null) issues[ProfileField.GoalWeightKg] = ValidationIssue.WeightRange
        if (startingWeight != null && goalWeight != null && goalWeight >= startingWeight) {
            issues[ProfileField.GoalWeightKg] = ValidationIssue.GoalBelowStartingWeight
        }

        if (ProfileDates.parseEpochDay(draft.goalDate) == null) {
            issues[ProfileField.GoalDate] = ValidationIssue.InvalidDate
        }
        return issues
    }

    fun toProfile(draft: ProfileDraft, current: UserProfile): UserProfile? {
        if (validate(draft).isNotEmpty()) return null
        return UserProfile(
            name = draft.name.trim(),
            age = requireNotNull(draft.age.toIntOrNull()),
            heightCm = requireNotNull(draft.heightCm.toIntOrNull()),
            startingWeightKg = requireNotNull(parseWeight(draft.startingWeightKg)),
            currentWeightKg = requireNotNull(parseWeight(draft.currentWeightKg)),
            goalWeightKg = requireNotNull(parseWeight(draft.goalWeightKg)),
            goalDate = draft.goalDate.trim(),
            plan = draft.plan,
            journeyStartEpochDay = current.journeyStartEpochDay,
        )
    }

    private fun parseWeight(value: String): Double? = value.toDoubleOrNull()?.takeIf {
        it.isFinite() && it in 1.0..500.0
    }
}

private fun Double.toInputString(): String = if (this % 1.0 == 0.0) {
    toLong().toString()
} else {
    toString()
}