package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClubhouseFirestoreEvent
import com.example.ui.theme.*
import com.example.viewmodel.ClubhouseEventsViewModel
import com.example.viewmodel.EventsUiState

@Composable
fun ClubhouseEventsListComponent(
    viewModel: ClubhouseEventsViewModel,
    modifier: Modifier = Modifier,
    onScheduleNewEventClick: () -> Unit = {}
) {
    val eventsState by viewModel.eventsUiState.collectAsState()
    val isCreating by viewModel.isCreatingEvent.collectAsState()
    val errorMsg by viewModel.operationError.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBg)
            .testTag("clubhouse_events_container")
    ) {
        // Section Header with Add Event action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🎮 Social Gaming & Meetups",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Text(
                    text = "Live tournaments, trips & clubhouse hangouts",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )
            }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("schedule_event_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Host Event", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Error message banner
        errorMsg?.let { err ->
            Surface(
                color = SoftError.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = err, color = SoftError, fontSize = 12.sp)
                    IconButton(onClick = { viewModel.clearError() }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = SoftError)
                    }
                }
            }
        }

        // List Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (val state = eventsState) {
                is EventsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EmeraldPrimary, modifier = Modifier.size(36.dp))
                    }
                }
                is EventsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⚠️ Connection Issue", fontWeight = FontWeight.Bold, color = SoftError, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(state.message, color = TextSecondaryDark, fontSize = 12.sp)
                            }
                        }
                    }
                }
                is EventsUiState.Success -> {
                    if (state.events.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.SportsEsports, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No upcoming clubhouse events.", color = TextSecondaryDark, fontSize = 14.sp)
                                Text("Host a Ludo championship or road trip!", color = TextMutedDark, fontSize = 12.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("events_lazy_list"),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.events, key = { it.id }) { event ->
                                EventCardItem(event = event)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for creating a new social gaming meetup
    if (showCreateDialog) {
        CreateEventDialog(
            isCreating = isCreating,
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, desc, loc, dateTime, game ->
                viewModel.scheduleGamingEvent(
                    title = title,
                    description = desc,
                    location = loc,
                    dateTime = dateTime,
                    gameType = game,
                    onSuccess = { showCreateDialog = false }
                )
            }
        )
    }
}

@Composable
private fun EventCardItem(event: ClubhouseFirestoreEvent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("event_item_${event.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.title,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = CyberTeal.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "🎮 ${event.gameType}",
                        color = CyberTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = event.location, fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.Schedule, contentDescription = null, tint = TextSecondaryDark, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = event.dateTime, fontSize = 12.sp, color = TextSecondaryDark)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.description,
                fontSize = 13.sp,
                color = TextSecondaryDark,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Organized by ${event.organizerName}",
                    fontSize = 11.sp,
                    color = TextMutedDark
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    var isReminderSet by remember { mutableStateOf(false) }
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val scheduler = remember { com.example.notification.EventNotificationScheduler(context) }

                    IconButton(
                        onClick = {
                            isReminderSet = !isReminderSet
                            if (isReminderSet) {
                                scheduler.scheduleEventReminder(event, delayMillis = 10_000L)
                            } else {
                                scheduler.cancelEventReminder(event.id)
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isReminderSet) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                            contentDescription = "Set Reminder",
                            tint = if (isReminderSet) WarmGoldAccent else TextSecondaryDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = EmeraldContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "👥 ${event.goingCount} Joined",
                            color = EmeraldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateEventDialog(
    isCreating: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, loc: String, dateTime: String, game: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf("") }
    var selectedGame by remember { mutableStateOf("Ludo Master") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Host Social Gaming Event", fontWeight = FontWeight.Bold, color = TextPrimaryDark)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
                    placeholder = { Text("e.g. Weekend Ludo Championship") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimaryDark
                    )
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description & Stakes") },
                    placeholder = { Text("Rules, prize chai, entry requirements...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimaryDark
                    )
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    placeholder = { Text("e.g. Clubhouse Voice Room / Kochi") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimaryDark
                    )
                )
                OutlinedTextField(
                    value = dateTime,
                    onValueChange = { dateTime = it },
                    label = { Text("Date & Time") },
                    placeholder = { Text("e.g. Sat, 10 Oct • 08:00 PM") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimaryDark
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, desc, location, dateTime, selectedGame) },
                enabled = title.isNotBlank() && !isCreating,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                if (isCreating) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black)
                } else {
                    Text("Publish Event", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        },
        containerColor = DarkSurface
    )
}
