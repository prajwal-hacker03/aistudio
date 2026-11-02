package com.example.data.model

data class GradeScaleItem(
    val grade: String,
    val points: Double,
    val minPercentage: Double,
    val description: String = ""
)

enum class GradingSystemType(val title: String, val scaleMax: Double) {
    TEN_POINT_STANDARD("10-Point Standard (O, A+, A...)", 10.0),
    TEN_POINT_VTU("10-Point Technical (S, A, B...)", 10.0),
    FOUR_POINT_US("4-Point Scale (A, B, C...)", 4.0)
}

object GradeScales {
    val Standard10Point = listOf(
        GradeScaleItem("O", 10.0, 90.0, "Outstanding"),
        GradeScaleItem("A+", 9.0, 80.0, "Excellent"),
        GradeScaleItem("A", 8.0, 70.0, "Very Good"),
        GradeScaleItem("B+", 7.0, 60.0, "Good"),
        GradeScaleItem("B", 6.0, 50.0, "Above Average"),
        GradeScaleItem("C", 5.0, 45.0, "Average"),
        GradeScaleItem("P", 4.0, 40.0, "Pass"),
        GradeScaleItem("F", 0.0, 0.0, "Fail")
    )

    val Vtu10Point = listOf(
        GradeScaleItem("S", 10.0, 90.0, "Outstanding"),
        GradeScaleItem("A", 9.0, 80.0, "Excellent"),
        GradeScaleItem("B", 8.0, 70.0, "Very Good"),
        GradeScaleItem("C", 7.0, 60.0, "Good"),
        GradeScaleItem("D", 6.0, 50.0, "Above Average"),
        GradeScaleItem("E", 5.0, 40.0, "Pass"),
        GradeScaleItem("F", 0.0, 0.0, "Fail")
    )

    val Us4Point = listOf(
        GradeScaleItem("A", 4.0, 93.0, "Excellent"),
        GradeScaleItem("A-", 3.7, 90.0, "Great"),
        GradeScaleItem("B+", 3.3, 87.0, "Very Good"),
        GradeScaleItem("B", 3.0, 83.0, "Good"),
        GradeScaleItem("B-", 2.7, 80.0, "Competent"),
        GradeScaleItem("C+", 2.3, 77.0, "Fair"),
        GradeScaleItem("C", 2.0, 70.0, "Average"),
        GradeScaleItem("D", 1.0, 60.0, "Below Average"),
        GradeScaleItem("F", 0.0, 0.0, "Fail")
    )

    fun getScale(type: GradingSystemType): List<GradeScaleItem> {
        return when (type) {
            GradingSystemType.TEN_POINT_STANDARD -> Standard10Point
            GradingSystemType.TEN_POINT_VTU -> Vtu10Point
            GradingSystemType.FOUR_POINT_US -> Us4Point
        }
    }

    fun getGradeForPercentage(percentage: Double, type: GradingSystemType = GradingSystemType.TEN_POINT_STANDARD): GradeScaleItem {
        val scale = getScale(type)
        return scale.firstOrNull { percentage >= it.minPercentage } ?: scale.last()
    }

    fun getPointsForGrade(grade: String, type: GradingSystemType = GradingSystemType.TEN_POINT_STANDARD): Double {
        val scale = getScale(type)
        return scale.firstOrNull { it.grade.equals(grade, ignoreCase = true) }?.points ?: 0.0
    }
}
