package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AttendanceDangerRed
import com.example.ui.theme.AttendanceSafeGreen
import com.example.ui.theme.AttendanceWarningYellow

enum class AttendanceStatusLevel(val label: String) {
    SAFE("Safe"),
    WARNING("Warning"),
    CRITICAL("Shortage")
}

fun getAttendanceStatus(percentage: Double, threshold: Int, yellowMargin: Int = 5): AttendanceStatusLevel {
    return when {
        percentage >= (threshold + yellowMargin) -> AttendanceStatusLevel.SAFE
        percentage >= threshold -> AttendanceStatusLevel.WARNING
        else -> AttendanceStatusLevel.CRITICAL
    }
}

fun getStatusColor(status: AttendanceStatusLevel): Color {
    return when (status) {
        AttendanceStatusLevel.SAFE -> AttendanceSafeGreen
        AttendanceStatusLevel.WARNING -> AttendanceWarningYellow
        AttendanceStatusLevel.CRITICAL -> AttendanceDangerRed
    }
}

@Composable
fun AttendanceDial(
    percentage: Double,
    threshold: Int = 75,
    yellowMargin: Int = 5,
    size: Dp = 140.dp,
    strokeWidth: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    val status = getAttendanceStatus(percentage, threshold, yellowMargin)
    val statusColor = getStatusColor(status)

    val sweepAngle by animateFloatAsState(
        targetValue = ((percentage.coerceIn(0.0, 100.0) / 100.0) * 360f).toFloat(),
        animationSpec = tween(durationMillis = 900),
        label = "attendanceSweep"
    )

    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = size.toPx() - strokePx

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Active Arc
            if (sweepAngle > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to statusColor.copy(alpha = 0.8f),
                        0.5f to statusColor,
                        1.0f to statusColor
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${percentage.toInt()}%",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Threshold $threshold%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AttendanceStatusChip(
    status: AttendanceStatusLevel,
    modifier: Modifier = Modifier
) {
    val color = getStatusColor(status)
    val icon = when (status) {
        AttendanceStatusLevel.SAFE -> Icons.Default.CheckCircle
        AttendanceStatusLevel.WARNING -> Icons.Default.Warning
        AttendanceStatusLevel.CRITICAL -> Icons.Default.Error
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status.label,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = color
        )
    }
}
