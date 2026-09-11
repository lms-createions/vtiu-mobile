package com.example.vtiu.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.vtiu.data.local.room.dao.ProfileDao
import com.example.vtiu.data.local.room.dao.TimetableDao
import com.example.vtiu.data.local.room.dao.CourseDao
import com.example.vtiu.data.local.room.entity.ProfileEntity
import com.example.vtiu.data.local.room.entity.TimetableEntity
import com.example.vtiu.data.local.room.entity.CourseEntity

@Database(entities = [ProfileEntity::class, TimetableEntity::class, CourseEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun timetableDao(): TimetableDao
    abstract fun courseDao(): CourseDao
}
