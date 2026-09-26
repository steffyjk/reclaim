package com.steffy.reclaim.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steffy.reclaim.data.profile.ProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository,
) : ViewModel() {
    private val defaultProfile = developmentDefaults()

    private val _uiState = MutableStateFlow(defaultProfile.toUiState().copy(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.loadOrCreateProfile(defaultProfile)
                repository.observeProfile().filterNotNull().collect { profile ->
                    _uiState.update { state ->
                        state.copy(
                            profile = profile,
                            metrics = ProfileCalculations.metrics(profile),
                            isLoading = false,
                            persistenceError = false,
                        )
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, persistenceError = true) }
            }
        }
    }

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
        val state = _uiState.value
        val draft = state.draft ?: return
        if (state.isLoading || state.isSaving) return

        val issues = ProfileValidation.validate(draft)
        if (issues.isNotEmpty()) {
            _uiState.update { it.copy(validationIssues = issues) }
            return
        }
        val profile = ProfileValidation.toProfile(draft, state.profile) ?: return
        _uiState.update { it.copy(isSaving = true, persistenceError = false) }

        viewModelScope.launch {
            try {
                repository.saveProfile(profile)
                _uiState.update { current ->
                    current.copy(
                        profile = profile,
                        metrics = ProfileCalculations.metrics(profile),
                        draft = null,
                        validationIssues = emptyMap(),
                        isSaving = false,
                        persistenceError = false,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.update { it.copy(isSaving = false, persistenceError = true) }
            }
        }
    }

    private fun UserProfile.toUiState() = ProfileUiState(
        profile = this,
        metrics = ProfileCalculations.metrics(this),
    )

    companion object {
        fun developmentDefaults() = UserProfile(
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
    }
}