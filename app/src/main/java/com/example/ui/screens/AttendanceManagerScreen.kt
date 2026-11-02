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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SubjectEntity
import com.example.domain.GradeTrackMath
import com.example.ui.components.AttendanceDial
import com.example.ui.components.AttendanceStatusChip
import com.example.ui.components.getAttendanceStatus
import com.example.ui.components.getStatusColor
import com.example.ui.theme.AttendanceDangerRed
import com.example.ui.theme.AttendanceSafeGreen
import com.example.ui.theme.AttendanceWarningYellow
import com.example.ui.theme.CyanAccent
import com.example.ui.viewmodel.GradeTrackViewModel

@Composable
fun AttendanceManagerScreen(
    viewModel: GradeTrackViewModel,
    onNavigateToPredictor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val profile by viewModel.studentProfile.collectAsStateWithLifecycle()

    var selectedSemesterFilter by remember { mutableStateOf<Int?>(profile.currentSemester) }
    var subjectToReset by remember { mutableStateOf<SubjectEntity?>(null) }

    val filteredSubjects = if (selectedSemesterFilter != null) {
        allSubjects.filter { it.semester == selectedSemesterFilter }
    } else {
        allSubjects
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Header
        item {
            Column {
                Text(
                    text = "Attendance Manager",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Track conducted and attended sessions with instant one-tap logging",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Overall Attendance Dashboard Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("attendance_overview_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Aggregate Attendance",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "${summary.overallAttendancePercentage.toInt()}%",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 40.sp
                                ),
                                color = getStatusColor(
                                    getAttendanceStatus(
                                        summary.overallAttendancePercentage,
                                        profile.attendanceThreshold,
                                        profile.yellowZoneMargin
                                    )
                                )
                            )

                            Text(
                                text = "${summary.totalClassesAttended} of ${summary.totalClassesConducted} total classes attended",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            AttendanceStatusChip(
                                status = getAttendanceStatus(
                                    summary.overallAttendancePercentage,
                                    profile.attendanceThreshold,
                                    profile.yellowZoneMargin
                                )
                            )
                        }

                        AttendanceDial(
                            percentage = summary.overallAttendancePercentage,
                            threshold = profile.attendanceThreshold,
                            yellowMargin = profile.yellowZoneMargin,
                            size = 100.dp,
                            strokeWidth = 10.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Button to Open Advanced Prediction Calculator
                    Button(
                        onClick = onNavigateToPredictor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_predictor_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Predict Classes (Safe Absences & Targets)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            }
        }

        // Semester Filter Chips
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Filter Semester:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    val isAll = selectedSemesterFilter == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isAll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                            .clickable { selectedSemesterFilter = null }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "All Semesters",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isAll) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                items(8) { idx ->
                    val sem = idx + 1
                    val isSelected = selectedSemesterFilter == sem
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                            .clickable { selectedSemesterFilter = sem }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
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

        // Subject Attendance Cards List
        if (filteredSubjects.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No subjects found for the selected semester.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredSubjects) { subject ->
                SubjectDetailedAttendanceCard(
                    subject = subject,
                    threshold = profile.attendanceThreshold,
                    yellowMargin = profile.yellowZoneMargin,
                    onMarkPresent = { viewModel.markPresent(subject.id) },
                    onMarkAbsent = { viewModel.markAbsent(subject.id) },
                    onUndo = { viewModel.undoAttendance(subject.id) },
                    onReset = { subjectToReset = subject }
                )
            }
        }
    }

    // Reset Confirmation Dialog
    if (subjectToReset != null) {
        val target = subjectToReset!!
        AlertDialog(
            onDismissRequest = { subjectToReset = null },
            title = { Text("Reset Attendance?") },
            text = {
                Text("This will clear all conducted and attended class logs for '${target.name}'. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAttendance(target.id)
                        subjectToReset = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectToReset = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SubjectDetailedAttendanceCard(
    subject: SubjectEntity,
    threshold: Int,
    yellowMargin: Int,
    onMarkPresent: () -> Unit,
    onMarkAbsent: () -> Unit,
    onUndo: () -> Unit,
    onReset: () -> Unit
) {
    val pct = subject.attendancePercentage
    val status = getAttendanceStatus(pct, threshold, yellowMargin)
    val statusColor = getStatusColor(status)

    // Calculate quick prediction metrics
    val prediction = GradeTrackMath.predictAttendance(
        attended = subject.attendedClasses,
        conducted = subject.totalClasses,
        targetPercentage = threshold
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("attendance_card_${subject.code}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            // Top Row: Name, Code & Attendance %
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "${subject.code} • Sem ${subject.semester} • ${subject.credits} Credits",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${pct.toInt()}%",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp
                        ),
                        color = statusColor
                    )
                    AttendanceStatusChip(status = status)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { (pct / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Counters Row: Conducted, Attended, Absent
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(vertical = 10.dp, horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Conducted", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${subject.totalClasses}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Attended", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${subject.attendedClasses}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = AttendanceSafeGreen)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Absent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${subject.classesAbsent}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = AttendanceDangerRed)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Guidance Line
            Text(
                text = prediction.explanation,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = if (prediction.isTargetMet) CyanAccent else AttendanceDangerRed
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Mark Present, Mark Absent, Undo, Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onMarkPresent,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(42.dp)
                        .testTag("btn_present_${subject.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = AttendanceSafeGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Present", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onMarkAbsent,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(42.dp)
                        .testTag("btn_absent_${subject.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = AttendanceDangerRed),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Absent", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onUndo,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("btn_undo_${subject.id}"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Undo", style = MaterialTheme.typography.labelSmall)
                }

                IconButton(
                    onClick = onReset,
                    modifier = Modifier
                        .size(42.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
