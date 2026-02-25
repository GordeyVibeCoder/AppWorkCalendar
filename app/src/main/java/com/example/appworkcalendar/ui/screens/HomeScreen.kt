package com.example.appworkcalendar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appworkcalendar.data.Appointment
import com.example.appworkcalendar.domain.ContactData
import com.example.appworkcalendar.ui.components.DateScrollBar
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    selectedDate: LocalDate,
    appointments: List<Appointment>,
    onDateChange: (LocalDate) -> Unit,
    onSave: (Appointment) -> Unit,
    onPickContact: (onPicked: (ContactData) -> Unit) -> Unit
) {
    var showCalendar by remember { mutableStateOf(false) }
    var showForm by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showForm = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            DateScrollBar(selectedDate = selectedDate, onDateClick = onDateChange)
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { showCalendar = true }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Text("Календарь", modifier = Modifier.padding(start = 8.dp))
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize().padding(12.dp)
            ) {
                if (appointments.isEmpty()) {
                    item {
                        Text("На этот день записей нет", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                items(appointments) { appointment ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${appointment.startTime} • ${appointment.procedureName}")
                            Text("${appointment.clientName} (${appointment.phone})")
                            Text("${appointment.durationMinutes} мин • ${appointment.costRub} ₽")
                        }
                    }
                }
            }
        }
    }

    if (showCalendar) {
        val state = rememberDatePickerState(selectedDate)
        DatePickerDialog(
            onDismissRequest = { showCalendar = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        onDateChange(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate())
                    }
                    showCalendar = false
                }) { Text("Ок") }
            },
            dismissButton = { TextButton(onClick = { showCalendar = false }) { Text("Отмена") } }
        ) { DatePicker(state = state) }
    }

    if (showForm) {
        AddAppointmentDialog(
            date = selectedDate,
            onDismiss = { showForm = false },
            onSave = {
                onSave(it)
                showForm = false
            },
            onPickContact = onPickContact
        )
    }
}

@Composable
private fun rememberDatePickerState(date: LocalDate): androidx.compose.material3.DatePickerState {
    val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    return androidx.compose.material3.rememberDatePickerState(initialSelectedDateMillis = millis)
}

@Composable
private fun AddAppointmentDialog(
    date: LocalDate,
    onDismiss: () -> Unit,
    onSave: (Appointment) -> Unit,
    onPickContact: (onPicked: (ContactData) -> Unit) -> Unit
) {
    var clientName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var procedure by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("60") }
    var startTime by remember { mutableStateOf("10:00") }
    var cost by remember { mutableStateOf("1500") }
    var showCancelConfirm by remember { mutableStateOf(false) }

    val dirty = listOf(clientName, phone, procedure, duration, startTime, cost).any { it.isNotBlank() }

    AlertDialog(
        onDismissRequest = {
            if (dirty) showCancelConfirm = true else onDismiss()
        },
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Новая запись")
                IconButton(onClick = { if (dirty) showCancelConfirm = true else onDismiss() }) {
                    Icon(Icons.Default.Close, contentDescription = "Закрыть")
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    onPickContact {
                        clientName = it.name
                        phone = it.phone
                    }
                }) {
                    Icon(Icons.Default.Contacts, contentDescription = null)
                    Text("Выбрать из контактов", modifier = Modifier.padding(start = 8.dp))
                }
                OutlinedTextField(value = clientName, onValueChange = { clientName = it }, label = { Text("Имя клиента") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Номер телефона") })
                OutlinedTextField(value = procedure, onValueChange = { procedure = it }, label = { Text("Процедура") })
                OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Длительность, мин") })
                OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Время начала (ЧЧ:ММ)") })
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Стоимость, ₽") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    Appointment(
                        clientName = clientName,
                        phone = phone,
                        procedureName = procedure,
                        durationMinutes = duration.toIntOrNull() ?: 0,
                        startTime = startTime,
                        costRub = cost.replace(',', '.').toDoubleOrNull() ?: 0.0,
                        date = date.toString()
                    )
                )
            }) { Text("Ок") }
        },
        dismissButton = {
            TextButton(onClick = { if (dirty) showCancelConfirm = true else onDismiss() }) { Text("Отмена") }
        }
    )

    if (showCancelConfirm) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            title = { Text("Отменить ввод?") },
            text = { Text("Данные формы будут потеряны.") },
            confirmButton = {
                TextButton(onClick = {
                    showCancelConfirm = false
                    onDismiss()
                }) { Text("Да") }
            },
            dismissButton = { TextButton(onClick = { showCancelConfirm = false }) { Text("Нет") } }
        )
    }
}
