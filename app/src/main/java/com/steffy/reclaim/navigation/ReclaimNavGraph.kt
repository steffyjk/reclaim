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
import com.steffy.reclaim.feature.settings.ProfileGoalsScreen
import com.steffy.reclaim.feature.settings.SettingsScreen
import com.steffy.reclaim.profile.ProfileDraft
import com.steffy.reclaim.profile.ProfileUiState

@Composable
fun ReclaimNavGraph(
    selectedScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    profileUiState: ProfileUiState,
    onEditProfile: () -> Unit,
    onDraftChange: (ProfileDraft) -> Unit,
    onSaveProfile: () -> Unit,
    onCancelProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentDestination = if (selectedScreen == Screen.Settings && profileUiState.draft != null) {
        "profile_editor"
    } else {
        selectedScreen.name
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
                        onDraftChange = onDraftChange,
                        onSave = onSaveProfile,
                        onCancel = onCancelProfile,
                    )
                }
                Screen.Home.name -> HomeScreen(profileUiState)
                Screen.Progress.name -> ProgressScreen(profileUiState)
                Screen.Habits.name -> HabitsScreen()
                Screen.Settings.name -> SettingsScreen(
                    profile = profileUiState.profile,
                    onEditProfile = onEditProfile,
                )
                else -> HomeScreen(profileUiState)
            }
        }
    }
}