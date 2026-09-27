package com.steffy.reclaim.weight

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steffy.reclaim.data.profile.WeightRepository
import com.steffy.reclaim.profile.ProfileViewModel
import com.steffy.reclaim.profile.ProfileUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

@Immutable
data class WeightTrackingUiState(
    val summary: WeightTrackingSummary,
    val history: List<WeightHistoryItem> = emptyList(),
    val chartPoints: List<WeightChartPoint> = emptyList(),
    val input: String = "",
    val todayEntryExists: Boolean = false,
    val editingEntryId: String? = null,
    val editingRecordedAt: Long? = null,
    val validationError: WeightInputError? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSucceeded: Boolean = false,
    val persistenceError: Boolean = false,
)

private data class WeightEditorState(
    val input: String = "",
    val editingEntryId: String? = null,
    val validationError: WeightInputError? = null,
    val isSaving: Boolean = false,
    val saveSucceeded: Boolean = false,
    val persistenceError: Boolean = false,
)

class WeightTrackingViewModel(
    private val repository: WeightRepository,
    profileState: StateFlow<ProfileUiState>,
) : ViewModel() {
    private val editorState = MutableStateFlow(WeightEditorState())

    val uiState: StateFlow<WeightTrackingUiState> = combine(
        profileState,
        repository.observeWeightHistory(),
        editorState,
    ) { profileUiState, history, editor ->
        val today = System.currentTimeMillis()
        val editingEntry = history.firstOrNull { it.id == editor.editingEntryId }
        WeightTrackingUiState(
            summary = WeightCalculations.summary(profileUiState.profile, history),
            history = WeightCalculations.historyItems(history),
            chartPoints = WeightCalculations.chartPoints(history),
            input = editor.input,
            todayEntryExists = history.any { WeightTime.isOnSameLocalDay(it.recordedAt, today) },
            editingEntryId = editor.editingEntryId,
            editingRecordedAt = editingEntry?.recordedAt,
            validationError = editor.validationError,
            isLoading = profileUiState.isLoading,
            isSaving = editor.isSaving,
            saveSucceeded = editor.saveSucceeded,
            persistenceError = editor.persistenceError || profileUiState.persistenceError,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = WeightTrackingUiState(
            summary = WeightCalculations.summary(ProfileViewModel.developmentDefaults(), emptyList()),
        ),
    )

    fun prepareTodayEntry() {
        val state = uiState.value
        val today = System.currentTimeMillis()
        val existingToday = state.history
            .firstOrNull { WeightTime.isOnSameLocalDay(it.entry.recordedAt, today) }
            ?.entry
        editorState.update {
            it.copy(
                input = formatInput(existingToday?.weightKg ?: state.summary.currentWeightKg),
                editingEntryId = null,
                validationError = null,
                saveSucceeded = false,
                persistenceError = false,
            )
        }
    }

    fun editEntry(id: String) {
        val entry = uiState.value.history.firstOrNull { it.entry.id == id }?.entry ?: return
        editorState.update {
            it.copy(
                input = formatInput(entry.weightKg),
                editingEntryId = entry.id,
                validationError = null,
                saveSucceeded = false,
                persistenceError = false,
            )
        }
    }

    fun returnToTodayEntry() {
        prepareTodayEntry()
    }

    fun onInputChanged(value: String) {
        editorState.update {
            it.copy(input = value, validationError = null, saveSucceeded = false, persistenceError = false)
        }
    }

    fun saveWeight() {
        val current = editorState.value
        if (current.isSaving || uiState.value.isLoading) return
        val validation = WeightValidation.parseKilograms(current.input)
        if (validation is WeightValidation.Result.Invalid) {
            editorState.update { it.copy(validationError = validation.error, saveSucceeded = false) }
            return
        }
        val weightKg = (validation as WeightValidation.Result.Valid).weightKg
        editorState.update { it.copy(isSaving = true, validationError = null, persistenceError = false) }

        viewModelScope.launch {
            try {
                val correctingHistory = current.editingEntryId != null
                val savedEntry = current.editingEntryId?.let { id ->
                    repository.updateWeightEntry(id, weightKg)
                        ?: throw IllegalStateException("The selected weight entry no longer exists")
                } ?: repository.saveTodaysWeight(weightKg)
                editorState.update {
                    it.copy(
                        input = formatInput(savedEntry.weightKg),
                        editingEntryId = if (correctingHistory) savedEntry.id else null,
                        isSaving = false,
                        saveSucceeded = true,
                        persistenceError = false,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                editorState.update { it.copy(isSaving = false, persistenceError = true) }
            }
        }
    }

    private fun formatInput(value: Double): String = String.format(Locale.US, "%.1f", value)
}