package com.example.data.repository

import com.example.data.local.AttendanceLogDao
import com.example.data.local.SemesterDao
import com.example.data.local.SubjectDao
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.AttendanceLogEntity
import com.example.data.model.SemesterEntity
import com.example.data.model.StudentProfile
import com.example.data.model.SubjectEntity
import com.example.domain.GradeTrackMath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class EffectiveSemester(
    val semesterNumber: Int,
    val sgpa: Double,
    val credits: Int,
    val isManual: Boolean,
    val notes: String
)

data class AcademicSummary(
    val overallCgpa: Double,
    val latestSgpa: Double,
    val totalCreditsEarned: Int,
    val overallAttendancePercentage: Double,
    val totalClassesConducted: Int,
    val totalClassesAttended: Int,
    val totalSubjects: Int,
    val subjectsBelowThresholdCount: Int,
    val effectiveSemesters: List<EffectiveSemester>
)

class GradeTrackRepository(
    private val subjectDao: SubjectDao,
    private val semesterDao: SemesterDao,
    private val attendanceLogDao: AttendanceLogDao,
    private val preferencesRepository: UserPreferencesRepository,
    private val scope: CoroutineScope
) {
    val allSubjects: Flow<List<SubjectEntity>> = subjectDao.getAllSubjects()
    val allSemesters: Flow<List<SemesterEntity>> = semesterDao.getAllSemesters()
    val studentProfile: StateFlow<StudentProfile> = preferencesRepository.profileFlow

    // Single Source of Truth: Combines subjects, semesters, and profile settings into clean derived state
    val academicSummary: StateFlow<AcademicSummary> = combine(
        allSubjects,
        allSemesters,
        studentProfile
    ) { subjects, semesters, profile ->
        // Group subjects by semester
        val subjectsBySemester = subjects.groupBy { it.semester }

        // Find all semester numbers present either in subjects or semesters table
        val allSemNumbers = (semesters.map { it.semesterNumber } + subjectsBySemester.keys).distinct().sorted()

        val effectiveSemesters = allSemNumbers.map { semNum ->
            val manualRecord = semesters.firstOrNull { it.semesterNumber == semNum }
            val semSubjects = subjectsBySemester[semNum] ?: emptyList()

            if (manualRecord != null && manualRecord.isManualOverride) {
                EffectiveSemester(
                    semesterNumber = semNum,
                    sgpa = manualRecord.sgpa,
                    credits = manualRecord.credits,
                    isManual = true,
                    notes = manualRecord.notes
                )
            } else if (semSubjects.isNotEmpty()) {
                val creditPoints = semSubjects.map { it.credits to it.gradePoints }
                val derivedSgpa = GradeTrackMath.calculateSGPA(creditPoints)
                val totalCredits = semSubjects.sumOf { it.credits }
                EffectiveSemester(
                    semesterNumber = semNum,
                    sgpa = derivedSgpa,
                    credits = totalCredits,
                    isManual = false,
                    notes = "${semSubjects.size} subjects"
                )
            } else {
                EffectiveSemester(
                    semesterNumber = semNum,
                    sgpa = manualRecord?.sgpa ?: 0.0,
                    credits = manualRecord?.credits ?: 0,
                    isManual = manualRecord?.isManualOverride ?: false,
                    notes = manualRecord?.notes ?: ""
                )
            }
        }.filter { it.credits > 0 || it.sgpa > 0.0 }

        // Calculate cumulative CGPA
        val semPairs = effectiveSemesters.map { it.sgpa to it.credits }
        val overallCgpa = GradeTrackMath.calculateCGPA(semPairs, isWeighted = profile.isWeightedCgpa)
        val latestSgpa = effectiveSemesters.lastOrNull()?.sgpa ?: 0.0
        val totalCredits = effectiveSemesters.sumOf { it.credits }

        // Attendance stats across all enrolled subjects
        var totalConducted = 0
        var totalAttended = 0
        var belowThresholdCount = 0

        for (subj in subjects) {
            totalConducted += subj.totalClasses
            totalAttended += subj.attendedClasses
            val pct = subj.attendancePercentage
            if (subj.totalClasses > 0 && pct < profile.attendanceThreshold) {
                belowThresholdCount++
            }
        }

        val overallAttendance = GradeTrackMath.calculateAttendancePercentage(totalAttended, totalConducted)

        AcademicSummary(
            overallCgpa = overallCgpa,
            latestSgpa = latestSgpa,
            totalCreditsEarned = totalCredits,
            overallAttendancePercentage = overallAttendance,
            totalClassesConducted = totalConducted,
            totalClassesAttended = totalAttended,
            totalSubjects = subjects.size,
            subjectsBelowThresholdCount = belowThresholdCount,
            effectiveSemesters = effectiveSemesters
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = AcademicSummary(
            overallCgpa = 0.0,
            latestSgpa = 0.0,
            totalCreditsEarned = 0,
            overallAttendancePercentage = 100.0,
            totalClassesConducted = 0,
            totalClassesAttended = 0,
            totalSubjects = 0,
            subjectsBelowThresholdCount = 0,
            effectiveSemesters = emptyList()
        )
    )

    init {
        scope.launch {
            seedSampleDataIfEmpty()
        }
    }

    private suspend fun seedSampleDataIfEmpty() = withContext(Dispatchers.IO) {
        val existingSemesters = semesterDao.getSemesterByNumberDirect(1)
        if (existingSemesters == null) {
            // Seed Past Completed Semesters (Sem 1 to 4)
            val pastSemesters = listOf(
                SemesterEntity(semesterNumber = 1, sgpa = 8.65, credits = 22, isManualOverride = true, notes = "Completed"),
                SemesterEntity(semesterNumber = 2, sgpa = 8.80, credits = 24, isManualOverride = true, notes = "Completed"),
                SemesterEntity(semesterNumber = 3, sgpa = 9.15, credits = 23, isManualOverride = true, notes = "Completed"),
                SemesterEntity(semesterNumber = 4, sgpa = 8.95, credits = 24, isManualOverride = true, notes = "Completed")
            )
            semesterDao.insertSemesters(pastSemesters)

            // Seed Current Semester 5 Enrolled Subjects
            val currentSubjects = listOf(
                SubjectEntity(
                    name = "Design & Analysis of Algorithms",
                    code = "CS501",
                    semester = 5,
                    credits = 4,
                    internalMarks = 28.0,
                    assignmentMarks = 18.0,
                    practicalMarks = 23.0,
                    theoryMarks = 24.0,
                    totalMarks = 93.0,
                    maxMarks = 100.0,
                    grade = "O",
                    gradePoints = 10.0,
                    totalClasses = 42,
                    attendedClasses = 36,
                    minimumAttendance = 75
                ),
                SubjectEntity(
                    name = "Operating Systems",
                    code = "CS502",
                    semester = 5,
                    credits = 4,
                    internalMarks = 26.0,
                    assignmentMarks = 17.0,
                    practicalMarks = 22.0,
                    theoryMarks = 21.0,
                    totalMarks = 86.0,
                    maxMarks = 100.0,
                    grade = "A+",
                    gradePoints = 9.0,
                    totalClasses = 38,
                    attendedClasses = 31,
                    minimumAttendance = 75
                ),
                SubjectEntity(
                    name = "Database Management Systems",
                    code = "CS503",
                    semester = 5,
                    credits = 4,
                    internalMarks = 25.0,
                    assignmentMarks = 19.0,
                    practicalMarks = 24.0,
                    theoryMarks = 20.0,
                    totalMarks = 88.0,
                    maxMarks = 100.0,
                    grade = "A+",
                    gradePoints = 9.0,
                    totalClasses = 40,
                    attendedClasses = 35,
                    minimumAttendance = 75
                ),
                SubjectEntity(
                    name = "Computer Networks",
                    code = "CS504",
                    semester = 5,
                    credits = 3,
                    internalMarks = 22.0,
                    assignmentMarks = 15.0,
                    practicalMarks = 20.0,
                    theoryMarks = 18.0,
                    totalMarks = 75.0,
                    maxMarks = 100.0,
                    grade = "A",
                    gradePoints = 8.0,
                    totalClasses = 34,
                    attendedClasses = 24, // 70.5% (Warning zone!)
                    minimumAttendance = 75
                ),
                SubjectEntity(
                    name = "Software Engineering & Agile",
                    code = "CS505",
                    semester = 5,
                    credits = 3,
                    internalMarks = 27.0,
                    assignmentMarks = 18.0,
                    practicalMarks = 22.0,
                    theoryMarks = 23.0,
                    totalMarks = 90.0,
                    maxMarks = 100.0,
                    grade = "O",
                    gradePoints = 10.0,
                    totalClasses = 32,
                    attendedClasses = 28,
                    minimumAttendance = 75
                ),
                SubjectEntity(
                    name = "Professional Ethics & Law",
                    code = "HS501",
                    semester = 5,
                    credits = 2,
                    internalMarks = 24.0,
                    assignmentMarks = 16.0,
                    practicalMarks = 0.0,
                    theoryMarks = 40.0,
                    totalMarks = 80.0,
                    maxMarks = 100.0,
                    grade = "A+",
                    gradePoints = 9.0,
                    totalClasses = 26,
                    attendedClasses = 21,
                    minimumAttendance = 75
                )
            )
            subjectDao.insertSubjects(currentSubjects)
        }
    }

    // ================= Subject Actions =================
    suspend fun insertSubject(subject: SubjectEntity): Long = withContext(Dispatchers.IO) {
        subjectDao.insertSubject(subject)
    }

    suspend fun updateSubject(subject: SubjectEntity) = withContext(Dispatchers.IO) {
        subjectDao.updateSubject(subject)
    }

    suspend fun deleteSubject(subject: SubjectEntity) = withContext(Dispatchers.IO) {
        subjectDao.deleteSubject(subject)
        attendanceLogDao.deleteLogsForSubject(subject.id)
    }

    // ================= Semester Actions =================
    suspend fun insertSemester(semester: SemesterEntity) = withContext(Dispatchers.IO) {
        semesterDao.insertSemester(semester)
    }

    suspend fun updateSemester(semester: SemesterEntity) = withContext(Dispatchers.IO) {
        semesterDao.updateSemester(semester)
    }

    suspend fun deleteSemester(semester: SemesterEntity) = withContext(Dispatchers.IO) {
        semesterDao.deleteSemester(semester)
    }

    // ================= Attendance Actions & Reliable Undo =================
    suspend fun markPresent(subjectId: Long) = withContext(Dispatchers.IO) {
        val subject = subjectDao.getSubjectByIdDirect(subjectId) ?: return@withContext
        val newAttended = subject.attendedClasses + 1
        val newTotal = subject.totalClasses + 1
        subjectDao.updateAttendance(subjectId, newTotal, newAttended)
        attendanceLogDao.insertLog(
            AttendanceLogEntity(
                subjectId = subjectId,
                status = "PRESENT"
            )
        )
    }

    suspend fun markAbsent(subjectId: Long) = withContext(Dispatchers.IO) {
        val subject = subjectDao.getSubjectByIdDirect(subjectId) ?: return@withContext
        val newTotal = subject.totalClasses + 1
        subjectDao.updateAttendance(subjectId, newTotal, subject.attendedClasses)
        attendanceLogDao.insertLog(
            AttendanceLogEntity(
                subjectId = subjectId,
                status = "ABSENT"
            )
        )
    }

    suspend fun undoLastAttendance(subjectId: Long): Boolean = withContext(Dispatchers.IO) {
        val lastLog = attendanceLogDao.getLatestLogForSubject(subjectId) ?: return@withContext false
        val subject = subjectDao.getSubjectByIdDirect(subjectId) ?: return@withContext false

        if (lastLog.status == "PRESENT") {
            val updatedTotal = (subject.totalClasses - 1).coerceAtLeast(0)
            val updatedAttended = (subject.attendedClasses - 1).coerceAtLeast(0)
            subjectDao.updateAttendance(subjectId, updatedTotal, updatedAttended)
        } else if (lastLog.status == "ABSENT") {
            val updatedTotal = (subject.totalClasses - 1).coerceAtLeast(0)
            subjectDao.updateAttendance(subjectId, updatedTotal, subject.attendedClasses)
        }

        attendanceLogDao.deleteLog(lastLog)
        true
    }

    suspend fun resetAttendance(subjectId: Long) = withContext(Dispatchers.IO) {
        subjectDao.updateAttendance(subjectId, 0, 0)
        attendanceLogDao.deleteLogsForSubject(subjectId)
    }

    // ================= Profile & Preferences =================
    fun updateProfile(profile: StudentProfile) {
        preferencesRepository.updateProfile(profile)
    }

    fun toggleTheme() {
        preferencesRepository.toggleTheme()
    }
}
