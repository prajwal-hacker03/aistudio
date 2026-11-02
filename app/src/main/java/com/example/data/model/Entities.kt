package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String,
    val semester: Int,
    val credits: Int,
    val internalMarks: Double = 0.0,
    val assignmentMarks: Double = 0.0,
    val practicalMarks: Double = 0.0,
    val theoryMarks: Double = 0.0,
    val totalMarks: Double = 0.0,
    val maxMarks: Double = 100.0,
    val grade: String = "A",
    val gradePoints: Double = 8.0,
    val totalClasses: Int = 0,
    val attendedClasses: Int = 0,
    val minimumAttendance: Int = 75,
    val isCompleted: Boolean = false
) {
    val attendancePercentage: Double
        get() = if (totalClasses <= 0) 100.0 else ((attendedClasses.toDouble() / totalClasses.toDouble()) * 1000.0).toInt() / 10.0

    val classesAbsent: Int
        get() = (totalClasses - attendedClasses).coerceAtLeast(0)
}

@Entity(tableName = "semesters")
data class SemesterEntity(
    @PrimaryKey
    val semesterNumber: Int,
    val sgpa: Double,
    val credits: Int,
    val isManualOverride: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "attendance_logs")
data class AttendanceLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String // "PRESENT", "ABSENT", "CANCELLED"
)
