package com.example.vtiu.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimetableEntry(
    val title: String,
    @SerialName("start_time")
    val startTime: String,
    @SerialName("end_time")
    val endTime: String,
    @SerialName("is_break")
    val isBreak: Boolean = false,
    val room: String? = null
)

@Serializable
data class DayTimetable(
    val day: String,
    val entries: List<TimetableEntry>
)
