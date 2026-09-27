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
import androidx.compose.material3.Button
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
import com.steffy.reclaim.weight.WeightChartPoint
import com.steffy.reclaim.weight.WeightTrackingUiState

@Composable
fun ProgressScreen(
    weightUiState: WeightTrackingUiState,
    onOpenWeightTracking: () -> Unit,
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
            value = stringResource(R.string.weight_value_kg, weightUiState.summary.currentWeightKg),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(
                titleRes = R.string.goal,
                value = stringResource(R.string.weight_value_kg, weightUiState.summary.goalWeightKg),
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                titleRes = R.string.remaining,
                value = stringResource(R.string.weight_value_kg, weightUiState.summary.remainingWeightKg),
                modifier = Modifier.weight(1f),
            )
        }
        MetricCard(
            titleRes = R.string.weight_change_from_start,
            value = stringResource(R.string.weight_value_kg, weightUiState.summary.totalChangeKg),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.progress_percent_format, weightUiState.summary.progressPercent),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        Button(
            onClick = onOpenWeightTracking,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.open_weight_tracking))
        }
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
                        text = stringResource(
                            if (weightUiState.chartPoints.size >= 2) {
                                R.string.weight_chart_logged_entries
                            } else {
                                R.string.weight_chart_limited_data
                            },
                            weightUiState.history.size,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (weightUiState.chartPoints.size >= 2) {
                        WeightTrendChart(
                            points = weightUiState.chartPoints,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp),
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.weight_chart_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightTrendChart(
    points: List<WeightChartPoint>,
    modifier: Modifier = Modifier,
) {
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
        val path = Path().apply {
            points.forEachIndexed { index, point ->
                val px = size.width * point.xFraction
                val py = size.height * point.yFraction
                if (index == 0) moveTo(px, py) else lineTo(px, py)
            }
        }
        drawPath(path, lineColor, style = Stroke(width = 4.dp.toPx()))
        points.forEach { point ->
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(
                    size.width * point.xFraction,
                    size.height * point.yFraction,
                ),
            )
        }
    }
}