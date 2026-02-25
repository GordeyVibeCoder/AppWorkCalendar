package com.example.appworkcalendar.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments WHERE date = :date ORDER BY startTime")
    fun observeByDate(date: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments ORDER BY date, startTime")
    fun observeAll(): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: AppointmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appointments: List<AppointmentEntity>)

    @Query("DELETE FROM appointments")
    suspend fun clearAll()
}
