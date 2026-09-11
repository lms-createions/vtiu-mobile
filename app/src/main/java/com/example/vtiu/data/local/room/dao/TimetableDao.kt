package com.example.vtiu.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.vtiu.data.local.room.entity.TimetableEntity

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable WHERE userId = :userId")
    suspend fun getTimetable(userId: String): List<TimetableEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTimetable(entries: List<TimetableEntity>)

    @Query("DELETE FROM timetable WHERE userId = :userId")
    suspend fun deleteTimetable(userId: String)
}
