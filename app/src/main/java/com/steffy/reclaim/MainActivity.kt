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
import com.steffy.reclaim.ui.theme.ReclaimTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        setContent {
            ReclaimTheme {
                var selectedScreen by rememberSaveable { mutableStateOf(Screen.Home) }
                val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()
                BackHandler(enabled = profileUiState.draft != null || selectedScreen != Screen.Home) {
                    if (profileUiState.draft != null) {
                        profileViewModel.closeEditor()
                    } else {
                        selectedScreen = Screen.Home
                    }
                }
                ReclaimNavGraph(
                    selectedScreen = selectedScreen,
                    onScreenSelected = { screen ->
                        if (profileUiState.draft != null) profileViewModel.closeEditor()
                        selectedScreen = screen
                    },
                    profileUiState = profileUiState,
                    onEditProfile = profileViewModel::openEditor,
                    onDraftChange = profileViewModel::updateDraft,
                    onSaveProfile = profileViewModel::saveDraft,
                    onCancelProfile = profileViewModel::closeEditor,
                )
            }
        }
    }
}
