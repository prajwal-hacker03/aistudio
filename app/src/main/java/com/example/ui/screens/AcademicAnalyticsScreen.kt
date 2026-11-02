package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.GradeTrackMath
import com.example.ui.components.AttendanceComparisonBars
import com.example.ui.components.SemesterGpaChart
import com.example.ui.components.StatCard
import com.example.ui.theme.AttendanceDangerRed
import com.example.ui.theme.AttendanceSafeGreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.PurpleAccent
import com.example.ui.viewmodel.GradeTrackViewModel

@Composable
fun AcademicAnalyticsScreen(
    viewModel: GradeTrackViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val profile by viewModel.studentProfile.collectAsStateWithLifecycle()

    val semesters = summary.effectiveSemesters
    val highestSgpa = semesters.maxOfOrNull { it.sgpa } ?: 0.0
    val lowestSgpa = semesters.minOfOrNull { it.sgpa } ?: 0.0
    val averageSgpa = if (semesters.isNotEmpty()) semesters.map { it.sgpa }.average() else 0.0

    // Grade distribution counts
    val gradeDistribution = allSubjects.groupBy { it.grade.uppercase() }
        .mapValues { it.value.size }

    // Share report generator
    val onShareReport = {
        val report = buildString {
            appendLine("🎓 GRADETRACK ACADEMIC REPORT")
            appendLine("Student: ${profile.name}")
            appendLine("College: ${profile.college} (${profile.university})")
            appendLine("Department: ${profile.department}")
            appendLine("Current Semester: Sem ${profile.currentSemester}")
            appendLine("----------------------------------------")
            appendLine("Overall CGPA: ${String.format("%.2f", summary.overallCgpa)} (${GradeTrackMath.cgpaToPercentage(summary.overallCgpa, profile.cgpaMultiplier)}%)")
            appendLine("Total Credits Earned: ${summary.totalCreditsEarned}")
            appendLine("Overall Attendance: ${summary.overallAttendancePercentage.toInt()}% (${summary.totalClassesAttended}/${summary.totalClassesConducted} classes)")
            appendLine("----------------------------------------")
            appendLine("Semester Breakdown:")
            semesters.forEach { sem ->
                appendLine(" • Semester ${sem.semesterNumber}: SGPA ${String.format("%.2f", sem.sgpa)} (${sem.credits} credits)")
            }
            appendLine("----------------------------------------")
            appendLine("Enrolled Subjects (${allSubjects.size}):")
            allSubjects.forEach { subj ->
                appendLine(" • ${subj.code} ${subj.name}: Grade ${subj.grade} (${subj.credits} cr, ${subj.attendancePercentage.toInt()}% att)")
            }
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, report)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Academic Report")
        context.startActivity(shareIntent)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Share Report Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Academic Analytics",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Visual performance trends, statistical distribution & reports",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onShareReport,
                    modifier = Modifier.testTag("share_report_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
            }
        }

        // Statistical Highlights 3-Card Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Highest SGPA",
                    value = String.format("%.2f", highestSgpa),
                    subtitle = "Peak Semester",
                    icon = Icons.Default.Star,
                    accentColor = CyanAccent,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Average SGPA",
                    value = String.format("%.2f", averageSgpa),
                    subtitle = "Mean Across Sems",
                    icon = Icons.Default.AutoGraph,
                    accentColor = PurpleAccent,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Lowest SGPA",
                    value = String.format("%.2f", lowestSgpa),
                    subtitle = "Floor Score",
                    icon = Icons.Default.Equalizer,
                    accentColor = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Semester GPA Trend Graph
        item {
            SemesterGpaChart(
                semesters = semesters,
                scaleMax = profile.gradingSystem.scaleMax
            )
        }

        // Grade Distribution Breakdown Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("grade_distribution_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Grade Distribution (All Subjects)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (gradeDistribution.isEmpty()) {
                        Text(
                            text = "No subjects added to compute grade distribution.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // Display badges for each grade count
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("O", "S", "A+", "A", "B+", "B", "C", "P", "F").forEach { gradeKey ->
                                val count = gradeDistribution[gradeKey] ?: 0
                                if (count > 0) {
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = gradeKey,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = CyanAccent
                                            )
                                            Text(
                                                text = "$count subj",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Subject Attendance Comparison
        if (allSubjects.isNotEmpty()) {
            item {
                AttendanceComparisonBars(
                    subjects = allSubjects,
                    threshold = profile.attendanceThreshold
                )
            }
        }
    }
}
