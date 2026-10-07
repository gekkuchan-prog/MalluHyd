package com.example.model

/**
 * VoicePeerState
 * Tracks the live voice presence and microphone state of a connected member.
 */
data class VoicePeerState(
    val userId: String,
    val name: String,
    val avatarInitials: String,
    val isSpeaking: Boolean = false,
    val isMuted: Boolean = false,
    val pingMs: Int = 28
)
