package com.example.vtiu.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudentProfile(
    @SerialName("user_id")
    val userId: String,
    @SerialName("index_number")
    val indexNumber: String?,
    @SerialName("current_programme")
    val currentProgramme: String,
    @SerialName("programme_level")
    val programmeLevel: Int,
    @SerialName("study_format")
    val studyFormat: String,
    @SerialName("academic_status")
    val academicStatus: String,
    val phone: String?,
    val email: String?,
    @SerialName("dob")
    val dob: String? = null,
    val gender: String? = null,
    val nationality: String? = null,
    val religion: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    @SerialName("postal_code")
    val postalCode: String? = null,
    @SerialName("academic_year")
    val academicYear: String? = null,
    val semester: String? = null,
    @SerialName("admission_date")
    val admissionDate: String? = null
)
