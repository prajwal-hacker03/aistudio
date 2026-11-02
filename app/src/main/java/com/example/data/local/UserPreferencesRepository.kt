package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.GradingSystemType
import com.example.data.model.StudentProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("gradetrack_prefs", Context.MODE_PRIVATE)

    private val _profileFlow = MutableStateFlow(loadProfile())
    val profileFlow: StateFlow<StudentProfile> = _profileFlow.asStateFlow()

    private fun loadProfile(): StudentProfile {
        val name = prefs.getString("student_name", "Alex Morgan") ?: "Alex Morgan"
        val college = prefs.getString("student_college", "Institute of Technology") ?: "Institute of Technology"
        val university = prefs.getString("student_university", "National Technical University") ?: "National Technical University"
        val department = prefs.getString("student_dept", "Computer Science & Engineering") ?: "Computer Science & Engineering"
        val currentSemester = prefs.getInt("current_semester", 5)
        val targetCgpa = prefs.getFloat("target_cgpa", 9.0f).toDouble()
        val attendanceThreshold = prefs.getInt("attendance_threshold", 75)
        val yellowZoneMargin = prefs.getInt("yellow_zone_margin", 5)
        val isWeightedCgpa = prefs.getBoolean("is_weighted_cgpa", true)
        val cgpaMultiplier = prefs.getFloat("cgpa_multiplier", 9.5f).toDouble()
        val gradingSystemName = prefs.getString("grading_system", GradingSystemType.TEN_POINT_STANDARD.name)
            ?: GradingSystemType.TEN_POINT_STANDARD.name
        val gradingSystem = try {
            GradingSystemType.valueOf(gradingSystemName)
        } catch (_: Exception) {
            GradingSystemType.TEN_POINT_STANDARD
        }
        val isDarkMode = prefs.getBoolean("is_dark_mode", true)

        return StudentProfile(
            name = name,
            college = college,
            university = university,
            department = department,
            currentSemester = currentSemester,
            targetCgpa = targetCgpa,
            attendanceThreshold = attendanceThreshold,
            yellowZoneMargin = yellowZoneMargin,
            isWeightedCgpa = isWeightedCgpa,
            cgpaMultiplier = cgpaMultiplier,
            gradingSystem = gradingSystem,
            isDarkMode = isDarkMode
        )
    }

    fun updateProfile(newProfile: StudentProfile) {
        prefs.edit().apply {
            putString("student_name", newProfile.name)
            putString("student_college", newProfile.college)
            putString("student_university", newProfile.university)
            putString("student_dept", newProfile.department)
            putInt("current_semester", newProfile.currentSemester)
            putFloat("target_cgpa", newProfile.targetCgpa.toFloat())
            putInt("attendance_threshold", newProfile.attendanceThreshold)
            putInt("yellow_zone_margin", newProfile.yellowZoneMargin)
            putBoolean("is_weighted_cgpa", newProfile.isWeightedCgpa)
            putFloat("cgpa_multiplier", newProfile.cgpaMultiplier.toFloat())
            putString("grading_system", newProfile.gradingSystem.name)
            putBoolean("is_dark_mode", newProfile.isDarkMode)
            apply()
        }
        _profileFlow.value = newProfile
    }

    fun toggleTheme() {
        val current = _profileFlow.value
        val updated = current.copy(isDarkMode = !current.isDarkMode)
        updateProfile(updated)
    }
}
