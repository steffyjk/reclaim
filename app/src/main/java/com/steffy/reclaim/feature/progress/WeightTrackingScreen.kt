package com.steffy.reclaim.feature.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.steffy.reclaim.R
import com.steffy.reclaim.core.ui.MetricCard
import com.steffy.reclaim.core.ui.SectionHeading
import com.steffy.reclaim.weight.WeightHistoryItem
import com.steffy.reclaim.weight.WeightInputError
import com.steffy.reclaim.weight.WeightTrackingUiState
import java.text.DateFormat
import java.util.Date

@Composable
fun WeightTrackingScreen(
    state: WeightTrackingUiState,
    onBack: () -> Unit,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit,
    onEditEntry: (String) -> Unit,
    onReturnToToday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(state.editingEntryId) {
        if (state.editingEntryId != null) listState.animateScrollToItem(0)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.back_to_progress))
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.weight_tracking_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.weight_tracking_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeading(titleRes = R.string.weight_summary_section)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        titleRes = R.string.current,
                        value = stringResource(R.string.weight_value_kg, state.summary.currentWeightKg),
                        modifier = Modifier.weight(1f),
                    )
                    MetricCard(
                        titleRes = R.string.goal,
                        value = stringResource(R.string.weight_value_kg, state.summary.goalWeightKg),
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        titleRes = R.string.remaining,
                        value = stringResource(R.string.weight_value_kg, state.summary.remainingWeightKg),
                        modifier = Modifier.weight(1f),
                    )
                    MetricCard(
                        titleRes = R.string.weight_change_from_start,
                        value = stringResource(R.string.weight_value_kg, state.summary.totalChangeKg),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeading(
                    titleRes = if (state.editingEntryId == null) {
                        R.string.log_today_weight
                    } else {
                        R.string.correct_weight_entry
                    },
                )
                if (state.editingEntryId == null && state.todayEntryExists) {
                    Text(
                        text = stringResource(R.string.weight_today_update_note),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else if (state.editingRecordedAt != null) {
                    Text(
                        text = stringResource(
                            R.string.weight_editing_date,
                            formatRecordedAt(state.editingRecordedAt),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedTextField(
                    value = state.input,
                    onValueChange = onInputChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.weight_input_label)) },
                    suffix = { Text(stringResource(R.string.kilograms_short)) },
                    singleLine = true,
                    isError = state.validationError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    supportingText = if (state.validationError != null) {
                        { Text(stringResource(state.validationError.messageRes())) }
                    } else {
                        null
                    },
                )
                if (state.persistenceError) {
                    Text(
                        text = stringResource(R.string.weight_save_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (state.saveSucceeded) {
                    Text(
                        text = stringResource(R.string.weight_saved),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading && !state.isSaving,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = stringResource(
                            when {
                                state.isSaving -> R.string.profile_saving
                                state.editingEntryId != null -> R.string.weight_save_correction
                                state.todayEntryExists -> R.string.weight_update_today
                                else -> R.string.weight_save_today
                            },
                        ),
                        modifier = Modifier.padding(vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (state.editingEntryId != null) {
                    TextButton(
                        onClick = onReturnToToday,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.weight_return_to_today))
                    }
                }
            }
        }
        item {
            SectionHeading(titleRes = R.string.weight_history_title)
        }
        if (state.history.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.weight_history_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(state.history, key = { it.entry.id }) { item ->
                WeightHistoryCard(item = item, onEdit = { onEditEntry(item.entry.id) })
            }
        }
    }
}

@Composable
private fun WeightHistoryCard(
    item: WeightHistoryItem,
    onEdit: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = formatRecordedAt(item.entry.recordedAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.weight_value_kg, item.entry.weightKg),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                item.changeFromPreviousKg?.let { change ->
                    Text(
                        text = stringResource(R.string.weight_change_previous, change),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            TextButton(onClick = onEdit) {
                Text(stringResource(R.string.weight_edit_entry))
            }
        }
    }
}

private fun formatRecordedAt(epochMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(epochMillis))

private fun WeightInputError.messageRes(): Int = when (this) {
    WeightInputError.InvalidNumber -> R.string.weight_error_number
    WeightInputError.OutsideSupportedRange -> R.string.weight_error_range
    WeightInputError.TooPrecise -> R.string.weight_error_precision
}
