package com.example.model

import com.google.firebase.Timestamp

/**
 * ClubhousePhoto
 * Data model for shared memory photos uploaded by clubhouse members.
 * Requires default values on all properties for Firestore serialization.
 */
data class ClubhousePhoto(
    val id: String = "",
    val title: String = "",
    val albumName: String = "Wayanad 2026", // Wayanad 2026, Goa Sunsets, Clubhouse Hangouts
    val imageUrl: String = "",
    val uploadedById: String = "",
    val uploaderName: String = "Gang Member",
    val likesCount: Int = 0,
    val likedByUids: List<String> = emptyList(),
    val location: String = "Kerala",
    val createdAt: Timestamp? = null
)
