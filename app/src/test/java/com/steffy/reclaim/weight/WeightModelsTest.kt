package com.steffy.reclaim.weight

import com.steffy.reclaim.profile.PlanType
import com.steffy.reclaim.profile.ProfileCalculations
import com.steffy.reclaim.profile.ProfileDates
import com.steffy.reclaim.profile.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class WeightModelsTest {
    @Test
    fun localDayWindowUsesTimezoneAndDaylightSavingTransitions() {
        val losAngeles = TimeZone.getTimeZone("America/Los_Angeles")
        val middayOnSpringTransition = Calendar.getInstance(losAngeles).apply {
            clear()
            set(2025, Calendar.MARCH, 9, 12, 0)
        }.timeInMillis
        val bounds = WeightTime.localDayWindow(middayOnSpringTransition, losAngeles)

        assertEquals(23L * 60L * 60L * 1000L, bounds.endExclusive - bounds.startInclusive)
        assertTrue(WeightTime.isOnSameLocalDay(bounds.startInclusive, middayOnSpringTransition, losAngeles))
        assertTrue(!WeightTime.isOnSameLocalDay(bounds.endExclusive, middayOnSpringTransition, losAngeles))
    }

    @Test
    fun weightValidationRejectsInvalidRangeAndPrecision() {
        assertTrue(WeightValidation.parseKilograms("abc") is WeightValidation.Result.Invalid)
        assertEquals(
            WeightInputError.OutsideSupportedRange,
            (WeightValidation.parseKilograms("0") as WeightValidation.Result.Invalid).error,
        )
        assertEquals(
            WeightInputError.OutsideSupportedRange,
            (WeightValidation.parseKilograms("500.1") as WeightValidation.Result.Invalid).error,
        )
        assertEquals(
            WeightInputError.TooPrecise,
            (WeightValidation.parseKilograms("70.25") as WeightValidation.Result.Invalid).error,
        )
        assertEquals(70.2, (WeightValidation.parseKilograms("70.2") as WeightValidation.Result.Valid).weightKg, 0.001)
    }

    @Test
    fun historyDeltasCompareEachEntryToThePreviousChronologicalEntry() {
        val history = listOf(
            entry("latest", 78.4, 3L),
            entry("middle", 79.0, 2L),
            entry("oldest", 80.0, 1L),
        )

        val items = WeightCalculations.historyItems(history)

        assertEquals(-0.6, items[0].changeFromPreviousKg!!, 0.001)
        assertEquals(-1.0, items[1].changeFromPreviousKg!!, 0.001)
        assertEquals(null, items[2].changeFromPreviousKg)
    }

    @Test
    fun latestHistoryDrivesSummaryAndEmptyHistoryFallsBackToProfile() {
        val profile = profile()
        val history = listOf(entry("latest", 78.4, 2L), entry("older", 79.0, 1L))

        val fromHistory = WeightCalculations.summary(profile, history)
        val fallback = WeightCalculations.summary(profile, emptyList())

        assertEquals(78.4, fromHistory.currentWeightKg, 0.001)
        assertEquals(-3.6, fromHistory.totalChangeKg, 0.001)
        assertEquals(24.4, fromHistory.remainingWeightKg, 0.001)
        assertEquals(82.0, fallback.currentWeightKg, 0.001)
    }

    @Test
    fun remainingWeightNeverGoesNegativeAndProgressIsBoundedAtGoal() {
        val profile = profile().copy(currentWeightKg = 50.0)
        val metrics = ProfileCalculations.metrics(profile)

        assertEquals(0.0, metrics.remainingWeightKg, 0.0)
        assertEquals(1f, metrics.progressFraction)
    }

    private fun entry(id: String, weight: Double, timestamp: Long) = WeightEntry(
        id = id,
        weightKg = weight,
        recordedAt = timestamp,
        createdAt = timestamp,
    )

    private fun profile() = UserProfile(
        name = "Maya",
        age = 26,
        heightCm = 160,
        startingWeightKg = 82.0,
        currentWeightKg = 82.0,
        goalWeightKg = 54.0,
        goalDate = "2027-06-30",
        plan = PlanType.NORMAL,
        journeyStartEpochDay = ProfileDates.todayEpochDay(),
    )
}