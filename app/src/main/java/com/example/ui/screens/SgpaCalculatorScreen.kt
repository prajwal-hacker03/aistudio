package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GradeScales
import com.example.domain.GradeTrackMath
import com.example.ui.components.GradeBadge
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.PurpleAccent
import com.example.ui.viewmodel.GradeTrackViewModel
import com.example.ui.viewmodel.SgpaDraftSubject

@Composable
fun SgpaCalculatorScreen(
    viewModel: GradeTrackViewModel,
    modifier: Modifier = Modifier
) {
    val draftSemester by viewModel.sgpaDraftSemester.collectAsStateWithLifecycle()
    val draftSubjects by viewModel.sgpaDraftSubjects.collectAsStateWithLifecycle()
    val profile by viewModel.studentProfile.collectAsStateWithLifecycle()

    val gradeScale = GradeScales.getScale(profile.gradingSystem)

    // Calculate real-time SGPA and stats
    val creditPoints = draftSubjects.map { it.credits to it.gradePoints }
    val calculatedSgpa = GradeTrackMath.calculateSGPA(creditPoints)
    val totalCredits = draftSubjects.sumOf { it.credits }
    val totalWeightedPoints = draftSubjects.sumOf { it.credits * it.gradePoints }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Semester Selector Header
        item {
            Column {
                Text(
                    text = "SGPA Calculator",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Calculate semester GPA with configurable university grade points",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Semester:",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(8) { idx ->
                        val sem = idx + 1
                        val isSelected = sem == draftSemester
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setSgpaDraftSemester(sem) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("select_semester_$sem")
                        ) {
                            Text(
                                text = "Sem $sem",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 2. Computed Result Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sgpa_result_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Calculated SGPA",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format("%.2f", calculatedSgpa),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 42.sp
                                ),
                                color = CyanAccent
                            )
                        }

                        // Formula Badge
                        Box(
                            modifier = Modifier
                                .background(PurpleAccent.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .border(1.dp, PurpleAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Total Credits: $totalCredits",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PurpleAccent
                                )
                                Text(
                                    text = "Grade Points: ${String.format("%.1f", totalWeightedPoints)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Calculation formula step breakdown
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Formula: Σ(Credits × Points) / Total Credits = ${String.format("%.1f", totalWeightedPoints)} / $totalCredits = ${String.format("%.2f", calculatedSgpa)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.saveDraftAsSemesterRecord() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_as_semester_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Record")
                        }

                        OutlinedButton(
                            onClick = { viewModel.saveDraftSubjectsToDatabase() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_subjects_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.FolderSpecial, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save to Subjects")
                        }
                    }
                }
            }
        }

        // 3. Subject List Header with "Add Subject"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Subjects (${draftSubjects.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Button(
                    onClick = { viewModel.addSgpaDraftSubject() },
                    modifier = Modifier.testTag("add_draft_subject_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Subject")
                }
            }
        }

        // 4. Subject Rows
        itemsIndexed(draftSubjects) { index, item ->
            SgpaSubjectEditorCard(
                subject = item,
                index = index,
                gradeScale = gradeScale,
                onUpdate = { updated -> viewModel.updateSgpaDraftSubject(index, updated) },
                onDelete = { viewModel.removeSgpaDraftSubject(index) }
            )
        }
    }
}

@Composable
fun SgpaSubjectEditorCard(
    subject: SgpaDraftSubject,
    index: Int,
    gradeScale: List<com.example.data.model.GradeScaleItem>,
    onUpdate: (SgpaDraftSubject) -> Unit,
    onDelete: () -> Unit
) {
    var expandedGradeMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("draft_subject_$index"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${index + 1} ${subject.name.ifBlank { "Subject" }}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = subject.name,
                    onValueChange = { onUpdate(subject.copy(name = it)) },
                    label = { Text("Subject Name") },
                    modifier = Modifier.weight(2f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = subject.code,
                    onValueChange = { onUpdate(subject.copy(code = it)) },
                    label = { Text("Code") },
                    modifier = Modifier.weight(1.2f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Credits picker
                OutlinedTextField(
                    value = if (subject.credits == 0) "" else subject.credits.toString(),
                    onValueChange = { text ->
                        val cr = text.toIntOrNull() ?: 0
                        onUpdate(subject.copy(credits = cr))
                    },
                    label = { Text("Credits") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                // Grade Selector Dropdown
                Box(modifier = Modifier.weight(1.2f)) {
                    OutlinedButton(
                        onClick = { expandedGradeMenu = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Grade: ${subject.grade} (${subject.gradePoints})",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = expandedGradeMenu,
                        onDismissRequest = { expandedGradeMenu = false }
                    ) {
                        gradeScale.forEach { scaleItem ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "${scaleItem.grade} (${scaleItem.points} pts)",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(scaleItem.description, style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = {
                                    onUpdate(
                                        subject.copy(
                                            grade = scaleItem.grade,
                                            gradePoints = scaleItem.points
                                        )
                                    )
                                    expandedGradeMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
