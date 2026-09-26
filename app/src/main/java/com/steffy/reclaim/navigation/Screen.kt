package com.steffy.reclaim.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.steffy.reclaim.R

enum class Screen(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Home(R.string.nav_home, Icons.Filled.Home),
    Progress(R.string.nav_progress, Icons.Filled.Favorite),
    Habits(R.string.nav_habits, Icons.Filled.CheckCircle),
    Settings(R.string.nav_settings, Icons.Filled.Settings),
}