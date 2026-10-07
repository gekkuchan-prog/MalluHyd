package com.example.model

import com.google.firebase.Timestamp

/**
 * ClubhouseFirestoreEvent
 * Data model for social gaming meetups, trips, and tournaments stored in Cloud Firestore.
 * Requires default values on all properties for Firestore serialization/deserialization.
 */
data class ClubhouseFirestoreEvent(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val dateTime: String = "",
    val organizerId: String = "",
    val organizerName: String = "",
    val category: String = "GAMING",
    val gameType: String = "Ludo",
    val maxParticipants: Int = 4,
    val goingCount: Int = 1,
    val maybeCount: Int = 0,
    val createdAt: Timestamp? = null
)
