package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.model.*
import com.example.ui.components.GangAvatar
import com.example.ui.components.MalluGangHeader
import com.example.ui.theme.*
import com.example.viewmodel.GangViewModel

@Composable
fun ChatDetailScreen(
    conversationId: String,
    viewModel: GangViewModel,
    onBackClick: () -> Unit,
    onOpenGameRoom: (String) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val allMessages by viewModel.messages.collectAsState()
    val currentMessages = allMessages[conversationId] ?: emptyList()

    val currentConversation = conversations.find { it.id == conversationId }
    val convTitle = currentConversation?.name ?: "Mallu Gang Chat"

    var inputMessageText by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<Message?>(null) }
    var showGamePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MalluGangHeader(
                title = convTitle,
                subtitle = if (currentConversation?.isGroup == true) "${currentConversation.memberCount} members online" else "Active now",
                showBackButton = true,
                onBackClick = onBackClick,
                actionIcon = Icons.Default.SportsEsports,
                onActionClick = { showGamePicker = true }
            )
        },
        bottomBar = {
            // WhatsApp-style message composer bar
            Surface(
                color = DarkSurface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Reply banner if active
                    replyingToMessage?.let { replyMsg ->
                        Surface(
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Replying to ${replyMsg.senderName}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                    Text(
                                        text = replyMsg.text,
                                        fontSize = 12.sp,
                                        color = TextSecondaryDark,
                                        maxLines = 1
                                    )
                                }
                                IconButton(onClick = { replyingToMessage = null }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancel reply", tint = TextSecondaryDark)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick In-Chat Game Launcher button (Priority feature: start game while chatting)
                        IconButton(
                            onClick = { showGamePicker = true },
                            modifier = Modifier
                                .testTag("in_chat_game_launcher_button")
                                .size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = "Start Game",
                                tint = WarmGoldAccent
                            )
                        }

                        OutlinedTextField(
                            value = inputMessageText,
                            onValueChange = { inputMessageText = it },
                            placeholder = { Text("Message...", color = TextMutedDark, fontSize = 14.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_textfield"),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant,
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                if (inputMessageText.isNotBlank()) {
                                    viewModel.sendMessage(inputMessageText, replyingToMessage)
                                    inputMessageText = ""
                                    replyingToMessage = null
                                }
                            },
                            modifier = Modifier
                                .testTag("chat_send_button")
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.Black
                            )
                        }
                    }
                }
            }
        },
        containerColor = DeepNavyBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("chat_messages_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(currentMessages) { message ->
                val isSelf = message.senderId == currentUser.id

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start
                ) {
                    // Chat Bubble
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isSelf) 16.dp else 4.dp,
                            bottomEnd = if (isSelf) 4.dp else 16.dp
                        ),
                        color = if (isSelf) EmeraldContainer else DarkSurfaceVariant,
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .testTag("chat_bubble_${message.id}")
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            if (!isSelf) {
                                Text(
                                    text = message.senderName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTeal
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }

                            // Reply snippet if present
                            message.replyToText?.let { replySnippet ->
                                Surface(
                                    color = DeepNavyBg.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp)
                                ) {
                                    Column(modifier = Modifier.padding(6.dp)) {
                                        Text(
                                            text = message.replyToSender ?: "Reply",
                                            color = EmeraldPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = replySnippet,
                                            color = TextSecondaryDark,
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            // Regular text or Game Invitation Card
                            if (message.type == MessageType.GAME_INVITE && message.gamePayload != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DeepNavyBg)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "🎮 ${message.gamePayload.gameType} ROOM CREATED",
                                        fontWeight = FontWeight.Bold,
                                        color = WarmGoldAccent,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Room: ${message.gamePayload.roomCode} • Max ${message.gamePayload.maxPlayers} Players",
                                        color = TextSecondaryDark,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            // Find or open matching room
                                            val room = viewModel.gameRooms.value.find { it.code == message.gamePayload.roomCode }
                                            if (room != null) {
                                                onOpenGameRoom(room.id)
                                            } else {
                                                viewModel.openCreateGame(GameType.LUDO)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        modifier = Modifier.fillMaxWidth().height(34.dp)
                                    ) {
                                        Text("JOIN GAME NOW", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Text(
                                    text = message.text,
                                    color = TextPrimaryDark,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.align(Alignment.End),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = message.timestamp,
                                    fontSize = 10.sp,
                                    color = TextSecondaryDark
                                )
                                if (isSelf) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "✓✓",
                                        fontSize = 10.sp,
                                        color = EmeraldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Message Action Shortcuts: Reaction & Reply
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("❤️", "🔥", "😂").forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { viewModel.addReaction(message.id, emoji) }
                                    .padding(2.dp)
                            )
                        }
                        Text(
                            text = "Reply",
                            fontSize = 11.sp,
                            color = CyberTeal,
                            modifier = Modifier
                                .clickable { replyingToMessage = message }
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal BottomSheet / Dialog: Launch Game From Chat
    if (showGamePicker) {
        AlertDialog(
            onDismissRequest = { showGamePicker = false },
            title = {
                Text("🎮 Launch Game in Gang Chat", fontWeight = FontWeight.Bold, color = TextPrimaryDark)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select a game to create room and invite everyone in this chat:", color = TextSecondaryDark, fontSize = 13.sp)
                    GameType.values().forEach { gType ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showGamePicker = false
                                    viewModel.openCreateGame(gType)
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = gType.displayName, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                    Text(text = gType.iconDesc, color = TextSecondaryDark, fontSize = 11.sp)
                                }
                                Text("2-${gType.maxPlayers}P", color = CyberTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGamePicker = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = DarkSurface
        )
    }
}
