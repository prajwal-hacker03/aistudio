package com.example

import com.example.domain.GradeTrackMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GradeTrackMathTest {

    @Test
    fun testSGPACalculation_specExample() {
        // Spec Example:
        // Subject 1: 4 credits * 9 points = 36
        // Subject 2: 3 credits * 8 points = 24
        // Subject 3: 3 credits * 10 points = 30
        // Total points = 90, Total credits = 10 -> SGPA = 9.00
        val subjects = listOf(
            4 to 9.0,
            3 to 8.0,
            3 to 10.0
        )
        val sgpa = GradeTrackMath.calculateSGPA(subjects)
        assertEquals(9.00, sgpa, 0.001)
    }

    @Test
    fun testSGPACalculation_zeroCredits() {
        val sgpa = GradeTrackMath.calculateSGPA(emptyList())
        assertEquals(0.0, sgpa, 0.001)

        val nonCreditOnly = listOf(0 to 10.0, 0 to 9.0)
        assertEquals(0.0, GradeTrackMath.calculateSGPA(nonCreditOnly), 0.001)
    }

    @Test
    fun testAttendancePrediction_specExample() {
        // Spec Example:
        // Attended: 32, Conducted: 40, Target: 75%
        // Current attendance: 80%
        // Result: Maximum 2 additional classes can be missed while maintaining at least 75%.
        val result = GradeTrackMath.predictAttendance(attended = 32, conducted = 40, targetPercentage = 75)

        assertEquals(80.0, result.currentPercentage, 0.1)
        assertTrue(result.isTargetMet)
        assertEquals(2, result.maxAbsencesAllowed)
        assertEquals(0, result.additionalClassesNeeded)
    }

    @Test
    fun testAttendancePrediction_needToAttendClasses() {
        // Attended 20, Conducted 30 (66.7%). Target: 75%
        // (75 * 30 - 100 * 20) / (100 - 75) = (2250 - 2000) / 25 = 250 / 25 = 10 classes
        val result = GradeTrackMath.predictAttendance(attended = 20, conducted = 30, targetPercentage = 75)

        assertEquals(66.7, result.currentPercentage, 0.1)
        assertEquals(false, result.isTargetMet)
        assertEquals(10, result.additionalClassesNeeded)
        assertEquals(0, result.maxAbsencesAllowed)
    }

    @Test
    fun testAttendancePrediction_target100Percent() {
        // Missed classes: impossible to reach 100%
        val impossible = GradeTrackMath.predictAttendance(attended = 9, conducted = 10, targetPercentage = 100)
        assertTrue(impossible.isTargetImpossible)
        assertEquals(-1, impossible.additionalClassesNeeded)

        // Perfect attendance: met
        val perfect = GradeTrackMath.predictAttendance(attended = 10, conducted = 10, targetPercentage = 100)
        assertTrue(perfect.isTargetMet)
        assertEquals(0, perfect.additionalClassesNeeded)
        assertEquals(0, perfect.maxAbsencesAllowed)
    }

    @Test
    fun testCGPACalculation_weightedAndEqual() {
        // Sem 1: SGPA 8.0, 20 credits -> 160 points
        // Sem 2: SGPA 9.0, 25 credits -> 225 points
        // Total points = 385 / 45 credits = 8.56
        val semesters = listOf(
            8.0 to 20,
            9.0 to 25
        )
        val weighted = GradeTrackMath.calculateCGPA(semesters, isWeighted = true)
        assertEquals(8.56, weighted, 0.01)

        // Simple average: (8.0 + 9.0) / 2 = 8.50
        val equal = GradeTrackMath.calculateCGPA(semesters, isWeighted = false)
        assertEquals(8.50, equal, 0.01)
    }

    @Test
    fun testCgpaToPercentage() {
        // 9.0 * 9.5 = 85.5%
        val pct = GradeTrackMath.cgpaToPercentage(9.0, 9.5)
        assertEquals(85.5, pct, 0.01)
    }

    @Test
    fun testTargetCgpaPlanner() {
        // Current CGPA = 8.0, completed 40 credits
        // Remaining 20 credits, Target CGPA = 8.5
        // Total credits = 60
        // Target points = 8.5 * 60 = 510
        // Current points = 8.0 * 40 = 320
        // Needed = 190 / 20 = 9.50 GPA
        val result = GradeTrackMath.calculateTargetGpa(
            currentCgpa = 8.0,
            completedCredits = 40,
            remainingCredits = 20,
            targetCgpa = 8.5,
            scaleMax = 10.0
        )

        assertTrue(result is GradeTrackMath.TargetCgpaResult.Achievable)
        val achievable = result as GradeTrackMath.TargetCgpaResult.Achievable
        assertEquals(9.50, achievable.requiredGpa, 0.01)

        // Not achievable test:
        // Current 6.0, 50 credits, Target 9.5 with only 10 credits remaining
        val impossible = GradeTrackMath.calculateTargetGpa(
            currentCgpa = 6.0,
            completedCredits = 50,
            remainingCredits = 10,
            targetCgpa = 9.5,
            scaleMax = 10.0
        )
        assertTrue(impossible is GradeTrackMath.TargetCgpaResult.NotAchievable)
    }
}
