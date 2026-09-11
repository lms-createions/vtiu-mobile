package com.example.vtiu.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable")
data class TimetableEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val courseName: String,
    val courseCode: String,
    val day: String,
    val start: String,
    val end: String,
    val venue: String?
)
