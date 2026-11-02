package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubjectEntity
import com.example.data.repository.EffectiveSemester
import com.example.ui.theme.AttendanceDangerRed
import com.example.ui.theme.AttendanceSafeGreen
import com.example.ui.theme.AttendanceWarningYellow
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.PurpleAccent

@Composable
fun SemesterGpaChart(
    semesters: List<EffectiveSemester>,
    scaleMax: Double = 10.0,
    modifier: Modifier = Modifier
) {
    if (semesters.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No semester data yet. Add a semester or subjects to view your GPA chart.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val primaryColor = CyanAccent
    val secondaryColor = PurpleAccent
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Semester SGPA Trend",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Scale 0.0 - $scaleMax",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Legend
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(primaryColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "SGPA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected point detail pill
        if (selectedIndex != null && selectedIndex!! in semesters.indices) {
            val item = semesters[selectedIndex!!]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Semester ${item.semesterNumber}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "SGPA: ${String.format("%.2f", item.sgpa)} • ${item.credits} Credits",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val width = size.width
            val height = size.height
            val padBottom = 28.dp.toPx()
            val padTop = 16.dp.toPx()
            val padLeft = 16.dp.toPx()
            val padRight = 16.dp.toPx()

            val chartWidth = width - padLeft - padRight
            val chartHeight = height - padTop - padBottom

            // Draw horizontal grid lines (e.g. at 25%, 50%, 75%, 100% of scale)
            val gridSteps = 4
            for (i in 0..gridSteps) {
                val y = padTop + (chartHeight * i / gridSteps)
                drawLine(
                    color = gridColor,
                    start = Offset(padLeft, y),
                    end = Offset(width - padRight, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            if (semesters.size == 1) {
                // Single point display
                val item = semesters[0]
                val cx = width / 2f
                val cy = padTop + chartHeight * (1f - (item.sgpa.toFloat() / scaleMax.toFloat()).coerceIn(0f, 1f))

                drawCircle(
                    color = primaryColor,
                    radius = 8.dp.toPx(),
                    center = Offset(cx, cy)
                )
                return@Canvas
            }

            val stepX = chartWidth / (semesters.size - 1)
            val points = semesters.mapIndexed { idx, sem ->
                val x = padLeft + idx * stepX
                val normalizedY = (sem.sgpa.toFloat() / scaleMax.toFloat()).coerceIn(0f, 1f)
                val y = padTop + chartHeight * (1f - normalizedY)
                Offset(x, y)
            }

            // Path for gradient area under line
            val fillPath = Path().apply {
                moveTo(points.first().x, height - padBottom)
                lineTo(points.first().x, points.first().y)

                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlX = (p0.x + p1.x) / 2f
                    cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                }

                lineTo(points.last().x, height - padBottom)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent),
                    startY = padTop,
                    endY = height - padBottom
                )
            )

            // Curved line stroke
            val strokePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlX = (p0.x + p1.x) / 2f
                    cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                }
            }

            drawPath(
                path = strokePath,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Point Nodes & Semester Labels
            points.forEachIndexed { idx, point ->
                // Outer glow circle
                drawCircle(
                    color = primaryColor.copy(alpha = 0.25f),
                    radius = 9.dp.toPx(),
                    center = point
                )
                // Center node
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = primaryColor,
                    radius = 3.dp.toPx(),
                    center = point
                )
            }
        }

        // X-axis Semester chips below canvas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            semesters.forEachIndexed { idx, sem ->
                Text(
                    text = "S${sem.semesterNumber}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (selectedIndex == idx) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable {
                        selectedIndex = if (selectedIndex == idx) null else idx
                    }
                )
            }
        }
    }
}

@Composable
fun AttendanceComparisonBars(
    subjects: List<SubjectEntity>,
    threshold: Int = 75,
    modifier: Modifier = Modifier
) {
    if (subjects.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Subject Attendance Comparison",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Goal: $threshold%",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = CyanAccent
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        subjects.take(6).forEach { subj ->
            val pct = subj.attendancePercentage
            val barColor = when {
                pct >= threshold + 5 -> AttendanceSafeGreen
                pct >= threshold -> AttendanceWarningYellow
                else -> AttendanceDangerRed
            }

            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = subj.name,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${pct.toInt()}% (${subj.attendedClasses}/${subj.totalClasses})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = barColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((pct / 100.0).toFloat().coerceIn(0f, 1f))
                            .height(8.dp)
                            .background(barColor, RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}
