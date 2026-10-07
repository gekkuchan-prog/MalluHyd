package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClubhouseFirestoreEvent
import com.example.ui.theme.*
import java.util.Calendar

data class CalendarDayItem(
    val dayNumber: Int,
    val isCurrentMonth: Boolean,
    val hasEvents: Boolean = false,
    val eventsList: List<ClubhouseFirestoreEvent> = emptyList()
)

@Composable
fun ClubhouseCalendarVisualizationComponent(
    events: List<ClubhouseFirestoreEvent>,
    modifier: Modifier = Modifier
) {
    // Current viewed Month & Year (Defaults to October 2026 based on Gang timeline)
    var currentYear by remember { mutableStateOf(2026) }
    var currentMonth by remember { mutableStateOf(Calendar.OCTOBER) } // 0-indexed: 9 = October
    var selectedDayNumber by remember { mutableStateOf<Int?>(24) } // Default selected day (Wayanad Camping day)

    // Month name title
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    // Compute calendar grid days for current month
    val calendarDays = remember(currentYear, currentMonth, events) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, currentYear)
        cal.set(Calendar.MONTH, currentMonth)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val daysList = mutableListOf<CalendarDayItem>()

        // Padding previous month days
        for (i in 1 until firstDayOfWeek) {
            daysList.add(CalendarDayItem(dayNumber = 0, isCurrentMonth = false))
        }

        // Current month days
        for (day in 1..daysInMonth) {
            // Find events for this day
            val dayEvents = events.filter { ev ->
                // Check if date string contains day number or match
                ev.dateTime.contains("$day ", ignoreCase = true) ||
                ev.dateTime.contains("${day}th", ignoreCase = true) ||
                ev.dateTime.contains("${day}st", ignoreCase = true) ||
                ev.dateTime.contains("${day}nd", ignoreCase = true) ||
                ev.dateTime.contains("${day}rd", ignoreCase = true) ||
                (day == 24 && ev.title.contains("Wayanad", ignoreCase = true)) ||
                (day == 10 && ev.title.contains("Ludo", ignoreCase = true)) ||
                (day == 1 && ev.title.contains("Birthday", ignoreCase = true))
            }
            daysList.add(
                CalendarDayItem(
                    dayNumber = day,
                    isCurrentMonth = true,
                    hasEvents = dayEvents.isNotEmpty(),
                    eventsList = dayEvents
                )
            )
        }
        daysList
    }

    // Active selected day's events
    val selectedEvents = remember(selectedDayNumber, calendarDays) {
        calendarDays.find { it.dayNumber == selectedDayNumber }?.eventsList ?: emptyList()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("clubhouse_calendar_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header with Month Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📅 Clubhouse Calendar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "${monthNames[currentMonth]} $currentYear",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyberTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (currentMonth == Calendar.JANUARY) {
                                currentMonth = Calendar.DECEMBER
                                currentYear--
                            } else {
                                currentMonth--
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month", tint = TextSecondaryDark)
                    }

                    IconButton(
                        onClick = {
                            if (currentMonth == Calendar.DECEMBER) {
                                currentMonth = Calendar.JANUARY
                                currentYear++
                            } else {
                                currentMonth++
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month", tint = TextSecondaryDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Weekday header row (Sun to Sat)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { dayLabel ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayLabel,
                            fontSize = 12.sp,
                            color = if (dayLabel == "Su") SoftError else TextMutedDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Calendar Grid of Days
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .testTag("calendar_days_grid"),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(calendarDays) { item ->
                    if (!item.isCurrentMonth) {
                        Box(modifier = Modifier.aspectRatio(1f))
                    } else {
                        val isSelected = item.dayNumber == selectedDayNumber

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isSelected -> EmeraldPrimary
                                        item.hasEvents -> DarkSurfaceVariant
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = if (item.hasEvents && !isSelected) 1.dp else 0.dp,
                                    color = if (item.hasEvents && !isSelected) EmeraldPrimary.copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedDayNumber = item.dayNumber
                                }
                                .testTag("calendar_day_${item.dayNumber}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = item.dayNumber.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected || item.hasEvents) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> Color.Black
                                        item.hasEvents -> TextPrimaryDark
                                        else -> TextSecondaryDark
                                    }
                                )

                                if (item.hasEvents) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.Black else EmeraldPrimary)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = DarkSurfaceVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Event Details Section for Selected Date
            Text(
                text = "Events on ${selectedDayNumber ?: "Selected Date"} ${monthNames[currentMonth]}:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTeal
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedEvents.isEmpty()) {
                Surface(
                    color = DarkSurfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No scheduled gaming matches or road trips on this date. Tap another highlighted day or host an event!",
                        fontSize = 12.sp,
                        color = TextSecondaryDark,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    selectedEvents.forEach { ev ->
                        Surface(
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("calendar_event_detail_${ev.id}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = ev.title,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryDark,
                                        fontSize = 14.sp
                                    )
                                    Surface(
                                        color = EmeraldContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "👥 ${ev.goingCount} Going",
                                            color = EmeraldPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📍 ${ev.location} • 🕒 ${ev.dateTime}",
                                    fontSize = 11.sp,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ev.description,
                                    fontSize = 12.sp,
                                    color = TextSecondaryDark,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
