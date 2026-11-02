package com.example.data.model

data class StudentProfile(
    val name: String = "Alex Morgan",
    val college: String = "Institute of Technology",
    val university: String = "National Technical University",
    val department: String = "Computer Science & Engineering",
    val currentSemester: Int = 5,
    val targetCgpa: Double = 9.0,
    val attendanceThreshold: Int = 75, // e.g. 75%
    val yellowZoneMargin: Int = 5,      // within 5 percentage points above threshold is Warning (75-80%)
    val isWeightedCgpa: Boolean = true,  // true for credit weighted, false for simple average
    val cgpaMultiplier: Double = 9.5,    // multiplier for percentage conversion (e.g., 9.5 or 10.0)
    val gradingSystem: GradingSystemType = GradingSystemType.TEN_POINT_STANDARD,
    val isDarkMode: Boolean = true
)
