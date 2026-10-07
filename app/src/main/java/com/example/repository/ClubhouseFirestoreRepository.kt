package com.example.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.EventCategory
import com.example.model.GangEvent
import com.example.model.Message
import com.example.model.MessageStatus
import com.example.model.RsvpStatus
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * ClubhouseFirestoreRepository
 * Manages persistent Cloud Firestore operations for Mallu Gang chat conversations and community events.
 */
class ClubhouseFirestoreRepository(
    private val db: FirebaseFirestore
) {
    // Secondary constructor resolving the custom provisioned database ID
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    /**
     * Fail-fast assertion for the authenticated user ID.
     */
    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    /**
     * Realtime observation of messages in a given conversation.
     */
    fun observeMessages(conversationId: String): Flow<List<Message>> = flow {
        val path = "conversations/$conversationId/messages"
        emitAll(
            db.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        val id = doc.getString("id") ?: doc.id
                        val senderId = doc.getString("senderId") ?: return@mapNotNull null
                        val senderName = doc.getString("senderName") ?: "Member"
                        val text = doc.getString("text") ?: ""
                        val timestamp = doc.getString("timestamp") ?: "Recently"
                        Message(
                            id = id,
                            conversationId = conversationId,
                            senderId = senderId,
                            senderName = senderName,
                            text = text,
                            timestamp = timestamp,
                            status = MessageStatus.SENT
                        )
                    }
                }
                .catch { e ->
                    Log.e("ClubhouseRepo", "Error observing messages at $path", e)
                    throw e
                }
        )
    }

    /**
     * Sends a message to the specified conversation using server timestamp.
     */
    suspend fun sendMessage(
        conversationId: String,
        text: String,
        senderName: String
    ): Result<String> {
        return try {
            val uid = requireUserId()
            val messagesRef = db.collection("conversations")
                .document(conversationId)
                .collection("messages")

            val docRef = messagesRef.document()
            val payload = hashMapOf(
                "id" to docRef.id,
                "conversationId" to conversationId,
                "senderId" to uid,
                "senderName" to senderName,
                "text" to text,
                "timestamp" to "Just now",
                "createdAt" to FieldValue.serverTimestamp()
            )

            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("ClubhouseRepo", "Failed to send message", e)
            Result.failure(e)
        }
    }

    /**
     * Realtime observation of clubhouse events.
     */
    fun observeEvents(): Flow<List<GangEvent>> = flow {
        val path = "events"
        emitAll(
            db.collection("events")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        val id = doc.getString("id") ?: doc.id
                        val title = doc.getString("title") ?: return@mapNotNull null
                        val desc = doc.getString("description") ?: ""
                        val loc = doc.getString("location") ?: "Kerala"
                        val dateTime = doc.getString("dateTime") ?: "Upcoming"
                        val organizer = doc.getString("organizerName") ?: "Admin"
                        val categoryStr = doc.getString("category") ?: "MEETUP"
                        val going = (doc.getLong("goingCount") ?: 1L).toInt()
                        val cat = try { EventCategory.valueOf(categoryStr) } catch (_: Exception) { EventCategory.MEETUP }

                        GangEvent(
                            id = id,
                            title = title,
                            description = desc,
                            location = loc,
                            dateTime = dateTime,
                            organizerName = organizer,
                            category = cat,
                            goingCount = going,
                            maybeCount = 0,
                            userRsvp = RsvpStatus.UNANSWERED
                        )
                    }
                }
                .catch { e ->
                    Log.e("ClubhouseRepo", "Error observing events at $path", e)
                    throw e
                }
        )
    }

    /**
     * Realtime observation of typed ClubhouseFirestoreEvent objects.
     */
    fun observeClubhouseEvents(): Flow<List<com.example.model.ClubhouseFirestoreEvent>> = flow {
        val path = "events"
        emitAll(
            db.collection("events")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(com.example.model.ClubhouseFirestoreEvent::class.java)?.copy(id = doc.id)
                    }
                }
                .catch { e ->
                    Log.e("ClubhouseRepo", "Error observing clubhouse events at $path", e)
                    throw e
                }
        )
    }

    /**
     * Creates a new social gaming event in Cloud Firestore.
     */
    suspend fun createSocialGamingEvent(
        event: com.example.model.ClubhouseFirestoreEvent
    ): Result<String> {
        return try {
            val uid = requireUserId()
            val eventsRef = db.collection("events")
            val docRef = if (event.id.isNotBlank()) eventsRef.document(event.id) else eventsRef.document()

            val payload = hashMapOf(
                "id" to docRef.id,
                "title" to event.title,
                "description" to event.description,
                "location" to event.location,
                "dateTime" to event.dateTime,
                "organizerId" to uid,
                "organizerName" to event.organizerName,
                "category" to event.category,
                "gameType" to event.gameType,
                "maxParticipants" to event.maxParticipants,
                "goingCount" to event.goingCount.coerceAtLeast(1),
                "maybeCount" to event.maybeCount,
                "createdAt" to FieldValue.serverTimestamp()
            )

            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("ClubhouseRepo", "Failed to create social gaming event", e)
            Result.failure(e)
        }
    }

    /**
     * Publishes a new clubhouse event to Cloud Firestore.
     */
    suspend fun createEvent(
        title: String,
        description: String,
        location: String,
        dateTime: String,
        organizerName: String,
        category: EventCategory
    ): Result<String> {
        return try {
            val uid = requireUserId()
            val eventsRef = db.collection("events")
            val docRef = eventsRef.document()

            val payload = hashMapOf(
                "id" to docRef.id,
                "title" to title,
                "description" to description,
                "location" to location,
                "dateTime" to dateTime,
                "organizerId" to uid,
                "organizerName" to organizerName,
                "category" to category.name,
                "goingCount" to 1,
                "createdAt" to FieldValue.serverTimestamp()
            )

            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("ClubhouseRepo", "Failed to create event", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches a member's profile once from Cloud Firestore.
     */
    suspend fun getUserProfile(userId: String): Result<com.example.model.UserProfile?> {
        return try {
            val snapshot = db.collection("users").document(userId).get().await()
            val profile = snapshot.toObject(com.example.model.UserProfile::class.java)?.copy(userId = snapshot.id)
            Result.success(profile)
        } catch (e: Exception) {
            Log.e("ClubhouseRepo", "Failed to fetch user profile for $userId", e)
            Result.failure(e)
        }
    }

    /**
     * Realtime observation of a member's profile.
     */
    fun observeUserProfile(userId: String): Flow<com.example.model.UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users")
                .document(userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObject(com.example.model.UserProfile::class.java)?.copy(userId = snapshot.id)
                }
                .catch { e ->
                    Log.e("ClubhouseRepo", "Error observing profile at $path", e)
                    throw e
                }
        )
    }

    /**
     * Creates or updates a member's profile in Cloud Firestore.
     */
    suspend fun saveUserProfile(profile: com.example.model.UserProfile): Result<Unit> {
        return try {
            val uid = requireUserId()
            val userRef = db.collection("users").document(uid)

            val payload = hashMapOf(
                "userId" to uid,
                "fullName" to profile.fullName.trim(),
                "nickname" to profile.nickname.trim(),
                "phoneNumber" to profile.phoneNumber.trim(),
                "gender" to profile.gender,
                "dateOfBirth" to profile.dateOfBirth,
                "calculatedAge" to profile.calculatedAge,
                "about" to profile.about.trim(),
                "avatarInitials" to profile.avatarInitials,
                "avatarUri" to profile.avatarUri,
                "activityStatus" to profile.activityStatus,
                "isDarkTheme" to profile.isDarkTheme,
                "role" to profile.role,
                "status" to profile.status,
                "gamesPlayed" to profile.gamesPlayed,
                "gamesWon" to profile.gamesWon,
                "joinedDate" to profile.joinedDate,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            userRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ClubhouseRepo", "Failed to save user profile", e)
            Result.failure(e)
        }
    }

    /**
     * Persists the user's light/dark Material 3 theme preference in Cloud Firestore.
     */
    suspend fun updateThemePreference(isDarkTheme: Boolean): Result<Unit> {
        return try {
            val uid = requireUserId()
            val userRef = db.collection("users").document(uid)
            val updates = hashMapOf<String, Any>(
                "isDarkTheme" to isDarkTheme,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ClubhouseRepo", "Failed to update theme preference", e)
            Result.failure(e)
        }
    }

    /**
     * Updates the member's live activity presence status in Cloud Firestore (ONLINE, IN_GAME, AWAY, OFFLINE).
     */
    suspend fun updateActivityStatus(status: com.example.model.MemberActivityStatus): Result<Unit> {
        return try {
            val uid = requireUserId()
            val userRef = db.collection("users").document(uid)
            val updates = hashMapOf<String, Any>(
                "activityStatus" to status.name,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ClubhouseRepo", "Failed to update activity status", e)
            Result.failure(e)
        }
    }

    /**
     * Updates only the member's profile avatar URI in Cloud Firestore.
     */
    suspend fun updateAvatarUri(avatarUri: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            val userRef = db.collection("users").document(uid)
            val updates = hashMapOf<String, Any>(
                "avatarUri" to avatarUri,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ClubhouseRepo", "Failed to update avatarUri", e)
            Result.failure(e)
        }
    }

    /**
     * Realtime observation of a member's unlocked badges and achievement progress.
     */
    fun observeUserBadges(userId: String): Flow<com.example.model.UserBadges> = flow {
        val path = "users/$userId/badges/summary"
        emitAll(
            db.collection("users")
                .document(userId)
                .collection("badges")
                .document("summary")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObject(com.example.model.UserBadges::class.java)?.copy(userId = userId)
                        ?: defaultInitialBadges(userId)
                }
                .catch { e ->
                    Log.e("ClubhouseRepo", "Error observing badges at $path", e)
                    emit(defaultInitialBadges(userId))
                }
        )
    }

    /**
     * Saves or syncs badge achievements to Cloud Firestore.
     */
    suspend fun saveUserBadges(badges: com.example.model.UserBadges): Result<Unit> {
        return try {
            val uid = requireUserId()
            val badgeRef = db.collection("users")
                .document(uid)
                .collection("badges")
                .document("summary")

            val payload = hashMapOf(
                "userId" to uid,
                "totalBadgesUnlocked" to badges.badges.count { it.isUnlocked },
                "gangPoints" to badges.gangPoints,
                "badges" to badges.badges.map { b ->
                    hashMapOf(
                        "badgeId" to b.badgeId,
                        "title" to b.title,
                        "description" to b.description,
                        "iconEmoji" to b.iconEmoji,
                        "category" to b.category,
                        "isUnlocked" to b.isUnlocked,
                        "progress" to b.progress,
                        "maxProgress" to b.maxProgress
                    )
                },
                "updatedAt" to FieldValue.serverTimestamp()
            )

            badgeRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ClubhouseRepo", "Failed to sync badges", e)
            Result.failure(e)
        }
    }

    private fun defaultInitialBadges(userId: String): com.example.model.UserBadges {
        return com.example.model.UserBadges(
            userId = userId,
            totalBadgesUnlocked = 3,
            gangPoints = 450,
            badges = listOf(
                com.example.model.UserBadgeItem("b_ludo", "Ludo Champion", "Won 10 Ludo matches in the Clubhouse", "🎲", "GAMING", true, 10, 10),
                com.example.model.UserBadgeItem("b_carrom", "Queen Striker", "Pocketed the Carrom queen 5 times", "🎯", "GAMING", true, 5, 5),
                com.example.model.UserBadgeItem("b_founder", "OG Gang Member", "Joined Mallu Gang in early launch", "🌴", "COMMUNITY", true, 1, 1),
                com.example.model.UserBadgeItem("b_host", "Clubhouse Host", "Organize 3 group gaming tournaments", "👑", "COMMUNITY", false, 2, 3),
                com.example.model.UserBadgeItem("b_racer", "Speed Demon", "Win first place in Nitro Highway Race", "🏎️", "GAMING", false, 0, 1),
                com.example.model.UserBadgeItem("b_traveler", "Wayanad Explorer", "RSVP and attend 2 gang road trips", "🚗", "TRIPS", false, 1, 2)
            )
        )
    }

    /**
     * Realtime observation of a multiplayer TicTacToe match.
     */
    fun observeTicTacToeMatch(matchId: String): Flow<com.example.model.TicTacToeMatch?> = flow {
        val path = "matches/$matchId"
        emitAll(
            db.collection("matches")
                .document(matchId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObject(com.example.model.TicTacToeMatch::class.java)?.copy(matchId = snapshot.id)
                }
                .catch { e ->
                    Log.e("ClubhouseRepo", "Error observing match at $path", e)
                    throw e
                }
        )
    }

    /**
     * Creates a new match room in Cloud Firestore.
     */
    suspend fun createTicTacToeMatch(hostName: String): Result<String> {
        return try {
            val uid = requireUserId()
            val matchRef = db.collection("matches").document()
            val payload = hashMapOf(
                "matchId" to matchRef.id,
                "playerXId" to uid,
                "playerXName" to hostName,
                "playerOId" to "",
                "playerOName" to "Waiting for opponent...",
                "board" to listOf("", "", "", "", "", "", "", "", ""),
                "currentTurn" to "X",
                "status" to "WAITING",
                "winnerId" to "",
                "updatedAt" to FieldValue.serverTimestamp()
            )
            matchRef.set(payload).await()
            Result.success(matchRef.id)
        } catch (e: Exception) {
            Log.e("ClubhouseRepo", "Failed to create match", e)
            Result.failure(e)
        }
    }

    /**
     * Joins an existing match as Player O.
     */
    suspend fun joinTicTacToeMatch(matchId: String, opponentName: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            val matchRef = db.collection("matches").document(matchId)
            val updates = hashMapOf<String, Any>(
                "playerOId" to uid,
                "playerOName" to opponentName,
                "status" to "IN_PROGRESS",
                "updatedAt" to FieldValue.serverTimestamp()
            )
            matchRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ClubhouseRepo", "Failed to join match", e)
            Result.failure(e)
        }
    }

    /**
     * Executes a move at cellIndex (0-8) and checks for win or draw.
     */
    suspend fun playTicTacToeMove(matchId: String, currentMatch: com.example.model.TicTacToeMatch, cellIndex: Int): Result<Unit> {
        return try {
            val uid = requireUserId()
            val matchRef = db.collection("matches").document(matchId)

            val isX = uid == currentMatch.playerXId
            val isO = uid == currentMatch.playerOId
            val expectedTurn = currentMatch.currentTurn

            if ((isX && expectedTurn != "X") || (isO && expectedTurn != "O")) {
                return Result.failure(IllegalStateException("Not your turn"))
            }

            val symbol = if (isX) "X" else "O"
            if (currentMatch.board[cellIndex].isNotEmpty()) {
                return Result.failure(IllegalStateException("Cell already taken"))
            }

            val newBoard = currentMatch.board.toMutableList()
            newBoard[cellIndex] = symbol

            // Check winning combinations
            val wins = listOf(
                listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
                listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
                listOf(0, 4, 8), listOf(2, 4, 6)
            )

            var newStatus = "IN_PROGRESS"
            var winnerId = ""

            val hasWon = wins.any { combo ->
                newBoard[combo[0]] == symbol && newBoard[combo[1]] == symbol && newBoard[combo[2]] == symbol
            }

            if (hasWon) {
                newStatus = if (symbol == "X") "X_WON" else "O_WON"
                winnerId = uid
            } else if (newBoard.none { it.isEmpty() }) {
                newStatus = "DRAW"
            }

            val nextTurn = if (symbol == "X") "O" else "X"

            val updates = hashMapOf(
                "board" to newBoard,
                "currentTurn" to nextTurn,
                "status" to newStatus,
                "winnerId" to winnerId,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            matchRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ClubhouseRepo", "Failed to play move", e)
            Result.failure(e)
        }
    }

    /**
     * Realtime observation of shared photos in the Clubhouse Gallery.
     */
    fun observeClubhousePhotos(): Flow<List<com.example.model.ClubhousePhoto>> = flow {
        val path = "photos"
        emitAll(
            db.collection("photos")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(com.example.model.ClubhousePhoto::class.java)?.copy(id = doc.id)
                    }.ifEmpty { defaultInitialPhotos() }
                }
                .catch { e ->
                    Log.e("ClubhouseRepo", "Error observing photos at $path", e)
                    emit(defaultInitialPhotos())
                }
        )
    }

    /**
     * Uploads a new photo reference to Cloud Firestore.
     */
    suspend fun uploadPhoto(
        title: String,
        albumName: String,
        imageUrl: String,
        uploaderName: String,
        location: String
    ): Result<String> {
        return try {
            val uid = requireUserId()
            val photosRef = db.collection("photos")
            val docRef = photosRef.document()

            val payload = hashMapOf(
                "id" to docRef.id,
                "title" to title.trim(),
                "albumName" to albumName.trim(),
                "imageUrl" to imageUrl.trim(),
                "uploadedById" to uid,
                "uploaderName" to uploaderName.trim(),
                "location" to location.trim(),
                "likesCount" to 0,
                "likedByUids" to emptyList<String>(),
                "createdAt" to FieldValue.serverTimestamp()
            )

            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("ClubhouseRepo", "Failed to upload photo", e)
            Result.failure(e)
        }
    }

    /**
     * Likes or unlikes a photo in the shared gallery.
     */
    suspend fun togglePhotoLike(photoId: String, currentPhoto: com.example.model.ClubhousePhoto): Result<Unit> {
        return try {
            val uid = requireUserId()
            val photoRef = db.collection("photos").document(photoId)
            val alreadyLiked = currentPhoto.likedByUids.contains(uid)

            val updatedLikesList = if (alreadyLiked) {
                currentPhoto.likedByUids - uid
            } else {
                currentPhoto.likedByUids + uid
            }

            val updates = hashMapOf<String, Any>(
                "likesCount" to updatedLikesList.size,
                "likedByUids" to updatedLikesList
            )

            photoRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ClubhouseRepo", "Failed to toggle photo like", e)
            Result.failure(e)
        }
    }

    private fun defaultInitialPhotos(): List<com.example.model.ClubhousePhoto> {
        return listOf(
            com.example.model.ClubhousePhoto(
                id = "photo_1",
                title = "Chembra Peak Sunrise Trek",
                albumName = "Wayanad 2026",
                imageUrl = "https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?auto=format&fit=crop&w=600&q=80",
                uploaderName = "Sneha Menon",
                likesCount = 5,
                location = "Meppadi, Wayanad"
            ),
            com.example.model.ClubhousePhoto(
                id = "photo_2",
                title = "Campfire Jamming & Guitar",
                albumName = "Wayanad 2026",
                imageUrl = "https://images.unsplash.com/photo-1510312305653-8ed496efae75?auto=format&fit=crop&w=600&q=80",
                uploaderName = "Arjun Vijayan",
                likesCount = 4,
                location = "Tent Camp, Wayanad"
            ),
            com.example.model.ClubhousePhoto(
                id = "photo_3",
                title = "Sunset at Vagamon Pine Forest",
                albumName = "Road Trips",
                imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=600&q=80",
                uploaderName = "Rahul Krishna",
                likesCount = 6,
                location = "Vagamon, Idukki"
            )
        )
    }
}
