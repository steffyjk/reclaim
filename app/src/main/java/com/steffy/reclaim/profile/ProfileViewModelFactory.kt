package com.steffy.reclaim.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.steffy.reclaim.data.profile.ProfileRepository

class ProfileViewModelFactory(
    private val repository: ProfileRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            throw IllegalArgumentException("Unsupported ViewModel type: ${modelClass.name}")
        }
        return ProfileViewModel(repository) as T
    }
}