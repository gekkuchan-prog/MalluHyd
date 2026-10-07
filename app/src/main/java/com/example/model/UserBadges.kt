package com.example.model

import com.google.firebase.Timestamp

/**
 * UserBadgeItem
 * Individual badge status for a member.
 */
data class UserBadgeItem(
    val badgeId: String = "",
    val title: String = "",
    val description: String = "",
    val iconEmoji: String = "🏆",
    val category: String = "GAMING", // GAMING, COMMUNITY, CHAT, TRIPS
    val isUnlocked: Boolean = false,
    val progress: Int = 0,
    val maxProgress: Int = 10,
    val unlockedAt: Timestamp? = null
)

/**
 * UserBadges
 * Root badges document stored per member at `/users/{userId}/badges/summary` or `/badges/{userId}`.
 * Requires default values on all properties for Firestore serialization.
 */
data class UserBadges(
    val userId: String = "",
    val totalBadgesUnlocked: Int = 0,
    val gangPoints: Int = 0,
    val badges: List<UserBadgeItem> = emptyList(),
    val updatedAt: Timestamp? = null
)
