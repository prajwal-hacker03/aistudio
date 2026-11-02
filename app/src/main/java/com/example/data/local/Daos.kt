package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceLogEntity
import com.example.data.model.SemesterEntity
import com.example.data.model.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY semester ASC, name ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE semester = :semester ORDER BY name ASC")
    fun getSubjectsForSemester(semester: Int): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    fun getSubjectById(id: Long): Flow<SubjectEntity?>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectByIdDirect(id: Long): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubjectById(id: Long)

    @Query("UPDATE subjects SET totalClasses = :total, attendedClasses = :attended WHERE id = :id")
    suspend fun updateAttendance(id: Long, total: Int, attended: Int)

    @Query("DELETE FROM subjects")
    suspend fun deleteAllSubjects()
}

@Dao
interface SemesterDao {
    @Query("SELECT * FROM semesters ORDER BY semesterNumber ASC")
    fun getAllSemesters(): Flow<List<SemesterEntity>>

    @Query("SELECT * FROM semesters WHERE semesterNumber = :number")
    fun getSemesterByNumber(number: Int): Flow<SemesterEntity?>

    @Query("SELECT * FROM semesters WHERE semesterNumber = :number")
    suspend fun getSemesterByNumberDirect(number: Int): SemesterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSemester(semester: SemesterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSemesters(semesters: List<SemesterEntity>)

    @Update
    suspend fun updateSemester(semester: SemesterEntity)

    @Delete
    suspend fun deleteSemester(semester: SemesterEntity)

    @Query("DELETE FROM semesters WHERE semesterNumber = :number")
    suspend fun deleteSemesterByNumber(number: Int)

    @Query("DELETE FROM semesters")
    suspend fun deleteAllSemesters()
}

@Dao
interface AttendanceLogDao {
    @Query("SELECT * FROM attendance_logs WHERE subjectId = :subjectId ORDER BY timestamp DESC")
    fun getLogsForSubject(subjectId: Long): Flow<List<AttendanceLogEntity>>

    @Query("SELECT * FROM attendance_logs WHERE subjectId = :subjectId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestLogForSubject(subjectId: Long): AttendanceLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AttendanceLogEntity): Long

    @Delete
    suspend fun deleteLog(log: AttendanceLogEntity)

    @Query("DELETE FROM attendance_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM attendance_logs WHERE subjectId = :subjectId")
    suspend fun deleteLogsForSubject(subjectId: Long)
}
