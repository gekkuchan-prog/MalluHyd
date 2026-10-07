package com.example.model

enum class MemberActivityStatus(
    val label: String,
    val iconEmoji: String,
    val colorHex: Long,
    val description: String
) {
    ONLINE("Online", "🟢", 0xFF00E676, "Active in clubhouse"),
    IN_GAME("In-Game", "🎮", 0xFF00E5FF, "Playing Ludo / Carrom"),
    AWAY("Away", "🟡", 0xFFFFB703, "Stepped away for chai"),
    OFFLINE("Offline", "⚪", 0xFF64748B, "Last seen recently");

    companion object {
        fun fromString(value: String): MemberActivityStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: ONLINE
        }
    }
}
