package com.steffy.reclaim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.steffy.reclaim.navigation.ReclaimNavGraph
import com.steffy.reclaim.navigation.Screen
import com.steffy.reclaim.profile.ProfileViewModel
import com.steffy.reclaim.profile.ProfileViewModelFactory
import com.steffy.reclaim.ui.theme.ReclaimTheme
import com.steffy.reclaim.weight.WeightTrackingViewModel
import com.steffy.reclaim.weight.WeightTrackingViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val reclaimApplication = application as ReclaimApplication
        val profileViewModel = ViewModelProvider(
            this,
            ProfileViewModelFactory(reclaimApplication.profileRepository),
        )[ProfileViewModel::class.java]
        val weightViewModel = ViewModelProvider(
            this,
            WeightTrackingViewModelFactory(
                reclaimApplication.weightRepository,
                profileViewModel.uiState,
            ),
        )[WeightTrackingViewModel::class.java]
        setContent {
            ReclaimTheme {
                var selectedScreen by rememberSaveable { mutableStateOf(Screen.Home) }
                var isWeightTracking by rememberSaveable { mutableStateOf(false) }
                val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()
                val weightUiState by weightViewModel.uiState.collectAsStateWithLifecycle()
                BackHandler(
                    enabled = profileUiState.draft != null || isWeightTracking || selectedScreen != Screen.Home,
                ) {
                    if (profileUiState.draft != null) {
                        profileViewModel.closeEditor()
                    } else if (isWeightTracking) {
                        isWeightTracking = false
                    } else {
                        selectedScreen = Screen.Home
                    }
                }
                ReclaimNavGraph(
                    selectedScreen = selectedScreen,
                    onScreenSelected = { screen ->
                        if (profileUiState.draft != null) profileViewModel.closeEditor()
                        isWeightTracking = false
                        selectedScreen = screen
                    },
                    isWeightTracking = isWeightTracking,
                    onOpenWeightTracking = {
                        weightViewModel.prepareTodayEntry()
                        isWeightTracking = true
                    },
                    onCloseWeightTracking = { isWeightTracking = false },
                    profileUiState = profileUiState,
                    onEditProfile = profileViewModel::openEditor,
                    onDraftChange = profileViewModel::updateDraft,
                    onSaveProfile = profileViewModel::saveDraft,
                    onCancelProfile = profileViewModel::closeEditor,
                    weightUiState = weightUiState,
                    onWeightInputChange = weightViewModel::onInputChanged,
                    onSaveWeight = weightViewModel::saveWeight,
                    onEditWeightEntry = weightViewModel::editEntry,
                    onReturnToTodayWeight = weightViewModel::returnToTodayEntry,
                )
            }
        }
    }
}
