package com.example.domain

import kotlin.math.max
import kotlin.math.roundToInt

object GradeTrackMath {

    /**
     * SGPA = Sum of (Credits * GradePoints) / Total Credits
     * Prevents division by zero. Excludes zero-credit non-graded subjects.
     */
    fun calculateSGPA(subjectCreditPoints: List<Pair<Int, Double>>): Double {
        var totalWeightedPoints = 0.0
        var totalCredits = 0

        for ((credits, points) in subjectCreditPoints) {
            if (credits > 0) {
                totalCredits += credits
                totalWeightedPoints += credits * points
            }
        }

        if (totalCredits == 0) return 0.0
        val raw = totalWeightedPoints / totalCredits
        return (raw * 100.0).roundToInt() / 100.0
    }

    /**
     * CGPA Calculation
     * If isWeighted = true: Sum(SGPA * Credits) / Sum(Credits)
     * If isWeighted = false: Simple arithmetic mean of SGPAs
     */
    fun calculateCGPA(semesters: List<Pair<Double, Int>>, isWeighted: Boolean = true): Double {
        if (semesters.isEmpty()) return 0.0

        return if (isWeighted) {
            var totalWeightedSgpa = 0.0
            var totalCredits = 0
            for ((sgpa, credits) in semesters) {
                if (credits > 0) {
                    totalCredits += credits
                    totalWeightedSgpa += sgpa * credits
                }
            }
            if (totalCredits == 0) return 0.0
            val raw = totalWeightedSgpa / totalCredits
            (raw * 100.0).roundToInt() / 100.0
        } else {
            val valid = semesters.filter { it.second > 0 || it.first > 0.0 }
            if (valid.isEmpty()) return 0.0
            val sum = valid.sumOf { it.first }
            val raw = sum / valid.size
            (raw * 100.0).roundToInt() / 100.0
        }
    }

    /**
     * Convert CGPA to percentage using university-specific multiplier (e.g., 9.5 or 10.0)
     */
    fun cgpaToPercentage(cgpa: Double, multiplier: Double = 9.5): Double {
        val raw = cgpa * multiplier
        return (raw * 10.0).roundToInt() / 10.0
    }

    /**
     * Current attendance percentage.
     * Returns 100.0 if conducted == 0 (no classes conducted yet).
     */
    fun calculateAttendancePercentage(attended: Int, conducted: Int): Double {
        if (conducted <= 0) return 100.0
        val safeAttended = attended.coerceIn(0, conducted)
        val raw = (safeAttended.toDouble() / conducted.toDouble()) * 100.0
        return (raw * 10.0).roundToInt() / 10.0
    }

    /**
     * Required Attendance Prediction Result
     */
    data class AttendancePredictionResult(
        val attended: Int,
        val conducted: Int,
        val currentPercentage: Double,
        val targetPercentage: Int,
        val additionalClassesNeeded: Int, // -1 if mathematically impossible
        val isTargetMet: Boolean,
        val isTargetImpossible: Boolean,
        val maxAbsencesAllowed: Int,
        val explanation: String
    )

    /**
     * Exact integer arithmetic for attendance prediction:
     * Condition to reach target P%: 100 * (A + X) >= P * (T + X)
     * Condition to safely miss Y classes: 100 * A >= P * (T + Y)
     */
    fun predictAttendance(attended: Int, conducted: Int, targetPercentage: Int): AttendancePredictionResult {
        val safeAttended = attended.coerceAtLeast(0)
        val safeConducted = conducted.coerceAtLeast(safeAttended)
        val target = targetPercentage.coerceIn(1, 100)

        val currentPct = calculateAttendancePercentage(safeAttended, safeConducted)

        // Case: Target P == 100%
        if (target == 100) {
            val impossible = safeAttended < safeConducted
            val met = safeAttended == safeConducted
            val needed = if (met) 0 else -1
            val maxMiss = 0
            val explanation = if (met) {
                "Your attendance is currently 100%. You cannot miss any classes to keep 100%."
            } else {
                "A 100% attendance is impossible because you have already missed ${safeConducted - safeAttended} class(es)."
            }
            return AttendancePredictionResult(
                attended = safeAttended,
                conducted = safeConducted,
                currentPercentage = currentPct,
                targetPercentage = target,
                additionalClassesNeeded = needed,
                isTargetMet = met,
                isTargetImpossible = impossible,
                maxAbsencesAllowed = maxMiss,
                explanation = explanation
            )
        }

        // Already met check using integer math: 100 * A >= P * T
        val isMet = (100 * safeAttended) >= (target * safeConducted)

        // Additional classes needed (X) using ceiling integer division:
        // (100 - P) * X >= P * T - 100 * A
        val additionalNeeded: Int
        if (isMet) {
            additionalNeeded = 0
        } else {
            val numerator = (target * safeConducted) - (100 * safeAttended)
            val denominator = 100 - target
            // ceil(numerator / denominator)
            additionalNeeded = (numerator + denominator - 1) / denominator
        }

        // Maximum safe absences (Y) using integer floor:
        // P * Y <= 100 * A - P * T
        val maxMissAllowed: Int
        if (!isMet) {
            maxMissAllowed = 0
        } else {
            val diff = (100 * safeAttended) - (target * safeConducted)
            maxMissAllowed = max(0, diff / target)
        }

        val explanation = when {
            isMet && maxMissAllowed > 0 -> {
                "Your attendance is ${currentPct}%. You can miss a maximum of $maxMissAllowed additional class(es) and still maintain at least $target%."
            }
            isMet && maxMissAllowed == 0 -> {
                "Your attendance is ${currentPct}%, right at your target! Missing even 1 more class will drop you below $target%."
            }
            else -> {
                "Your attendance is ${currentPct}%. You need to attend the next $additionalNeeded consecutive class(es) to reach $target%."
            }
        }

        return AttendancePredictionResult(
            attended = safeAttended,
            conducted = safeConducted,
            currentPercentage = currentPct,
            targetPercentage = target,
            additionalClassesNeeded = additionalNeeded,
            isTargetMet = isMet,
            isTargetImpossible = false,
            maxAbsencesAllowed = maxMissAllowed,
            explanation = explanation
        )
    }

