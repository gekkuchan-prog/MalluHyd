package com.example.ui.screens

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
import com.example.model.*
import com.example.ui.components.MalluGangHeader
import com.example.ui.theme.*
import com.example.viewmodel.GangViewModel

@Composable
fun CommunityScreen(
    viewModel: GangViewModel,
    onOpenRules: () -> Unit,
    onOpenComplaint: () -> Unit
) {
    var selectedCommunityTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Events & Trips", "Polls", "Expenses", "Memories")

    val events by viewModel.events.collectAsState()
    val polls by viewModel.polls.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    Scaffold(
        topBar = {
            MalluGangHeader(
                title = "Mallu Gang Clubhouse",
                subtitle = "Events, Polls, Shared Bills & Memories",
                actionIcon = Icons.Default.Gavel,
                onActionClick = onOpenRules
            )
        },
        containerColor = DeepNavyBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector Row
            ScrollableTabRow(
                selectedTabIndex = selectedCommunityTab,
                containerColor = DarkSurface,
                contentColor = EmeraldPrimary,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedCommunityTab == index,
                        onClick = { selectedCommunityTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedCommunityTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCommunityTab == index) EmeraldPrimary else TextSecondaryDark
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("community_screen_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedCommunityTab) {
                    0 -> { // Events
                        item {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            val repo = remember { com.example.repository.ClubhouseFirestoreRepository(context) }
                            val firestoreEvents by repo.observeClubhouseEvents().collectAsState(initial = emptyList())

                            com.example.ui.components.ClubhouseCalendarVisualizationComponent(
                                events = if (firestoreEvents.isNotEmpty()) firestoreEvents else listOf(
                                    com.example.model.ClubhouseFirestoreEvent(
                                        id = "ev_1",
                                        title = "🌴 Wayanad Camping & Night Trek",
                                        description = "Tent camping near Chembra peak, stargazing and barbecue.",
                                        location = "Meppadi, Wayanad",
                                        dateTime = "Sat, 24 Oct 2026 • 04:00 PM",
                                        goingCount = 5
                                    ),
                                    com.example.model.ClubhouseFirestoreEvent(
                                        id = "ev_2",
                                        title = "🎂 Kichu's Birthday Biryani Party",
                                        description = "Thalassery dum biryani feast + gaming tournament.",
                                        location = "Kaloor, Kochi",
                                        dateTime = "Sun, 01 Nov 2026 • 01:00 PM",
                                        goingCount = 6
                                    )
                                )
                            )
                        }

                        items(events) { ev ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(text = ev.title, fontWeight = FontWeight.Bold, color = TextPrimaryDark, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "📍 ${ev.location} • 🕒 ${ev.dateTime}", fontSize = 12.sp, color = CyberTeal)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = ev.description, fontSize = 13.sp, color = TextSecondaryDark)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "👥 ${ev.goingCount} Going", color = EmeraldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Button(
                                            onClick = {
                                                val next = if (ev.userRsvp == RsvpStatus.GOING) RsvpStatus.NOT_GOING else RsvpStatus.GOING
                                                viewModel.updateRsvp(ev.id, next)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (ev.userRsvp == RsvpStatus.GOING) EmeraldPrimary else DarkSurfaceVariant
                                            )
                                        ) {
                                            Text(
                                                text = if (ev.userRsvp == RsvpStatus.GOING) "Attending ✓" else "Join Meetup",
                                                color = if (ev.userRsvp == RsvpStatus.GOING) Color.Black else TextPrimaryDark,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> { // Polls
                        items(polls) { poll ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(text = poll.question, fontWeight = FontWeight.Bold, color = TextPrimaryDark, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "${poll.totalVotes} total votes • ${poll.closesInText}", fontSize = 11.sp, color = TextSecondaryDark)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    poll.options.forEach { opt ->
                                        val isVoted = poll.userVotedOptionId == opt.id
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clickable { viewModel.votePoll(poll.id, opt.id) },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isVoted) EmeraldContainer else DarkSurfaceVariant
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = opt.text, color = if (isVoted) EmeraldPrimary else TextPrimaryDark, fontSize = 13.sp)
                                                Text(text = "${opt.votes} (${opt.percentage.toInt()}%)", color = if (isVoted) EmeraldPrimary else TextSecondaryDark, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> { // Expenses
                        item {
                            com.example.ui.components.SharedExpenseSummaryComponent(
                                viewModel = viewModel,
                                modifier = Modifier.fillParentMaxSize()
                            )
                        }
                    }

                    3 -> { // Memories / Albums
                        item {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            val repo = remember { com.example.repository.ClubhouseFirestoreRepository(context) }
                            val galleryVm = remember { com.example.viewmodel.PhotoGalleryViewModel(repo) }

                            com.example.ui.components.SharedPhotoGalleryComponent(
                                viewModel = galleryVm,
                                modifier = Modifier.fillParentMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
