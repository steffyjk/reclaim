package com.steffy.reclaim.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = ReclaimGreen,
    onPrimary = ReclaimLightSurface,
    secondary = ReclaimSage,
    onSecondary = ReclaimLightSurface,
    tertiary = ReclaimLightCompletion,
    onTertiary = ReclaimLightOnCompletion,
    secondaryContainer = ReclaimLightPlanSurface,
    onSecondaryContainer = ReclaimLightPlanContent,
    background = ReclaimLightBackground,
    surface = ReclaimLightSurface,
    surfaceVariant = ReclaimLightSurfaceVariant,
    onSurfaceVariant = ReclaimLightOnSurfaceVariant,
    outlineVariant = ReclaimLightOutline,
)

private val DarkColors = darkColorScheme(
    primary = ReclaimLightGreen,
    onPrimary = ReclaimDarkBackground,
    secondary = ReclaimLightSage,
    onSecondary = ReclaimDarkBackground,
    tertiary = ReclaimDarkCompletion,
    onTertiary = ReclaimDarkOnCompletion,
    secondaryContainer = ReclaimDarkPlanSurface,
    onSecondaryContainer = ReclaimDarkPlanContent,
    background = ReclaimDarkBackground,
    surface = ReclaimDarkSurface,
    surfaceVariant = ReclaimDarkSurfaceVariant,
    onSurfaceVariant = ReclaimDarkOnSurfaceVariant,
    outlineVariant = ReclaimDarkOutline,
)

@Composable
fun ReclaimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}