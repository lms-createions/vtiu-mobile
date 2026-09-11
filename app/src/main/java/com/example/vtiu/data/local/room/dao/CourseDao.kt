package com.example.vtiu.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.vtiu.data.local.room.entity.CourseEntity

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses WHERE userId = :userId")
    suspend fun getCourses(userId: String): List<CourseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCourses(courses: List<CourseEntity>)

    @Query("DELETE FROM courses WHERE userId = :userId")
    suspend fun deleteCourses(userId: String)
}
