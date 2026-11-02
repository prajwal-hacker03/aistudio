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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GradeScales
import com.example.data.model.SubjectEntity
import com.example.domain.GradeTrackMath
import com.example.ui.components.AttendanceComparisonBars
import com.example.ui.components.AttendanceDial
import com.example.ui.components.AttendanceStatusChip
import com.example.ui.components.GradeBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.SemesterGpaChart
import com.example.ui.components.StatCard
import com.example.ui.components.getAttendanceStatus
import com.example.ui.theme.AttendanceDangerRed
import com.example.ui.theme.AttendanceSafeGreen
import com.example.ui.theme.AttendanceWarningYellow
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.PurpleAccent
import com.example.ui.viewmodel.GradeTrackViewModel

@Composable
fun HomeScreen(
    viewModel: GradeTrackViewModel,
    onNavigateToSgpa: () -> Unit,
    onNavigateToCgpa: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToSubjects: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onOpenProfileSettings: () -> Unit,
    onAddSubjectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val subjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val profile by viewModel.studentProfile.collectAsStateWithLifecycle()

    val currentSemSubjects = subjects.filter { it.semester == profile.currentSemester }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Student Profile Header
        item {
            StudentHeaderCard(
                name = profile.name,
                college = profile.college,
                university = profile.university,
                department = profile.department,
                semester = profile.currentSemester,
                onEditClick = onOpenProfileSettings
            )
        }

        // 2. Attendance Warning Alert (if any subject below threshold)
        if (summary.subjectsBelowThresholdCount > 0) {
            item {
                AttendanceWarningBanner(
                    count = summary.subjectsBelowThresholdCount,
                    threshold = profile.attendanceThreshold,
                    onViewAttendanceClick = onNavigateToAttendance
                )
            }
        }

        // 3. Hero Academic Overview Cards (CGPA & Attendance Dials)
        item {
            AcademicHeroOverview(
                overallCgpa = summary.overallCgpa,
                latestSgpa = summary.latestSgpa,
                targetCgpa = profile.targetCgpa,
                cgpaMultiplier = profile.cgpaMultiplier,
                overallAttendance = summary.overallAttendancePercentage,
                threshold = profile.attendanceThreshold,
                yellowMargin = profile.yellowZoneMargin,
                scaleMax = profile.gradingSystem.scaleMax
            )
        }

        // 4. Quick Action Buttons
        item {
            SectionHeader(title = "Quick Actions")
            Spacer(modifier = Modifier.height(6.dp))
            QuickActionsRow(
                onCalculateSgpa = onNavigateToSgpa,
                onCalculateCgpa = onNavigateToCgpa,
                onCheckAttendance = onNavigateToAttendance,
                onAddSubject = onAddSubjectClick,
                onViewReport = onNavigateToAnalytics
            )
        }

        // 5. Academic Performance Graph
        item {
            SemesterGpaChart(
                semesters = summary.effectiveSemesters,
                scaleMax = profile.gradingSystem.scaleMax
            )
        }

        // 6. Quick Attendance Logger (Current Semester Subjects)
        item {
            SectionHeader(
                title = "Today's Attendance Tracker",
                actionText = "Manage All",
                onActionClick = onNavigateToAttendance
            )
        }

        if (currentSemSubjects.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No subjects enrolled for Semester ${profile.currentSemester}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onAddSubjectClick,
                            modifier = Modifier.testTag("add_subject_empty_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Enrolled Subject")
                        }
                    }
                }
            }
        } else {
            items(currentSemSubjects) { subject ->
                HomeSubjectAttendanceRow(
                    subject = subject,
                    threshold = profile.attendanceThreshold,
                    yellowMargin = profile.yellowZoneMargin,
                    onMarkPresent = { viewModel.markPresent(subject.id) },
                    onMarkAbsent = { viewModel.markAbsent(subject.id) },
                    onUndo = { viewModel.undoAttendance(subject.id) }
                )
            }
        }

        // 7. Subject Attendance Comparison Bars
        if (subjects.isNotEmpty()) {
            item {
                AttendanceComparisonBars(
                    subjects = subjects,
                    threshold = profile.attendanceThreshold
                )
            }
        }
    }
}

