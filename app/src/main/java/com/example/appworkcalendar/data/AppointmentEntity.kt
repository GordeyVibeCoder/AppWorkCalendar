package com.example.appworkcalendar.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey val id: String,
    val clientName: String,
    val phone: String,
    val procedureName: String,
    val durationMinutes: Int,
    val startTime: String,
    val costRub: Double,
    val date: String
)

fun AppointmentEntity.toDomain() = Appointment(
    id = id,
    clientName = clientName,
    phone = phone,
    procedureName = procedureName,
    durationMinutes = durationMinutes,
    startTime = startTime,
    costRub = costRub,
    date = date
)

fun Appointment.toEntity() = AppointmentEntity(
    id = id,
    clientName = clientName,
    phone = phone,
    procedureName = procedureName,
    durationMinutes = durationMinutes,
    startTime = startTime,
    costRub = costRub,
    date = date
)
