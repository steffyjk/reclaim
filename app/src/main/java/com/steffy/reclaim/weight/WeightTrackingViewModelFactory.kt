package com.steffy.reclaim.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.steffy.reclaim.data.profile.WeightRepository
import com.steffy.reclaim.profile.ProfileUiState
import kotlinx.coroutines.flow.StateFlow

class WeightTrackingViewModelFactory(
    private val repository: WeightRepository,
    private val profileState: StateFlow<ProfileUiState>,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(WeightTrackingViewModel::class.java)) {
            throw IllegalArgumentException("Unsupported ViewModel type: ${modelClass.name}")
        }
        return WeightTrackingViewModel(repository, profileState) as T
    }
}