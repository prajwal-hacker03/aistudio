package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SemesterEntity
import com.example.domain.GradeTrackMath
import com.example.ui.components.GradeBadge
import com.example.ui.theme.AttendanceDangerRed
import com.example.ui.theme.AttendanceSafeGreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.PurpleAccent
import com.example.ui.viewmodel.GradeTrackViewModel

@Composable
fun CgpaCalculatorScreen(
    viewModel: GradeTrackViewModel,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val semesters by viewModel.allSemesters.collectAsStateWithLifecycle()
    val profile by viewModel.studentProfile.collectAsStateWithLifecycle()

    var showAddSemesterDialog by remember { mutableStateOf(false) }
    var editingSemester by remember { mutableStateOf<SemesterEntity?>(null) }

    val effectiveSemesters = summary.effectiveSemesters
    val isWeighted = profile.isWeightedCgpa
    val overallCgpa = summary.overallCgpa
    val percentageEq = GradeTrackMath.cgpaToPercentage(overallCgpa, profile.cgpaMultiplier)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "CGPA Calculator",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Cumulative grade point average across all university semesters",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Hero Cumulative CGPA Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cgpa_hero_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, PurpleAccent.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Cumulative CGPA",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format("%.2f", overallCgpa),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 42.sp
                                ),
                                color = PurpleAccent
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Percentage",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$percentageEq%",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = CyanAccent
                            )
                            Text(
                                text = "(Multiplier: ${profile.cgpaMultiplier})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Calculation weighting toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isWeighted) "Weighted by Credits (Standard)" else "Equal Weight (Simple Average)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isWeighted) "Formula: Σ(SGPA × Credits) / Σ(Credits)" else "Formula: Σ(SGPA) / Total Semesters",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isWeighted,
                            onCheckedChange = { viewModel.updateProfile(profile.copy(isWeightedCgpa = it)) },
                            modifier = Modifier.testTag("weighted_cgpa_switch")
                        )
                    }
                }
            }
        }

        // Semesters Breakdown Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Semester Progression (${effectiveSemesters.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Button(
                    onClick = { showAddSemesterDialog = true },
                    modifier = Modifier.testTag("add_semester_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Semester")
                }
            }
        }

        // Semester Cards with Delta improvement indicator
        itemsIndexed(effectiveSemesters) { index, sem ->
            val prevSgpa = if (index > 0) effectiveSemesters[index - 1].sgpa else null
            val delta = if (prevSgpa != null) sem.sgpa - prevSgpa else null

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("semester_item_${sem.semesterNumber}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(CyanAccent.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "S${sem.semesterNumber}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = CyanAccent
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SGPA: ${String.format("%.2f", sem.sgpa)}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (delta != null && delta != 0.0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val isImprovement = delta > 0.0
                                    val deltaColor = if (isImprovement) AttendanceSafeGreen else AttendanceDangerRed
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(deltaColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isImprovement) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = deltaColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = String.format("%.2f", kotlin.math.abs(delta)),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = deltaColor
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${sem.credits} Credits • ${if (sem.isManual) "Manual Entry" else "Derived from subjects"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Action buttons
                    Row {
                        IconButton(
                            onClick = {
                                editingSemester = SemesterEntity(
                                    semesterNumber = sem.semesterNumber,
                                    sgpa = sem.sgpa,
                                    credits = sem.credits,
                                    isManualOverride = true,
                                    notes = sem.notes
                                )
                            }
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.deleteSemester(
                                    SemesterEntity(
                                        semesterNumber = sem.semesterNumber,
                                        sgpa = sem.sgpa,
                                        credits = sem.credits
                                    )
                                )
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Semester Dialog
    if (showAddSemesterDialog || editingSemester != null) {
        val isEdit = editingSemester != null
        val target = editingSemester ?: SemesterEntity(
            semesterNumber = (effectiveSemesters.maxOfOrNull { it.semesterNumber } ?: 0) + 1,
            sgpa = 8.5,
            credits = 22,
            isManualOverride = true
        )

        var semNumStr by remember { mutableStateOf(target.semesterNumber.toString()) }
        var sgpaStr by remember { mutableStateOf(target.sgpa.toString()) }
        var creditsStr by remember { mutableStateOf(target.credits.toString()) }
        var notesStr by remember { mutableStateOf(target.notes) }

        AlertDialog(
            onDismissRequest = {
                showAddSemesterDialog = false
                editingSemester = null
            },
            title = { Text(if (isEdit) "Edit Semester ${target.semesterNumber}" else "Add Semester Record") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = semNumStr,
                        onValueChange = { semNumStr = it },
                        label = { Text("Semester Number (1-8)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = sgpaStr,
                        onValueChange = { sgpaStr = it },
                        label = { Text("Semester SGPA (e.g. 8.75)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = creditsStr,
                        onValueChange = { creditsStr = it },
                        label = { Text("Total Semester Credits (e.g. 24)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notesStr,
                        onValueChange = { notesStr = it },
                        label = { Text("Notes (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = semNumStr.toIntOrNull() ?: 1
                        val sgpa = sgpaStr.toDoubleOrNull() ?: 0.0
                        val credits = creditsStr.toIntOrNull() ?: 20
                        viewModel.addOrUpdateSemester(
                            SemesterEntity(
                                semesterNumber = num,
                                sgpa = sgpa,
                                credits = credits,
                                isManualOverride = true,
                                notes = notesStr
                            )
                        )
                        showAddSemesterDialog = false
                        editingSemester = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddSemesterDialog = false
                        editingSemester = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
