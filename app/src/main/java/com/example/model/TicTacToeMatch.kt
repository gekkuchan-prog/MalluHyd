package com.example.model

import com.google.firebase.Timestamp

/**
 * TicTacToeMatch
 * Data model for a real-time multiplayer Tic-Tac-Toe match in Cloud Firestore.
 * Requires default values on all properties for Firestore serialization.
 */
data class TicTacToeMatch(
    val matchId: String = "",
    val playerXId: String = "",
    val playerXName: String = "Player X",
    val playerOId: String = "",
    val playerOName: String = "Waiting for opponent...",
    val board: List<String> = listOf("", "", "", "", "", "", "", "", ""), // 9 cells: "", "X", "O"
    val currentTurn: String = "X", // "X" or "O"
    val status: String = "WAITING", // "WAITING", "IN_PROGRESS", "X_WON", "O_WON", "DRAW"
    val winnerId: String = "",
    val updatedAt: Timestamp? = null
)