@Composable
fun StudentHeaderCard(
    name: String,
    college: String,
    university: String,
    department: String,
    semester: Int,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_header_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                )
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Student Avatar Badge with Initials
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            Brush.linearGradient(listOf(CyanAccent, PurpleAccent)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = name.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                        .ifBlank { "ST" }
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(CyanAccent.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Sem $semester",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CyanAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "$department • $college",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = university,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.testTag("edit_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceWarningBanner(
    count: Int,
    threshold: Int,
    onViewAttendanceClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AttendanceDangerRed.copy(alpha = 0.12f))
            .border(1.dp, AttendanceDangerRed.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .clickable(onClick = onViewAttendanceClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(AttendanceDangerRed.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Attendance Warning",
                    tint = AttendanceDangerRed,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "$count Subject${if (count > 1) "s" else ""} Below ${threshold}%",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AttendanceDangerRed
                )
                Text(
                    text = "Immediate attendance needed to avoid exam debarment.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "View",
            tint = AttendanceDangerRed,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun AcademicHeroOverview(
    overallCgpa: Double,
    latestSgpa: Double,
    targetCgpa: Double,
    cgpaMultiplier: Double,
    overallAttendance: Double,
    threshold: Int,
    yellowMargin: Int,
    scaleMax: Double
) {
    val percentageEq = GradeTrackMath.cgpaToPercentage(overallCgpa, cgpaMultiplier)
    val gradeScaleItem = GradeScales.getGradeForPercentage(percentageEq)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // CGPA Main Card
        Card(
            modifier = Modifier
                .weight(1.2f)
                .testTag("hero_cgpa_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Overall CGPA",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    GradeBadge(grade = gradeScaleItem.grade)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = String.format("%.2f", overallCgpa),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 36.sp
                    ),
                    color = CyanAccent
                )

                Text(
                    text = "≈ ${percentageEq}% • Target: ${String.format("%.2f", targetCgpa)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Latest SGPA",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%.2f", latestSgpa),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Attendance Dial Card
        Card(
            modifier = Modifier
                .weight(1f)
                .testTag("hero_attendance_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Attendance",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                AttendanceDial(
                    percentage = overallAttendance,
                    threshold = threshold,
                    yellowMargin = yellowMargin,
                    size = 90.dp,
                    strokeWidth = 9.dp
                )

                Spacer(modifier = Modifier.height(8.dp))

                val status = getAttendanceStatus(overallAttendance, threshold, yellowMargin)
                AttendanceStatusChip(status = status)
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    onCalculateSgpa: () -> Unit,
    onCalculateCgpa: () -> Unit,
    onCheckAttendance: () -> Unit,
    onAddSubject: () -> Unit,
    onViewReport: () -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        item {
            QuickActionButton(
                label = "Calculate SGPA",
                icon = Icons.Default.Calculate,
                color = CyanAccent,
                onClick = onCalculateSgpa
            )
        }
        item {
            QuickActionButton(
                label = "Calculate CGPA",
                icon = Icons.Default.AutoGraph,
                color = PurpleAccent,
                onClick = onCalculateCgpa
            )
        }
        item {
            QuickActionButton(
                label = "Check Attendance",
                icon = Icons.Default.EventAvailable,
                color = AttendanceSafeGreen,
                onClick = onCheckAttendance
            )
        }
        item {
            QuickActionButton(
                label = "Add Subject",
                icon = Icons.Default.Add,
                color = Color(0xFF38BDF8),
                onClick = onAddSubject
            )
        }
        item {
            QuickActionButton(
                label = "Academic Report",
                icon = Icons.Default.Analytics,
                color = Color(0xFFFBBF24),
                onClick = onViewReport
            )
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("quick_action_${label.lowercase().replace(" ", "_")}"),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun HomeSubjectAttendanceRow(
    subject: SubjectEntity,
    threshold: Int,
    yellowMargin: Int,
    onMarkPresent: () -> Unit,
    onMarkAbsent: () -> Unit,
    onUndo: () -> Unit
) {
    val pct = subject.attendancePercentage
    val status = getAttendanceStatus(pct, threshold, yellowMargin)
    val statusColor = when (status) {
        com.example.ui.components.AttendanceStatusLevel.SAFE -> AttendanceSafeGreen
        com.example.ui.components.AttendanceStatusLevel.WARNING -> AttendanceWarningYellow
        com.example.ui.components.AttendanceStatusLevel.CRITICAL -> AttendanceDangerRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("attendance_row_${subject.code}"),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "${subject.code} • ${subject.credits} Credits",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${pct.toInt()}%",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                    Text(
                        text = "${subject.attendedClasses}/${subject.totalClasses} classes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Present, Absent, Undo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onMarkPresent,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("present_${subject.code}"),
                    colors = ButtonDefaults.buttonColors(containerColor = AttendanceSafeGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Present", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Present", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = onMarkAbsent,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("absent_${subject.code}"),
                    colors = ButtonDefaults.buttonColors(containerColor = AttendanceDangerRed),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Absent", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Absent", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }

                OutlinedButton(
                    onClick = onUndo,
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("undo_${subject.code}"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("Undo", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
