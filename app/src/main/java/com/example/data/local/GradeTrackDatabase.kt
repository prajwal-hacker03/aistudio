package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AttendanceLogEntity
import com.example.data.model.SemesterEntity
import com.example.data.model.SubjectEntity

@Database(
    entities = [
        SubjectEntity::class,
        SemesterEntity::class,
        AttendanceLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GradeTrackDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun semesterDao(): SemesterDao
    abstract fun attendanceLogDao(): AttendanceLogDao

    companion object {
        @Volatile
        private var INSTANCE: GradeTrackDatabase? = null

        fun getDatabase(context: Context): GradeTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GradeTrackDatabase::class.java,
                    "gradetrack_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
