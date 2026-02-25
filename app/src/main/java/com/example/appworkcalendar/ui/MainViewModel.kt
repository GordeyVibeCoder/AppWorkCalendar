package com.example.appworkcalendar.ui

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appworkcalendar.data.AppRepository
import com.example.appworkcalendar.data.Appointment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel(private val repository: AppRepository) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val selectedRange = MutableStateFlow(LocalDate.now() to LocalDate.now())

    val appointments: StateFlow<List<Appointment>> =
        selectedDate
            .combine(repository.observeAllAppointments()) { date, all ->
                all.filter { it.localDate == date }.sortedBy { it.localTime }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAppointments: StateFlow<List<Appointment>> =
        repository.observeAllAppointments()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentDate: StateFlow<LocalDate> = selectedDate

    val earningsRange: StateFlow<Pair<LocalDate, LocalDate>> = selectedRange

    fun setDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun setRange(start: LocalDate, end: LocalDate) {
        selectedRange.value = if (start <= end) start to end else end to start
    }

    fun saveAppointment(appointment: Appointment) {
        viewModelScope.launch { repository.saveAppointment(appointment) }
    }

    fun export(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch { repository.exportToJson(contentResolver, uri) }
    }

    fun import(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch { repository.importFromJson(contentResolver, uri) }
    }

    companion object {
        fun factory(repository: AppRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(repository) as T
                }
            }
    }
}
