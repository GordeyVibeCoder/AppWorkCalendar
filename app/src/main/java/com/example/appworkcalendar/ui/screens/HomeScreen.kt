package com.example.appworkcalendar.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.appworkcalendar.R
import com.example.appworkcalendar.data.Appointment
import com.example.appworkcalendar.domain.ContactData
import com.example.appworkcalendar.ui.components.DateScrollBar
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val timeRegex = Regex("^([01]\\d|2[0-3]):([0-5]\\d)$")
private val phoneRegex = Regex("^\\+?\\d{10,15}$")

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
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            DateScrollBar(selectedDate = selectedDate, onDateClick = onDateChange)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { showCalendar = true }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Text(stringResource(R.string.calendar), modifier = Modifier.padding(start = 8.dp))
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize().padding(12.dp)
            ) {
                if (appointments.isEmpty()) item {
                    Text(stringResource(R.string.no_appointments), style = MaterialTheme.typography.bodyLarge)
                }
                items(appointments) { appointment ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.70f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${appointment.startTime} • ${appointment.procedureName}")
                            Text("${appointment.clientName} (${appointment.phone})")
                            Text(stringResource(R.string.minutes_short, appointment.durationMinutes, appointment.costRub))
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
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showCalendar = false }) { Text(stringResource(R.string.cancel)) } }
        ) { DatePicker(state = state) }
    }

    if (showForm) {
        AddAppointmentDialog(
            date = selectedDate,
            onDismiss = { showForm = false },
            onSave = { onSave(it); showForm = false },
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
    var duration by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var showCancelConfirm by remember { mutableStateOf(false) }

    val dirty = listOf(clientName, phone, procedure, duration, startTime, cost).any { it.isNotBlank() }
    val durationValue = duration.toIntOrNull()
    val costValue = cost.replace(',', '.').toDoubleOrNull()

    val nameError = if (clientName.isBlank()) stringResource(R.string.error_required) else null
    val phoneError = when {
        phone.isBlank() -> stringResource(R.string.error_required)
        !phoneRegex.matches(phone.trim()) -> stringResource(R.string.error_phone)
        else -> null
    }
    val procedureError = if (procedure.isBlank()) stringResource(R.string.error_required) else null
    val durationError = if (durationValue == null || durationValue <= 0) stringResource(R.string.error_duration) else null
    val timeError = if (!timeRegex.matches(startTime.trim())) stringResource(R.string.error_time) else null
    val costError = if (costValue == null || costValue <= 0) stringResource(R.string.error_cost) else null

    val canSave = listOf(nameError, phoneError, procedureError, durationError, timeError, costError).all { it == null }

    AlertDialog(
        onDismissRequest = { if (dirty) showCancelConfirm = true else onDismiss() },
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.new_appointment))
                IconButton(onClick = { if (dirty) showCancelConfirm = true else onDismiss() }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
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
                    Text(stringResource(R.string.pick_from_contacts), modifier = Modifier.padding(start = 8.dp))
                }
                InputField(clientName, { clientName = it }, R.string.client_name, nameError)
                InputField(phone, { phone = it }, R.string.phone, phoneError)
                InputField(procedure, { procedure = it }, R.string.procedure, procedureError)
                InputField(duration, { duration = it }, R.string.duration_minutes, durationError)
                InputField(startTime, { startTime = it }, R.string.start_time, timeError)
                InputField(cost, { cost = it }, R.string.cost_rub, costError)
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    onSave(
                        Appointment(
                            clientName = clientName.trim(),
                            phone = phone.trim(),
                            procedureName = procedure.trim(),
                            durationMinutes = durationValue!!,
                            startTime = startTime.trim(),
                            costRub = costValue!!,
                            date = date.toString()
                        )
                    )
                }
            ) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = { if (dirty) showCancelConfirm = true else onDismiss() }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )

    if (showCancelConfirm) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            title = { Text(stringResource(R.string.cancel_input_title)) },
            text = { Text(stringResource(R.string.cancel_input_text)) },
            confirmButton = {
                TextButton(onClick = { showCancelConfirm = false; onDismiss() }) { Text(stringResource(R.string.yes)) }
            },
            dismissButton = { TextButton(onClick = { showCancelConfirm = false }) { Text(stringResource(R.string.no)) } }
        )
    }
}

@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    labelRes: Int,
    error: String?
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelRes)) },
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        modifier = Modifier.fillMaxWidth()
    )
}
