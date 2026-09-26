package com.steffy.reclaim.feature.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.steffy.reclaim.R
import com.steffy.reclaim.core.ui.MetricCard
import com.steffy.reclaim.core.ui.PlanBadge
import com.steffy.reclaim.core.ui.SectionHeading
import com.steffy.reclaim.core.ui.WaterDropIcon
import com.steffy.reclaim.profile.ProfileUiState

@Composable
fun HomeScreen(
    profileUiState: ProfileUiState,
    modifier: Modifier = Modifier,
) {
    var isDone by remember { mutableStateOf(false) }
    val actionColor by animateColorAsState(
        targetValue = if (isDone) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 220),
        label = "action_color",
    )
    val actionContentColor = if (isDone) {
        MaterialTheme.colorScheme.onTertiary
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        PlanBadge(plan = profileUiState.profile.plan, modifier = Modifier.fillMaxWidth())

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = actionColor),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                SectionHeading(titleRes = R.string.next_action, color = actionContentColor)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WaterDropIcon(
                        tint = actionContentColor,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(22.dp),
                    )
                    Text(
                        text = stringResource(R.string.drink_water),
                        style = MaterialTheme.typography.headlineSmall,
                        color = actionContentColor,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = stringResource(R.string.next_action_support),
                    style = MaterialTheme.typography.bodyMedium,
                    color = actionContentColor.copy(alpha = 0.82f),
                )
                Button(
                    onClick = { isDone = !isDone },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(
                        text = stringResource(if (isDone) R.string.done_feedback else R.string.done),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        WeightSummary(profileUiState)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeading(titleRes = R.string.today)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(R.string.water, stringResource(R.string.water_value), Modifier.weight(1f))
                MetricCard(R.string.steps, stringResource(R.string.steps_value), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(R.string.movement, stringResource(R.string.movement_value), Modifier.weight(1f))
                MetricCard(R.string.meals, stringResource(R.string.meals_value), Modifier.weight(1f))
            }
            MetricCard(R.string.sleep, stringResource(R.string.no_value), Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun WeightSummary(profileUiState: ProfileUiState) {
    val profile = profileUiState.profile
    val metrics = profileUiState.metrics
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                WeightValue(
                    R.string.current_weight,
                    stringResource(R.string.weight_value_kg, profile.currentWeightKg),
                )
                Text(
                    text = stringResource(R.string.down_arrow),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                WeightValue(
                    R.string.goal_weight,
                    stringResource(R.string.weight_value_kg, profile.goalWeightKg),
                )
            }
            Text(
                text = stringResource(R.string.remaining_weight_format, metrics.remainingWeightKg),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
            LinearProgressIndicator(
                progress = { metrics.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.journey_day_format, metrics.journeyDay),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(
                        if (metrics.daysRemaining >= 0L) R.string.days_remaining_format else R.string.goal_date_passed,
                        metrics.daysRemaining.coerceAtLeast(0L),
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WeightValue(labelRes: Int, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}