package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.GangAvatar
import com.example.ui.components.MalluGangHeader
import com.example.ui.components.RoleBadge
import com.example.ui.theme.*
import com.example.viewmodel.GangViewModel

@Composable
fun HomeScreen(
    viewModel: GangViewModel,
    onNavigateToChat: (String) -> Unit,
    onNavigateToGame: (String) -> Unit,
    onOpenRules: () -> Unit,
    onOpenComplaint: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val gameRooms by viewModel.gameRooms.collectAsState()
    val events by viewModel.events.collectAsState()
    val polls by viewModel.polls.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    Scaffold(
        topBar = {
            MalluGangHeader(
                title = "Mallu Gang",
                subtitle = "Private Clubhouse • 6 Brothers & Sisters",
                actionIcon = Icons.Default.AccountCircle,
                onActionClick = onOpenProfile
            )
        },
        containerColor = DeepNavyBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("home_screen_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Welcome Card with User Identity & Role
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(EmeraldPrimary.copy(alpha = 0.5f), CyberTeal.copy(alpha = 0.3f)))
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GangAvatar(
                            initials = currentUser.avatarInitials,
                            sizeDp = 52,
                            colorHex = 0xFF00E676,
                            isOnline = true
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Namaskaram, ${currentUser.nickname}!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            RoleBadge(role = currentUser.role)
                        }
                    }
                }
            }

            // Quick Shortcut Action Pills
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionPill(
                        label = "📜 Gang Rules",
                        color = CyberTeal,
                        onClick = onOpenRules,
                        modifier = Modifier.weight(1f).testTag("action_rules_button")
                    )
                    ActionPill(
                        label = "🔒 Complaint Box",
                        color = WarmGoldAccent,
                        onClick = onOpenComplaint,
                        modifier = Modifier.weight(1f).testTag("action_complaint_button")
                    )
                }
            }

            // WebRTC Live Clubhouse Voice Channel Component
            item {
                com.example.ui.components.WebRtcVoiceChannelComponent()
            }

            // Section 1: Active Game Rooms (Live multiplayer action)
            item {
                SectionHeader(title = "🎮 Live Game Rooms", count = gameRooms.size)
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(gameRooms) { room ->
                        Card(
                            modifier = Modifier
                                .width(240.dp)
                                .clickable { onNavigateToGame(room.id) }
                                .testTag("game_room_card_${room.code}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = room.gameType.displayName,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary,
                                        fontSize = 15.sp
                                    )
                                    Surface(
                                        color = EmeraldContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = room.status,
                                            color = EmeraldPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Host: ${room.hostName}",
                                    color = TextSecondaryDark,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${room.players.size}/${room.maxPlayers} Players joined • Voice Live 🎙️",
                                    color = CyberTeal,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { onNavigateToGame(room.id) },
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                ) {
                                    Text("Enter Room", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Recent Conversations
            item {
                SectionHeader(title = "💬 Active Conversations", count = conversations.size)
            }
            items(conversations) { conv ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToChat(conv.id) }
                        .testTag("conversation_item_${conv.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GangAvatar(
                            initials = conv.name.take(2),
                            sizeDp = 44,
                            colorHex = conv.avatarColorHex,
                            isOnline = conv.isOnline
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = conv.name,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimaryDark,
                                    maxLines = 1
                                )
                                Text(
                                    text = conv.lastMessageTime,
                                    fontSize = 11.sp,
                                    color = TextSecondaryDark
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = conv.lastMessage,
                                fontSize = 13.sp,
                                color = TextSecondaryDark,
                                maxLines = 1
                            )
                        }
                        if (conv.unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = conv.unreadCount.toString(),
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Upcoming Events
            item {
                SectionHeader(title = "🌴 Upcoming Meetups & Trips", count = events.size)
            }
            items(events) { ev ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                text = ev.title,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark,
                                fontSize = 16.sp
                            )
                            Surface(
                                color = WarmAmber.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = ev.category.name,
                                    color = WarmGoldAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "📍 ${ev.location} • 🕒 ${ev.dateTime}",
                            fontSize = 12.sp,
                            color = CyberTeal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = ev.description,
                            fontSize = 13.sp,
                            color = TextSecondaryDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "👥 ${ev.goingCount} Going • Organized by ${ev.organizerName}",
                                fontSize = 12.sp,
                                color = TextSecondaryDark
                            )
                            FilterChip(
                                selected = ev.userRsvp == RsvpStatus.GOING,
                                onClick = {
                                    val next = if (ev.userRsvp == RsvpStatus.GOING) RsvpStatus.NOT_GOING else RsvpStatus.GOING
                                    viewModel.updateRsvp(ev.id, next)
                                },
                                label = { Text(if (ev.userRsvp == RsvpStatus.GOING) "I'm Going ✓" else "RSVP") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }
            }

            // Section 4: Live Polls
            item {
                SectionHeader(title = "📊 Gang Polls", count = polls.size)
            }
            items(polls) { poll ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = poll.question,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Created by ${poll.creatorName} • ${poll.totalVotes} votes",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        poll.options.forEach { opt ->
                            val isSelected = poll.userVotedOptionId == opt.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { viewModel.votePoll(poll.id, opt.id) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) EmeraldContainer else DarkSurfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = opt.text,
                                        color = if (isSelected) EmeraldPrimary else TextPrimaryDark,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${opt.votes} (${opt.percentage.toInt()}%)",
                                        color = if (isSelected) EmeraldPrimary else TextSecondaryDark,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryDark
        )
        Surface(
            color = DarkSurfaceVariant,
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "$count",
                color = EmeraldPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun ActionPill(label: String, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = DarkSurface,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(color.copy(alpha = 0.5f), color.copy(alpha = 0.1f)))
        )
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = color,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}
