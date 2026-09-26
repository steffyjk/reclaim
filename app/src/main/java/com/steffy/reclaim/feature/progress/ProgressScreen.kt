package com.steffy.reclaim.feature.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.steffy.reclaim.R
import com.steffy.reclaim.core.ui.MetricCard
import com.steffy.reclaim.core.ui.SectionHeading
import com.steffy.reclaim.profile.ProfileUiState

@Composable
fun ProgressScreen(
    profileUiState: ProfileUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = stringResource(R.string.progress_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        MetricCard(
            titleRes = R.string.current,
            value = stringResource(R.string.weight_value_kg, profileUiState.profile.currentWeightKg),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(
                titleRes = R.string.goal,
                value = stringResource(R.string.weight_value_kg, profileUiState.profile.goalWeightKg),
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                titleRes = R.string.remaining,
                value = stringResource(R.string.weight_value_kg, profileUiState.metrics.remainingWeightKg),
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            text = stringResource(R.string.progress_percent_format, profileUiState.metrics.progressPercent),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeading(titleRes = R.string.weight_trend)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.trend_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TrendPlaceholder(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp),
                    )
                    Text(
                        text = stringResource(R.string.trend_disclaimer),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendPlaceholder(modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier = modifier) {
        val horizontalLines = 4
        repeat(horizontalLines) { index ->
            val y = size.height * index / (horizontalLines - 1)
            drawLine(
                color = gridColor,
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
            )
        }
        val points = listOf(
            0.06f to 0.25f,
            0.20f to 0.35f,
            0.34f to 0.30f,
            0.49f to 0.48f,
            0.63f to 0.42f,
            0.78f to 0.62f,
            0.94f to 0.72f,
        )
        val path = Path().apply {
            points.forEachIndexed { index, (x, y) ->
                val px = size.width * x
                val py = size.height * y
                if (index == 0) moveTo(px, py) else lineTo(px, py)
            }
        }
        drawPath(path, lineColor, style = Stroke(width = 4.dp.toPx()))
        points.forEach { (x, y) ->
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(size.width * x, size.height * y),
            )
        }
    }
}