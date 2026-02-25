package com.example.appworkcalendar.data

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate

class AppRepository(
    private val dao: AppointmentDao,
    private val json: Json = Json { prettyPrint = true; ignoreUnknownKeys = true }
) {
    fun observeAppointmentsByDate(date: LocalDate): Flow<List<Appointment>> =
        dao.observeByDate(date.toString()).map { list -> list.map { it.toDomain() } }

    fun observeAllAppointments(): Flow<List<Appointment>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun saveAppointment(appointment: Appointment) {
        dao.insert(appointment.toEntity())
    }

    suspend fun exportToJson(contentResolver: ContentResolver, uri: Uri) {
        val all = dao.observeAll().map { it.map(AppointmentEntity::toDomain) }
        val payload = BackupPayload(Instant.now().toString(), all.first())
        contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
            writer.write(json.encodeToString(payload))
        }
    }

    suspend fun importFromJson(contentResolver: ContentResolver, uri: Uri) {
        val payload = contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
            json.decodeFromString<BackupPayload>(reader.readText())
        } ?: return
        dao.clearAll()
        dao.insertAll(payload.appointments.map { it.toEntity() })
    }
}
