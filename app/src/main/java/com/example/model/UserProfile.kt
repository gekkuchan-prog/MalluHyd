package com.example.model

import com.google.firebase.Timestamp

/**
 * UserProfile
 * Firestore-backed profile data model for Mallu Gang clubhouse members.
 * Requires default values on all properties for Firestore serialization/deserialization.
 */
data class UserProfile(
    val userId: String = "",
    val fullName: String = "",
    val nickname: String = "",
    val phoneNumber: String = "",
    val gender: String = "Unspecified",
    val dateOfBirth: String = "2005-01-01",
    val calculatedAge: Int = 21,
    val about: String = "",
    val avatarInitials: String = "MG",
    val avatarUri: String = "",
    val activityStatus: String = "ONLINE",
    val isDarkTheme: Boolean = true,
    val role: String = "MEMBER",
    val status: String = "ACTIVE",
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val joinedDate: String = "Oct 2026",
    val updatedAt: Timestamp? = null
)
