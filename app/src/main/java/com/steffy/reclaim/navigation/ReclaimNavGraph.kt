package com.steffy.reclaim.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.steffy.reclaim.feature.habits.HabitsScreen
import com.steffy.reclaim.feature.home.HomeScreen
import com.steffy.reclaim.feature.progress.ProgressScreen
import com.steffy.reclaim.feature.progress.WeightTrackingScreen
import com.steffy.reclaim.feature.settings.ProfileGoalsScreen
import com.steffy.reclaim.feature.settings.SettingsScreen
import com.steffy.reclaim.profile.ProfileDraft
import com.steffy.reclaim.profile.ProfileUiState
import com.steffy.reclaim.weight.WeightTrackingUiState

@Composable
fun ReclaimNavGraph(
    selectedScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    profileUiState: ProfileUiState,
    onEditProfile: () -> Unit,
    onDraftChange: (ProfileDraft) -> Unit,
    onSaveProfile: () -> Unit,
    onCancelProfile: () -> Unit,
    isWeightTracking: Boolean,
    onOpenWeightTracking: () -> Unit,
    onCloseWeightTracking: () -> Unit,
    weightUiState: WeightTrackingUiState,
    onWeightInputChange: (String) -> Unit,
    onSaveWeight: () -> Unit,
    onEditWeightEntry: (String) -> Unit,
    onReturnToTodayWeight: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentDestination = when {
        selectedScreen == Screen.Settings && profileUiState.draft != null -> "profile_editor"
        selectedScreen == Screen.Progress && isWeightTracking -> "weight_tracking"
        else -> selectedScreen.name
    }
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Screen.entries.forEach { screen ->
                    NavigationBarItem(
                        selected = selectedScreen == screen,
                        onClick = { onScreenSelected(screen) },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(screen.labelRes)) },
                    )
                }
            }
        },
    ) { contentPadding ->
        AnimatedContent(
            targetState = contentDestination,
            modifier = Modifier.padding(contentPadding),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "destination_transition",
        ) { destination ->
            when (destination) {
                "profile_editor" -> profileUiState.draft?.let { draft ->
                    ProfileGoalsScreen(
                        draft = draft,
                        validationIssues = profileUiState.validationIssues,
                        isSaving = profileUiState.isSaving,
                        persistenceError = profileUiState.persistenceError,
                        onDraftChange = onDraftChange,
                        onSave = onSaveProfile,
                        onCancel = onCancelProfile,
                    )
                }
                Screen.Home.name -> HomeScreen(profileUiState)
                Screen.Progress.name -> ProgressScreen(
                    weightUiState = weightUiState,
                    onOpenWeightTracking = onOpenWeightTracking,
                )
                "weight_tracking" -> WeightTrackingScreen(
                    state = weightUiState,
                    onInputChange = onWeightInputChange,
                    onSave = onSaveWeight,
                    onEditEntry = onEditWeightEntry,
                    onReturnToToday = onReturnToTodayWeight,
                    onBack = onCloseWeightTracking,
                )
                Screen.Habits.name -> HabitsScreen()
                Screen.Settings.name -> SettingsScreen(
                    profile = profileUiState.profile,
                    isLoading = profileUiState.isLoading,
                    onEditProfile = onEditProfile,
                )
                else -> HomeScreen(profileUiState)
            }
        }
    }
}