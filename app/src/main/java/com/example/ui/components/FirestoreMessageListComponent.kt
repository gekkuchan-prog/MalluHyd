package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.model.Message
import com.example.ui.theme.*
import com.example.viewmodel.ChatUiState
import com.example.viewmodel.ChatViewModel

@Composable
fun FirestoreMessageListComponent(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val chatState by viewModel.messagesState.collectAsState()
    val currentUser by viewModel.currentAuthUser.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val sendError by viewModel.sendError.collectAsState()

    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBg)
            .testTag("firestore_chat_container")
    ) {
        // Message List Display Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (val state = chatState) {
                is ChatUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = EmeraldPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                is ChatUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "⚠️ Connection Error",
                                    fontWeight = FontWeight.Bold,
                                    color = SoftError,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = state.message,
                                    color = TextSecondaryDark,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
                is ChatUiState.Success -> {
                    if (state.messages.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "💬 No messages yet. Be the first to say Namaskaram!",
                                color = TextSecondaryDark,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("firestore_messages_lazy_list"),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.messages, key = { it.id }) { message ->
                                val isSelf = message.senderId == currentUser?.uid
                                FirestoreMessageBubble(message = message, isSelf = isSelf)
                            }
                        }
                    }
                }
            }
        }

        // Send Error Banner
        sendError?.let { err ->
            Surface(
                color = SoftError.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = err, color = SoftError, fontSize = 12.sp)
                    TextButton(onClick = { viewModel.clearSendError() }) {
                        Text("Dismiss", color = SoftError, fontSize = 11.sp)
                    }
                }
            }
        }

        // Message Input Composer Bar
        Surface(
            color = DarkSurface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Clubhouse message...", color = TextMutedDark, fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("firestore_chat_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank() && !isSending) {
                            viewModel.sendMessage(textInput)
                            textInput = ""
                        }
                    },
                    enabled = textInput.isNotBlank() && !isSending,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (textInput.isNotBlank() && !isSending) EmeraldPrimary else DarkSurfaceVariant)
                        .testTag("firestore_chat_send_btn")
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (textInput.isNotBlank()) Color.Black else TextMutedDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FirestoreMessageBubble(
    message: Message,
    isSelf: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isSelf) 16.dp else 4.dp,
                bottomEnd = if (isSelf) 4.dp else 16.dp
            ),
            color = if (isSelf) EmeraldContainer else DarkSurfaceVariant,
            modifier = Modifier
                .widthIn(max = 290.dp)
                .testTag("message_bubble_${message.id}")
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

                Text(
                    text = message.text,
                    color = TextPrimaryDark,
                    fontSize = 14.sp
                )

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
    }
}
