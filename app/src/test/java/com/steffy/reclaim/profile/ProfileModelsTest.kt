package com.steffy.reclaim.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileModelsTest {
    @Test
    fun metricsDeriveRemainingProgressDaysAndJourneyDay() {
        val profile = sampleProfile()
        val today = requireNotNull(ProfileDates.parseEpochDay("2026-10-01"))
        val metrics = ProfileCalculations.metrics(profile, today)

        assertEquals(25.5, metrics.remainingWeightKg, 0.001)
        assertEquals((82.0 - 80.5) / (82.0 - 55.0), metrics.progressFraction.toDouble(), 0.001)
        assertEquals(6, metrics.progressPercent)
        assertEquals(
            requireNotNull(ProfileDates.parseEpochDay("2027-06-30")) - today,
            metrics.daysRemaining,
        )
        assertEquals(5L, metrics.journeyDay)
    }

    @Test
    fun progressIsBoundedAndZeroWeightRangeDoesNotDivideByZero() {
        val profile = sampleProfile().copy(
            startingWeightKg = 58.0,
            currentWeightKg = 58.0,
            goalWeightKg = 58.0,
        )

        assertEquals(0f, ProfileCalculations.metrics(profile).progressFraction)
        assertEquals(
            0f,
            ProfileCalculations.metrics(sampleProfile().copy(currentWeightKg = 90.0)).progressFraction,
        )
        assertEquals(
            1f,
            ProfileCalculations.metrics(sampleProfile().copy(currentWeightKg = 40.0)).progressFraction,
        )
    }

    @Test
    fun validationAcceptsDefaultShapedDraft() {
        assertTrue(ProfileValidation.validate(ProfileDraft.from(sampleProfile())).isEmpty())
    }

    @Test
    fun validationReportsInvalidFieldsWithoutThrowing() {
        val issues = ProfileValidation.validate(
            ProfileDraft.from(sampleProfile()).copy(
                name = "  ",
                age = "12",
                heightCm = "0",
                currentWeightKg = "NaN",
                goalWeightKg = "82",
                goalDate = "2027-02-29",
            ),
        )

        assertEquals(ValidationIssue.Required, issues[ProfileField.Name])
        assertEquals(ValidationIssue.AgeRange, issues[ProfileField.Age])
        assertEquals(ValidationIssue.HeightRange, issues[ProfileField.HeightCm])
        assertEquals(ValidationIssue.WeightRange, issues[ProfileField.CurrentWeightKg])
        assertEquals(ValidationIssue.GoalBelowStartingWeight, issues[ProfileField.GoalWeightKg])
        assertEquals(ValidationIssue.InvalidDate, issues[ProfileField.GoalDate])
    }

    @Test
    fun dateParserRejectsMalformedAndImpossibleDates() {
        assertNull(ProfileDates.parseEpochDay("2027-2-09"))
        assertNull(ProfileDates.parseEpochDay("2027-02-29"))
        assertFalse(ProfileValidation.validate(ProfileDraft.from(sampleProfile()).copy(goalDate = "")).isEmpty())
    }

    private fun sampleProfile() = UserProfile(
        name = "Ava",
        age = 26,
        heightCm = 160,
        startingWeightKg = 82.0,
        currentWeightKg = 80.5,
        goalWeightKg = 55.0,
        goalDate = "2027-06-30",
        plan = PlanType.NORMAL,
        journeyStartEpochDay = requireNotNull(ProfileDates.parseEpochDay("2026-09-27")),
    )
}