package com.example.appworkcalendar.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DateScrollBar(selectedDate: LocalDate, onDateClick: (LocalDate) -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM", Locale("ru"))
    val dates = (-15..15).map { selectedDate.plusDays(it.toLong()) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        dates.forEach { date ->
            FilterChip(
                selected = date == selectedDate,
                onClick = { onDateClick(date) },
                label = { Text(date.format(formatter)) }
            )
        }
    }
}
