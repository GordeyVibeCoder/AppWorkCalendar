package com.example.appworkcalendar.data

import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Serializable
data class Appointment(
    val id: String = UUID.randomUUID().toString(),
    val clientName: String,
    val phone: String,
    val procedureName: String,
    val durationMinutes: Int,
    val startTime: String,
    val costRub: Double,
    val date: String
) {
    val localDate: LocalDate get() = LocalDate.parse(date)
    val localTime: LocalTime get() = LocalTime.parse(startTime)
    val duration: Duration get() = Duration.ofMinutes(durationMinutes.toLong())
}

@Serializable
data class BackupPayload(
    val exportedAt: String,
    val appointments: List<Appointment>
)
