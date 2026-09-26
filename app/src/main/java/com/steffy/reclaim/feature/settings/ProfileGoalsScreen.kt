package com.steffy.reclaim.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.steffy.reclaim.R
import com.steffy.reclaim.profile.PlanType
import com.steffy.reclaim.profile.ProfileDraft
import com.steffy.reclaim.profile.ProfileField
import com.steffy.reclaim.profile.ValidationIssue
import com.steffy.reclaim.core.ui.SectionHeading

@Composable
fun ProfileGoalsScreen(
    draft: ProfileDraft,
    validationIssues: Map<ProfileField, ValidationIssue>,
    onDraftChange: (ProfileDraft) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.profile_goals_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.profile_goals_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeading(titleRes = R.string.personal_details)
            ProfileTextField(
                label = stringResource(R.string.profile_name),
                value = draft.name,
                issue = validationIssues[ProfileField.Name],
                onValueChange = { onDraftChange(draft.copy(name = it)) },
            )
            ProfileTextField(
                label = stringResource(R.string.profile_age),
                value = draft.age,
                issue = validationIssues[ProfileField.Age],
                keyboardType = KeyboardType.Number,
                onValueChange = { onDraftChange(draft.copy(age = it)) },
            )
            ProfileTextField(
                label = stringResource(R.string.profile_height),
                value = draft.heightCm,
                issue = validationIssues[ProfileField.HeightCm],
                keyboardType = KeyboardType.Number,
                onValueChange = { onDraftChange(draft.copy(heightCm = it)) },
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeading(titleRes = R.string.weight_goals)
            ProfileTextField(
                label = stringResource(R.string.starting_weight),
                value = draft.startingWeightKg,
                issue = validationIssues[ProfileField.StartingWeightKg],
                keyboardType = KeyboardType.Decimal,
                onValueChange = { onDraftChange(draft.copy(startingWeightKg = it)) },
            )
            ProfileTextField(
                label = stringResource(R.string.current_weight),
                value = draft.currentWeightKg,
                issue = validationIssues[ProfileField.CurrentWeightKg],
                keyboardType = KeyboardType.Decimal,
                onValueChange = { onDraftChange(draft.copy(currentWeightKg = it)) },
            )
            ProfileTextField(
                label = stringResource(R.string.goal_weight),
                value = draft.goalWeightKg,
                issue = validationIssues[ProfileField.GoalWeightKg],
                keyboardType = KeyboardType.Decimal,
                onValueChange = { onDraftChange(draft.copy(goalWeightKg = it)) },
            )
            ProfileTextField(
                label = stringResource(R.string.goal_date_format),
                value = draft.goalDate,
                issue = validationIssues[ProfileField.GoalDate],
                supportingText = stringResource(R.string.goal_date_hint),
                onValueChange = { onDraftChange(draft.copy(goalDate = it)) },
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeading(titleRes = R.string.plan_selection)
            PlanChoice(
                plan = PlanType.NORMAL,
                selectedPlan = draft.plan,
                title = stringResource(R.string.normal_plan),
                description = stringResource(R.string.normal_plan_description),
                onSelect = { onDraftChange(draft.copy(plan = it)) },
            )
            PlanChoice(
                plan = PlanType.ACCELERATED,
                selectedPlan = draft.plan,
                title = stringResource(R.string.accelerated_plan),
                description = stringResource(R.string.accelerated_plan_description),
                onSelect = { onDraftChange(draft.copy(plan = it)) },
            )
        }

        Text(
            text = stringResource(R.string.profile_memory_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(
                text = stringResource(R.string.save_changes),
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(
                text = stringResource(R.string.cancel),
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    issue: ValidationIssue?,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    supportingText: String? = null,
) {
    val errorText = issue?.let { stringResource(it.messageRes()) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        isError = issue != null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        supportingText = when {
            errorText != null -> ({ Text(errorText) })
            supportingText != null -> ({ Text(supportingText) })
            else -> null
        },
    )
}

@Composable
private fun PlanChoice(
    plan: PlanType,
    selectedPlan: PlanType,
    title: String,
    description: String,
    onSelect: (PlanType) -> Unit,
) {
    val selected = plan == selectedPlan
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.RadioButton) { onSelect(plan) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 1.dp else 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            RadioButton(
                selected = selected,
                onClick = { onSelect(plan) },
            )
        }
    }
}

private fun ValidationIssue.messageRes(): Int = when (this) {
    ValidationIssue.Required -> R.string.validation_required
    ValidationIssue.AgeRange -> R.string.validation_age
    ValidationIssue.HeightRange -> R.string.validation_height
    ValidationIssue.WeightRange -> R.string.validation_weight
    ValidationIssue.GoalBelowStartingWeight -> R.string.validation_goal_below_start
    ValidationIssue.InvalidDate -> R.string.validation_goal_date
}