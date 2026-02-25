package com.example.appworkcalendar.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.appworkcalendar.R
import com.example.appworkcalendar.data.Appointment
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningsScreen(
    appointments: List<Appointment>,
    range: Pair<LocalDate, LocalDate>,
    onRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val filtered = appointments.filter { it.localDate >= range.first && it.localDate <= range.second }
    val grouped = filtered.groupBy { it.localDate }.toSortedMap().mapValues { (_, list) -> list.sumOf { it.costRub } }
    val total = grouped.values.sum()

    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = { showPicker = true }) {
            Icon(Icons.Default.CalendarMonth, null)
            Text(
                stringResource(R.string.period, range.first.toString(), range.second.toString()),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
        ) {
            EarningsChart(values = grouped.values.toList())
        }
        Text(stringResource(R.string.total_earnings, total))
    }

    if (showPicker) {
        var firstClick by remember { mutableStateOf<LocalDate?>(null) }
        val pickerState = androidx.compose.material3.rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        val selected = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        if (firstClick == null) firstClick = selected
                        else {
                            onRangeSelected(firstClick!!, selected)
                            showPicker = false
                            firstClick = null
                        }
                    }
                }) { Text(stringResource(if (firstClick == null) R.string.pick_start else R.string.pick_end)) }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text(stringResource(R.string.close)) } }
        ) {
            Column {
                DatePicker(state = pickerState)
                firstClick?.let { Text(stringResource(R.string.start_label, it.toString()), modifier = Modifier.padding(8.dp)) }
            }
        }
    }
}

@Composable
private fun EarningsChart(values: List<Double>) {
    val max = (values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    Canvas(modifier = Modifier.fillMaxWidth().height(220.dp).padding(12.dp)) {
        val barWidth = size.width / (values.size.coerceAtLeast(1) * 1.5f)
        values.forEachIndexed { index, value ->
            val x = index * barWidth * 1.5f + barWidth / 2
            val h = (value / max).toFloat() * size.height
            drawLine(
                color = Color(0xFF74A8FF),
                start = Offset(x, size.height),
                end = Offset(x, size.height - h),
                strokeWidth = barWidth
            )
        }
    }
}
