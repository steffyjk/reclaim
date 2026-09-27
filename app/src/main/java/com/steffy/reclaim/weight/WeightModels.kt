package com.steffy.reclaim.weight

import androidx.compose.runtime.Immutable
import com.steffy.reclaim.profile.ProfileCalculations
import com.steffy.reclaim.profile.UserProfile
import java.util.Calendar
import java.util.TimeZone

@Immutable
data class WeightEntry(
    val id: String,
    val weightKg: Double,
    val recordedAt: Long,
    val createdAt: Long,
)

@Immutable
data class WeightHistoryItem(
    val entry: WeightEntry,
    val changeFromPreviousKg: Double?,
)

@Immutable
data class WeightChartPoint(
    val xFraction: Float,
    val yFraction: Float,
)

@Immutable
data class WeightTrackingSummary(
    val currentWeightKg: Double,
    val goalWeightKg: Double,
    val remainingWeightKg: Double,
    val totalChangeKg: Double,
    val progressFraction: Float,
    val progressPercent: Int,
)

@Immutable
data class LocalDayWindow(
    val startInclusive: Long,
    val endExclusive: Long,
)

enum class WeightInputError {
    InvalidNumber,
    OutsideSupportedRange,
    TooPrecise,
}

object WeightTime {
    fun localDayWindow(
        epochMillis: Long,
        timeZone: TimeZone = TimeZone.getDefault(),
    ): LocalDayWindow {
        val start = Calendar.getInstance(timeZone).apply {
            timeInMillis = epochMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = (start.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        return LocalDayWindow(start.timeInMillis, end.timeInMillis)
    }

    fun isOnSameLocalDay(
        firstEpochMillis: Long,
        secondEpochMillis: Long,
        timeZone: TimeZone = TimeZone.getDefault(),
    ): Boolean = localDayWindow(firstEpochMillis, timeZone) == localDayWindow(secondEpochMillis, timeZone)
}

object WeightValidation {
    fun parseKilograms(input: String): Result {
        val normalized = input.trim()
        val value = normalized.toDoubleOrNull()
            ?: return Result.Invalid(WeightInputError.InvalidNumber)
        if (!value.isFinite() || value <= 0.0 || value > 500.0) {
            return Result.Invalid(WeightInputError.OutsideSupportedRange)
        }
        val decimalPart = normalized.substringAfter('.', missingDelimiterValue = "")
        if (decimalPart.length > 1) {
            return Result.Invalid(WeightInputError.TooPrecise)
        }
        return Result.Valid(value)
    }

    sealed interface Result {
        data class Valid(val weightKg: Double) : Result
        data class Invalid(val error: WeightInputError) : Result
    }
}

object WeightCalculations {
    fun summary(profile: UserProfile, history: List<WeightEntry>): WeightTrackingSummary {
        val currentWeight = history.firstOrNull()?.weightKg ?: profile.currentWeightKg
        val metrics = ProfileCalculations.metrics(profile.copy(currentWeightKg = currentWeight))
        return WeightTrackingSummary(
            currentWeightKg = currentWeight,
            goalWeightKg = profile.goalWeightKg,
            remainingWeightKg = metrics.remainingWeightKg.coerceAtLeast(0.0),
            totalChangeKg = currentWeight - profile.startingWeightKg,
            progressFraction = metrics.progressFraction,
            progressPercent = metrics.progressPercent,
        )
    }

    fun historyItems(history: List<WeightEntry>): List<WeightHistoryItem> =
        history.mapIndexed { index, entry ->
            val previous = history.getOrNull(index + 1)
            WeightHistoryItem(
                entry = entry,
                changeFromPreviousKg = previous?.let { entry.weightKg - it.weightKg },
            )
        }

    fun chartPoints(history: List<WeightEntry>): List<WeightChartPoint> {
        if (history.size < 2) return emptyList()
        val chronological = history.asReversed()
        val firstTime = chronological.first().recordedAt
        val lastTime = chronological.last().recordedAt
        val timeRange = lastTime - firstTime
        val minWeight = chronological.minOf { it.weightKg }
        val maxWeight = chronological.maxOf { it.weightKg }
        val weightRange = maxWeight - minWeight

        return chronological.mapIndexed { index, entry ->
            val xFraction = if (timeRange > 0L) {
                (entry.recordedAt - firstTime).toFloat() / timeRange.toFloat()
            } else {
                index.toFloat() / (chronological.lastIndex).toFloat()
            }
            val yFraction = if (weightRange > 0.0) {
                ((maxWeight - entry.weightKg) / weightRange).toFloat()
            } else {
                0.5f
            }
            WeightChartPoint(xFraction, yFraction)
        }
    }
}