    /**
     * Simulate future attendance after attending N classes and missing M classes
     */
    fun simulateAttendance(attended: Int, conducted: Int, additionalAttended: Int, additionalMissed: Int): Double {
        val totalAttended = (attended + additionalAttended).coerceAtLeast(0)
        val totalConducted = (conducted + additionalAttended + additionalMissed).coerceAtLeast(totalAttended)
        return calculateAttendancePercentage(totalAttended, totalConducted)
    }

    /**
     * Target CGPA Calculation Result
     */
    sealed class TargetCgpaResult {
        data class Achievable(
            val requiredGpa: Double,
            val targetCgpa: Double,
            val currentCgpa: Double,
            val completedCredits: Int,
            val remainingCredits: Int,
            val totalFinalCredits: Int,
            val effortDescription: String
        ) : TargetCgpaResult()

        data class AlreadyGuaranteed(
            val targetCgpa: Double,
            val currentCgpa: Double,
            val currentPointsAlreadyExceedTarget: Boolean
        ) : TargetCgpaResult()

        data class NotAchievable(
            val targetCgpa: Double,
            val maxPossibleCgpa: Double,
            val scaleMax: Double,
            val completedCredits: Int,
            val remainingCredits: Int
        ) : TargetCgpaResult()

        data class InvalidCredits(val message: String) : TargetCgpaResult()
    }

    /**
     * Calculate required GPA in remaining credits to achieve Target CGPA
     * Total Final Credits = Completed Credits + Remaining Credits
     * Required GPA = (Target * Total - Current * Completed) / Remaining
     */
    fun calculateTargetGpa(
        currentCgpa: Double,
        completedCredits: Int,
        remainingCredits: Int,
        targetCgpa: Double,
        scaleMax: Double = 10.0
    ): TargetCgpaResult {
        if (remainingCredits <= 0) {
            return TargetCgpaResult.InvalidCredits("Remaining credits must be greater than 0 to calculate target GPA.")
        }
        if (completedCredits < 0) {
            return TargetCgpaResult.InvalidCredits("Completed credits cannot be negative.")
        }

        val totalFinalCredits = completedCredits + remainingCredits
        val currentPoints = currentCgpa * completedCredits
        val targetTotalPoints = targetCgpa * totalFinalCredits
        val neededPoints = targetTotalPoints - currentPoints

        val requiredGpaRaw = neededPoints / remainingCredits
        val roundedRequired = (requiredGpaRaw * 100.0).roundToInt() / 100.0

        if (roundedRequired <= 0.0) {
            return TargetCgpaResult.AlreadyGuaranteed(
                targetCgpa = targetCgpa,
                currentCgpa = currentCgpa,
                currentPointsAlreadyExceedTarget = true
            )
        }

        if (roundedRequired > scaleMax) {
            val maxPossiblePoints = currentPoints + (scaleMax * remainingCredits)
            val maxPossibleCgpaRaw = maxPossiblePoints / totalFinalCredits
            val maxPossibleCgpa = (maxPossibleCgpaRaw * 100.0).roundToInt() / 100.0
            return TargetCgpaResult.NotAchievable(
                targetCgpa = targetCgpa,
                maxPossibleCgpa = maxPossibleCgpa,
                scaleMax = scaleMax,
                completedCredits = completedCredits,
                remainingCredits = remainingCredits
            )
        }

        val effort = when {
            roundedRequired <= scaleMax * 0.70 -> "Comfortable (Easily achievable with steady study)"
            roundedRequired <= scaleMax * 0.85 -> "Moderate (Consistent effort & good preparation required)"
            roundedRequired <= scaleMax * 0.95 -> "High Focus (Requires top grades across all subjects)"
            else -> "Maximum Effort (Near perfect scores needed)"
        }

        return TargetCgpaResult.Achievable(
            requiredGpa = roundedRequired,
            targetCgpa = targetCgpa,
            currentCgpa = currentCgpa,
            completedCredits = completedCredits,
            remainingCredits = remainingCredits,
            totalFinalCredits = totalFinalCredits,
            effortDescription = effort
        )
    }
}
