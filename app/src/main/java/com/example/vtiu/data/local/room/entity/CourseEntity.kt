package com.example.vtiu.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: Int,
    val userId: String,
    val name: String,
    val code: String,
    val level: String,
    val credits: Int
)
