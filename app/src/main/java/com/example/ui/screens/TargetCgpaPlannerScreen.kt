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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.domain.GradeTrackMath
import com.example.ui.theme.AttendanceDangerRed
import com.example.ui.theme.AttendanceSafeGreen
import com.example.ui.theme.AttendanceWarningYellow
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.PurpleAccent
import com.example.ui.viewmodel.GradeTrackViewModel

@Composable
fun TargetCgpaPlannerScreen(
    viewModel: GradeTrackViewModel,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val profile by viewModel.studentProfile.collectAsStateWithLifecycle()

    var currentCgpaStr by remember { mutableStateOf(String.format("%.2f", summary.overallCgpa)) }
    var completedCreditsStr by remember { mutableStateOf(summary.totalCreditsEarned.toString()) }
    var remainingCreditsStr by remember { mutableStateOf("48") } // Typical 2 semesters (~48 credits)

    val scaleMax = profile.gradingSystem.scaleMax
    var targetSliderValue by remember { mutableFloatStateOf(profile.targetCgpa.toFloat().coerceIn(0f, scaleMax.toFloat())) }

    val currentCgpa = currentCgpaStr.toDoubleOrNull() ?: summary.overallCgpa
    val completedCredits = completedCreditsStr.toIntOrNull() ?: summary.totalCreditsEarned
    val remainingCredits = remainingCreditsStr.toIntOrNull() ?: 48
    val targetCgpa = targetSliderValue.toDouble()

    val plannerResult = GradeTrackMath.calculateTargetGpa(
        currentCgpa = currentCgpa,
        completedCredits = completedCredits,
        remainingCredits = remainingCredits,
        targetCgpa = targetCgpa,
        scaleMax = scaleMax
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Column {
                Text(
                    text = "Target CGPA Planner",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Forecast required grade points per remaining semester to reach your dream graduation GPA",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Inputs Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("target_inputs_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Your Academic Coordinates",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = currentCgpaStr,
                            onValueChange = { currentCgpaStr = it },
                            label = { Text("Current CGPA") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = completedCreditsStr,
                            onValueChange = { completedCreditsStr = it },
                            label = { Text("Completed Cr") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = remainingCreditsStr,
                            onValueChange = { remainingCreditsStr = it },
                            label = { Text("Remaining Cr") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Target CGPA Interactive Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target Graduation CGPA:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = String.format("%.2f", targetSliderValue),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = CyanAccent
                        )
                    }

                    Slider(
                        value = targetSliderValue,
                        onValueChange = { targetSliderValue = it },
                        valueRange = 4.0f..scaleMax.toFloat(),
                        steps = ((scaleMax - 4.0) * 10).toInt(),
                        modifier = Modifier.testTag("target_cgpa_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("4.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Scale Max: $scaleMax", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Planning Result Output Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("target_result_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.5.dp,
                            when (plannerResult) {
                                is GradeTrackMath.TargetCgpaResult.Achievable -> CyanAccent.copy(alpha = 0.5f)
                                is GradeTrackMath.TargetCgpaResult.AlreadyGuaranteed -> AttendanceSafeGreen.copy(alpha = 0.5f)
                                else -> AttendanceDangerRed.copy(alpha = 0.5f)
                            },
                            RoundedCornerShape(24.dp)
                        )
                        .padding(20.dp)
                ) {
                    when (plannerResult) {
                        is GradeTrackMath.TargetCgpaResult.Achievable -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Required Average GPA in Remaining Credits",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = String.format("%.2f", plannerResult.requiredGpa),
                                        style = MaterialTheme.typography.displayLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 42.sp
                                        ),
                                        color = CyanAccent
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .background(CyanAccent.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Target ${String.format("%.2f", plannerResult.targetCgpa)}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = CyanAccent
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Effort badge
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PurpleAccent.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.TrackChanges,
                                        contentDescription = null,
                                        tint = PurpleAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Effort Assessment:",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = PurpleAccent
                                        )
                                        Text(
                                            text = plannerResult.effortDescription,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        is GradeTrackMath.TargetCgpaResult.AlreadyGuaranteed -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AttendanceSafeGreen, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Target Already Guaranteed!",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = AttendanceSafeGreen
                                    )
                                    Text(
                                        text = "Your current credits already secure a CGPA of at least ${String.format("%.2f", plannerResult.targetCgpa)}.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        is GradeTrackMath.TargetCgpaResult.NotAchievable -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = AttendanceDangerRed, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Target Mathematically Not Achievable",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = AttendanceDangerRed
                                    )
                                    Text(
                                        text = "Even with perfect ${plannerResult.scaleMax} in all remaining ${plannerResult.remainingCredits} credits, your maximum achievable graduation CGPA is ${String.format("%.2f", plannerResult.maxPossibleCgpa)}.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        is GradeTrackMath.TargetCgpaResult.InvalidCredits -> {
                            Text(
                                text = plannerResult.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AttendanceDangerRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Formula explanation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Formula: Required GPA = (Target CGPA × Final Credits - Current CGPA × Completed Credits) / Remaining Credits\nTotal Final Credits = $completedCredits + $remainingCredits = ${completedCredits + remainingCredits}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
