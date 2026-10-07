package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.model.TicTacToeMatch
import com.example.ui.theme.*
import com.example.viewmodel.TicTacToeViewModel

@Composable
fun RealtimeTicTacToeComponent(
    viewModel: TicTacToeViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val match by viewModel.currentMatch.collectAsState()
    val activeMatchId by viewModel.activeMatchId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.matchError.collectAsState()

    var joinRoomCodeInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("tictactoe_component_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Game Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "⭕ Tic-Tac-Toe Pro ❌",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Text(
                    text = "Live Firestore match state synchronization",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )
            }

            if (match != null) {
                IconButton(onClick = { viewModel.leaveMatch() }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Leave Match", tint = SoftError)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Error message banner
        errorMsg?.let { err ->
            Surface(
                color = SoftError.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
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

        if (match == null) {
            // Lobby / Create or Join Match UI
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Multiplayer Lobby", fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 14.sp)
                    Text("Host a live match or paste a Match ID to play with a brother/sister:", color = TextSecondaryDark, fontSize = 12.sp)

                    Button(
                        onClick = {
                            val name = currentUser?.displayName ?: "Adhi"
                            viewModel.hostMatch(name)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("host_match_btn"),
                        enabled = !isLoading
                    ) {
                        Text("HOST NEW MATCH (PLAYER X)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = joinRoomCodeInput,
                            onValueChange = { joinRoomCodeInput = it },
                            placeholder = { Text("Match Document ID...") },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface,
                                focusedTextColor = TextPrimaryDark
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (joinRoomCodeInput.isNotBlank()) {
                                    val name = currentUser?.displayName ?: "Opponent"
                                    viewModel.joinMatch(joinRoomCodeInput.trim(), name)
                                }
                            },
                            enabled = joinRoomCodeInput.isNotBlank() && !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                            modifier = Modifier.height(50.dp)
                        ) {
                            Text("JOIN (O)", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Live Match Active State
            val active = match!!
            val myUid = currentUser?.uid ?: ""
            val isMyTurn = (active.currentTurn == "X" && myUid == active.playerXId) ||
                    (active.currentTurn == "O" && myUid == active.playerOId)

            // Match Status & Players Bar
            Surface(
                color = DarkSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "❌ ${active.playerXName}",
                            color = if (active.currentTurn == "X") EmeraldPrimary else TextSecondaryDark,
                            fontWeight = if (active.currentTurn == "X") FontWeight.Black else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "⭕ ${active.playerOName}",
                            color = if (active.currentTurn == "O") CyberTeal else TextSecondaryDark,
                            fontWeight = if (active.currentTurn == "O") FontWeight.Black else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val statusBanner = when (active.status) {
                        "WAITING" -> "⏳ Waiting for opponent to join..."
                        "IN_PROGRESS" -> if (isMyTurn) "🎯 YOUR TURN (${active.currentTurn})! Tap an empty square." else "⏳ Opponent's turn (${active.currentTurn})..."
                        "X_WON" -> "🎉 Player X Won the Match!"
                        "O_WON" -> "🎉 Player O Won the Match!"
                        "DRAW" -> "🤝 Draw Match! Great defense."
                        else -> active.status
                    }

                    Text(
                        text = statusBanner,
                        color = when (active.status) {
                            "X_WON", "O_WON" -> WarmGoldAccent
                            else -> if (isMyTurn) EmeraldPrimary else TextSecondaryDark
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3x3 Interactive Real-time Board
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DeepNavyBg)
                    .padding(8.dp)
                    .testTag("tictactoe_board_box")
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(9) { index ->
                        val cellValue = active.board.getOrElse(index) { "" }
                        val canPlay = active.status == "IN_PROGRESS" && isMyTurn && cellValue.isEmpty()

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceVariant)
                                .border(
                                    width = 1.dp,
                                    color = if (canPlay) EmeraldPrimary.copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable(enabled = canPlay) {
                                    viewModel.makeMove(index)
                                }
                                .testTag("cell_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cellValue,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = when (cellValue) {
                                    "X" -> EmeraldPrimary
                                    "O" -> CyberTeal
                                    else -> Color.Transparent
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Match ID: ${active.matchId}",
                fontSize = 10.sp,
                color = TextMutedDark
            )
        }
    }
}
