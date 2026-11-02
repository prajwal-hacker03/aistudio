package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.GradeTrackDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.GradeScaleItem
import com.example.data.model.GradeScales
import com.example.data.model.GradingSystemType
import com.example.data.model.SemesterEntity
import com.example.data.model.StudentProfile
import com.example.data.model.SubjectEntity
import com.example.data.repository.AcademicSummary
import com.example.data.repository.GradeTrackRepository
import com.example.domain.GradeTrackMath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Calculator draft subject row
data class SgpaDraftSubject(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val code: String = "",
    val credits: Int = 3,
    val grade: String = "A",
    val gradePoints: Double = 8.0,
    val marks: Double = 75.0
)

class GradeTrackViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GradeTrackDatabase.getDatabase(application)
    private val preferencesRepository = UserPreferencesRepository(application)

    val repository = GradeTrackRepository(
        subjectDao = database.subjectDao(),
        semesterDao = database.semesterDao(),
        attendanceLogDao = database.attendanceLogDao(),
        preferencesRepository = preferencesRepository,
        scope = viewModelScope
    )

    val summary: StateFlow<AcademicSummary> = repository.academicSummary
    val allSubjects: StateFlow<List<SubjectEntity>> = repository.allSubjects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val allSemesters: StateFlow<List<SemesterEntity>> = repository.allSemesters.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val studentProfile: StateFlow<StudentProfile> = repository.studentProfile

    // UI Feedback Message (e.g. snackbars)
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    // ================= SGPA Calculator State =================
    private val _sgpaDraftSemester = MutableStateFlow(5)
    val sgpaDraftSemester: StateFlow<Int> = _sgpaDraftSemester.asStateFlow()

    private val _sgpaDraftSubjects = MutableStateFlow<List<SgpaDraftSubject>>(
        listOf(
            SgpaDraftSubject(name = "Data Structures", code = "CS301", credits = 4, grade = "O", gradePoints = 10.0),
            SgpaDraftSubject(name = "Discrete Mathematics", code = "MA301", credits = 4, grade = "A+", gradePoints = 9.0),
            SgpaDraftSubject(name = "Digital Electronics", code = "EC301", credits = 3, grade = "A", gradePoints = 8.0),
            SgpaDraftSubject(name = "Object Oriented Java", code = "CS302", credits = 3, grade = "A+", gradePoints = 9.0)
        )
    )
    val sgpaDraftSubjects: StateFlow<List<SgpaDraftSubject>> = _sgpaDraftSubjects.asStateFlow()

    fun setSgpaDraftSemester(semester: Int) {
        _sgpaDraftSemester.value = semester
    }

    fun addSgpaDraftSubject() {
        val currentList = _sgpaDraftSubjects.value.toMutableList()
        val num = currentList.size + 1
        currentList.add(
            SgpaDraftSubject(
                name = "Subject $num",
                code = "SUB$num",
                credits = 3,
                grade = "A",
                gradePoints = 8.0
            )
        )
        _sgpaDraftSubjects.value = currentList
    }

    fun updateSgpaDraftSubject(index: Int, updated: SgpaDraftSubject) {
        val currentList = _sgpaDraftSubjects.value.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = updated
            _sgpaDraftSubjects.value = currentList
        }
    }

    fun removeSgpaDraftSubject(index: Int) {
        val currentList = _sgpaDraftSubjects.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _sgpaDraftSubjects.value = currentList
        }
    }

    fun saveDraftAsSemesterRecord() {
        viewModelScope.launch {
            val drafts = _sgpaDraftSubjects.value
            val semNum = _sgpaDraftSemester.value
            val pairs = drafts.map { it.credits to it.gradePoints }
            val sgpa = GradeTrackMath.calculateSGPA(pairs)
            val credits = drafts.sumOf { it.credits }

            repository.insertSemester(
                SemesterEntity(
                    semesterNumber = semNum,
                    sgpa = sgpa,
                    credits = credits,
                    isManualOverride = true,
                    notes = "Calculated & Saved"
                )
            )
            _snackbarMessage.value = "Semester $semNum SGPA ($sgpa) saved successfully!"
        }
    }

    fun saveDraftSubjectsToDatabase() {
        viewModelScope.launch {
            val drafts = _sgpaDraftSubjects.value
            val semNum = _sgpaDraftSemester.value
            for (draft in drafts) {
                repository.insertSubject(
                    SubjectEntity(
                        name = draft.name.ifBlank { "Subject" },
                        code = draft.code.ifBlank { "CODE" },
                        semester = semNum,
                        credits = draft.credits,
                        totalMarks = draft.marks,
                        grade = draft.grade,
                        gradePoints = draft.gradePoints,
                        totalClasses = 0,
                        attendedClasses = 0
                    )
                )
            }
            _snackbarMessage.value = "${drafts.size} subjects saved to Semester $semNum!"
        }
    }

    // ================= Subject Actions =================
    fun addSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.insertSubject(subject)
            _snackbarMessage.value = "Subject '${subject.name}' added"
        }
    }

    fun updateSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.updateSubject(subject)
            _snackbarMessage.value = "Subject '${subject.name}' updated"
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            _snackbarMessage.value = "Subject '${subject.name}' deleted"
        }
    }

    // ================= Semester Actions =================
    fun addOrUpdateSemester(semester: SemesterEntity) {
        viewModelScope.launch {
            repository.insertSemester(semester)
            _snackbarMessage.value = "Semester ${semester.semesterNumber} saved"
        }
    }

    fun deleteSemester(semester: SemesterEntity) {
        viewModelScope.launch {
            repository.deleteSemester(semester)
            _snackbarMessage.value = "Semester ${semester.semesterNumber} removed"
        }
    }

    // ================= Attendance Actions =================
    fun markPresent(subjectId: Long) {
        viewModelScope.launch {
            repository.markPresent(subjectId)
        }
    }

    fun markAbsent(subjectId: Long) {
        viewModelScope.launch {
            repository.markAbsent(subjectId)
        }
    }

    fun undoAttendance(subjectId: Long) {
        viewModelScope.launch {
            val success = repository.undoLastAttendance(subjectId)
            if (success) {
                _snackbarMessage.value = "Attendance entry undone"
            } else {
                _snackbarMessage.value = "No recent attendance to undo"
            }
        }
    }

    fun resetAttendance(subjectId: Long) {
        viewModelScope.launch {
            repository.resetAttendance(subjectId)
            _snackbarMessage.value = "Attendance reset"
        }
    }

    // ================= Settings & Profile =================
    fun updateProfile(profile: StudentProfile) {
        repository.updateProfile(profile)
        _snackbarMessage.value = "Profile settings updated"
    }

    fun toggleDarkMode() {
        repository.toggleTheme()
    }
}
