package com.example.vtiu.data.mapper

import com.example.vtiu.data.local.room.entity.ProfileEntity
import com.example.vtiu.data.local.room.entity.TimetableEntity
import com.example.vtiu.data.local.room.entity.CourseEntity
import com.example.vtiu.data.model.api.UserProfileData
import com.example.vtiu.data.model.api.TimetableEntryApi
import com.example.vtiu.data.model.api.StudentCourseApi

fun UserProfileData.toEntity(): ProfileEntity {
    return ProfileEntity(
        userId = userId,
        username = username,
        name = name,
        email = email,
        role = role,
        department = department,
        employeeId = employeeId,
        qualification = qualification,
        specialization = specialization,
        officeLocation = officeLocation,
        programme = programme,
        level = level,
        indexNumber = indexNumber,
        profilePictureUrl = profilePictureUrl,
        dob = dob,
        gender = gender,
        nationality = nationality,
        religion = religion,
        phone = phone,
        address = address,
        academicYear = academicYear,
        semester = semester,
        academicStatus = academicStatus
    )
}

fun ProfileEntity.toDomain(): UserProfileData {
    return UserProfileData(
        userId = userId,
        username = username,
        name = name,
        email = email,
        role = role,
        department = department,
        employeeId = employeeId,
        qualification = qualification,
        specialization = specialization,
        officeLocation = officeLocation,
        programme = programme,
        level = level,
        indexNumber = indexNumber,
        profilePictureUrl = profilePictureUrl,
        dob = dob,
        gender = gender,
        nationality = nationality,
        religion = religion,
        phone = phone,
        address = address,
        academicYear = academicYear,
        semester = semester,
        academicStatus = academicStatus
    )
}

fun TimetableEntryApi.toEntity(userId: String): TimetableEntity {
    return TimetableEntity(
        userId = userId,
        courseName = courseName,
        courseCode = courseCode,
        day = day,
        start = start,
        end = end,
        venue = venue
    )
}

fun TimetableEntity.toApi(): TimetableEntryApi {
    return TimetableEntryApi(
        courseName = courseName,
        courseCode = courseCode,
        day = day,
        start = start,
        end = end,
        venue = venue
    )
}

fun StudentCourseApi.toEntity(userId: String): CourseEntity {
    return CourseEntity(
        id = id,
        userId = userId,
        name = name,
        code = code,
        level = level,
        credits = credits
    )
}

fun CourseEntity.toApi(): StudentCourseApi {
    return StudentCourseApi(
        id = id,
        name = name,
        code = code,
        level = level,
        credits = credits
    )
}
