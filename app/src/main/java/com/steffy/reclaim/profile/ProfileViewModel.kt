package com.steffy.reclaim.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel : ViewModel() {
    private val initialProfile = UserProfile(
        name = "Steffy",
        age = 26,
        heightCm = 160,
        startingWeightKg = 82.0,
        currentWeightKg = 82.0,
        goalWeightKg = 58.0,
        goalDate = "2027-06-30",
        plan = PlanType.ACCELERATED,
        journeyStartEpochDay = ProfileDates.todayEpochDay(),
    )

    private val _uiState = MutableStateFlow(initialProfile.toUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun openEditor() {
        _uiState.update { state ->
            state.copy(draft = ProfileDraft.from(state.profile), validationIssues = emptyMap())
        }
    }

    fun closeEditor() {
        _uiState.update { it.copy(draft = null, validationIssues = emptyMap()) }
    }

    fun updateDraft(draft: ProfileDraft) {
        _uiState.update { it.copy(draft = draft, validationIssues = emptyMap()) }
    }

    fun saveDraft() {
        _uiState.update { state ->
            val draft = state.draft ?: return@update state
            val issues = ProfileValidation.validate(draft)
            if (issues.isNotEmpty()) return@update state.copy(validationIssues = issues)
            val profile = ProfileValidation.toProfile(draft, state.profile) ?: return@update state
            profile.toUiState().copy(draft = null)
        }
    }

    private fun UserProfile.toUiState() = ProfileUiState(
        profile = this,
        metrics = ProfileCalculations.metrics(this),
    )
}