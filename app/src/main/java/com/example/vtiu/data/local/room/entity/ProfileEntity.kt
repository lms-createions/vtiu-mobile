package com.example.vtiu.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val userId: String,
    val username: String,
    val name: String,
    val email: String?,
    val role: String,
    val department: String?,
    val employeeId: String?,
    val qualification: String?,
    val specialization: String?,
    val officeLocation: String?,
    val programme: String?,
    val level: Int?,
    val indexNumber: String?,
    val profilePictureUrl: String?,
    val dob: String?,
    val gender: String?,
    val nationality: String?,
    val religion: String?,
    val phone: String?,
    val address: String?,
    val academicYear: String?,
    val semester: String?,
    val academicStatus: String?
)
