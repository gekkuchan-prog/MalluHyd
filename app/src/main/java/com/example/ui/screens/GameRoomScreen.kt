package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
fun GameRoomScreen(
    roomId: String,
    viewModel: GangViewModel,
    onBackClick: () -> Unit
) {
    val gameRooms by viewModel.gameRooms.collectAsState()
    val room = gameRooms.find { it.id == roomId } ?: gameRooms.firstOrNull()

    val isVoiceMuted by viewModel.isVoiceMuted.collectAsState()
    val isVoiceConnected by viewModel.isVoiceConnected.collectAsState()

    var inGameChatText by remember { mutableStateOf("") }
    var inGameMessages by remember {
        mutableStateOf(
            listOf(
                "Adithyan: Let's see who gets into the home safe zone first! 🎲",
                "Rahul: Watch out for my tokens, I'm right behind you!",
                "Sneha: Rolling for a 6!"
            )
        )
    }

    if (room == null) {
        Box(modifier = Modifier.fillMaxSize().background(DeepNavyBg), contentAlignment = Alignment.Center) {
            Text("Room not found", color = TextPrimaryDark)
        }
        return
    }

    Scaffold(
        topBar = {
            MalluGangHeader(
                title = "${room.gameType.displayName} • ${room.code}",
                subtitle = room.statusMessage,
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        containerColor = DeepNavyBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("game_room_screen_container")
        ) {
            // Live Voice Bar (Play While Talking Architecture)
            Surface(
                color = DarkSurface,
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isVoiceConnected) EmeraldPrimary else SoftError)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isVoiceConnected) "Clubhouse Voice: Connected 🎙️" else "Voice: Disconnected",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { viewModel.toggleVoiceMute() },
                            modifier = Modifier
                                .testTag("voice_mute_toggle_button")
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isVoiceMuted) SoftError.copy(alpha = 0.2f) else EmeraldContainer)
                        ) {
                            Icon(
                                imageVector = if (isVoiceMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mute Toggle",
                                tint = if (isVoiceMuted) SoftError else EmeraldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Interactive Game Arena Board
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(12.dp)
                    .testTag("interactive_game_board_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Turn & Status Banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status: ${room.status}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTeal
                        )
                        room.lastDiceRoll?.let { roll ->
                            Surface(
                                color = EmeraldPrimary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Dice: $roll 🎲",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Visual Game Board Render
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurface)
                            .border(2.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        when (room.gameType) {
                            GameType.LUDO -> LudoBoardVisual(players = room.players)
                            GameType.SNAKE_LADDERS -> SnakeBoardVisual(players = room.players)
                            GameType.CARROM -> CarromBoardVisual(players = room.players)
                            else -> GenericGameBoardVisual(room = room)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Player tokens / score list
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        room.players.forEach { p ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(p.colorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = p.name.take(1),
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    text = p.name.take(6),
                                    fontSize = 11.sp,
                                    color = TextPrimaryDark,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Step: ${p.position}",
                                    fontSize = 10.sp,
                                    color = CyberTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Roll Dice Primary Interactive Action Button
                    Button(
                        onClick = { viewModel.rollDice() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("roll_dice_action_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = "Roll Dice",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ROLL DICE 🎲",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // In-Game Concurrent Communication Section (Talk & Type while playing)
            Surface(
                color = DarkSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .testTag("in_game_communication_panel"),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💬 In-Room Chat & Banter",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTeal
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(inGameMessages) { chatText ->
                            Text(
                                text = chatText,
                                fontSize = 12.sp,
                                color = TextPrimaryDark
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inGameChatText,
                            onValueChange = { inGameChatText = it },
                            placeholder = { Text("Quick banter...", color = TextMutedDark, fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("in_game_chat_textfield"),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant,
                                focusedBorderColor = CyberTeal,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (inGameChatText.isNotBlank()) {
                                    inGameMessages = inGameMessages + "${viewModel.currentUser.value.nickname}: $inGameChatText"
                                    inGameChatText = ""
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CyberTeal)
                                .testTag("in_game_chat_send_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LudoBoardVisual(players: List<GamePlayer>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 4 Quad Bases
        drawRect(Color(0xFFE53935), topLeft = Offset(0f, 0f), size = Size(w * 0.4f, h * 0.4f)) // Red
        drawRect(Color(0xFF43A047), topLeft = Offset(w * 0.6f, 0f), size = Size(w * 0.4f, h * 0.4f)) // Green
        drawRect(Color(0xFFFDD835), topLeft = Offset(0f, h * 0.6f), size = Size(w * 0.4f, h * 0.4f)) // Yellow
        drawRect(Color(0xFF1E88E5), topLeft = Offset(w * 0.6f, h * 0.6f), size = Size(w * 0.4f, h * 0.4f)) // Blue

        // Center Home
        drawRect(Color(0xFF37474F), topLeft = Offset(w * 0.4f, h * 0.4f), size = Size(w * 0.2f, h * 0.2f))

        // Center token dots representing player positions
        players.forEachIndexed { i, p ->
            val angle = i * (Math.PI / 2.0)
            val cx = w * 0.5f + (Math.cos(angle) * (w * 0.18f)).toFloat()
            val cy = h * 0.5f + (Math.sin(angle) * (h * 0.18f)).toFloat()
            drawCircle(Color(p.colorHex), radius = 10f, center = Offset(cx, cy))
        }
    }
}

@Composable
private fun SnakeBoardVisual(players: List<GamePlayer>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cols = 5
        val rows = 5
        val cellW = w / cols
        val cellH = h / rows

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val color = if ((r + c) % 2 == 0) Color(0xFF263238) else Color(0xFF37474F)
                drawRect(color, topLeft = Offset(c * cellW, r * cellH), size = Size(cellW, cellH))
            }
        }

        // Draw Ladders (Green lines)
        drawLine(Color(0xFF00E676), start = Offset(cellW * 0.5f, cellH * 4.5f), end = Offset(cellW * 2.5f, cellH * 1.5f), strokeWidth = 5f)
        // Draw Snakes (Red lines)
        drawLine(Color(0xFFFF5252), start = Offset(cellW * 3.5f, cellH * 0.5f), end = Offset(cellW * 1.5f, cellH * 3.5f), strokeWidth = 5f)
    }
}

@Composable
private fun CarromBoardVisual(players: List<GamePlayer>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        // Carrom wooden rim
        drawRect(Color(0xFF8D6E63), topLeft = Offset(0f, 0f), size = Size(w, h))
        drawRect(Color(0xFFFFE082), topLeft = Offset(14f, 14f), size = Size(w - 28f, h - 28f))

        // 4 Pockets
        drawCircle(Color(0xFF212121), radius = 16f, center = Offset(24f, 24f))
        drawCircle(Color(0xFF212121), radius = 16f, center = Offset(w - 24f, 24f))
        drawCircle(Color(0xFF212121), radius = 16f, center = Offset(24f, h - 24f))
        drawCircle(Color(0xFF212121), radius = 16f, center = Offset(w - 24f, h - 24f))

        // Center Circle & Queen
        drawCircle(Color(0xFFD32F2F), radius = 24f, center = Offset(w / 2f, h / 2f))
        drawCircle(Color(0xFFFFD54F), radius = 10f, center = Offset(w / 2f, h / 2f))
    }
}

@Composable
private fun GenericGameBoardVisual(room: GameRoom) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(52.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(room.gameType.displayName, color = TextPrimaryDark, fontWeight = FontWeight.Bold)
        }
    }
}
