package com.example.model

enum class UserRole(val title: String) {
    SUPER_ADMIN("Super Admin (Owner)"),
    ADMIN_1("Admin 1"),
    ADMIN_2("Admin 2"),
    MEMBER("Gang Member")
}

enum class UserStatus {
    ACTIVE,
    SUSPENDED,
    INVITED
}

data class User(
    val id: String,
    val phoneNumber: String,
    val fullName: String,
    val nickname: String,
    val gender: String,
    val dateOfBirth: String, // YYYY-MM-DD
    val calculatedAge: Int,
    val about: String,
    val avatarInitials: String,
    val role: UserRole,
    val status: UserStatus = UserStatus.ACTIVE,
    val isOnline: Boolean = false,
    val lastSeenText: String = "Just now",
    val joinedDate: String = "Oct 2026",
    val gamesPlayed: Int = 12,
    val gamesWon: Int = 5
)

enum class MessageType {
    TEXT,
    VOICE,
    IMAGE,
    GAME_INVITE,
    EXPENSE_CARD,
    EVENT_CARD
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: String,
    val type: MessageType = MessageType.TEXT,
    val status: MessageStatus = MessageStatus.SENT,
    val isPinned: Boolean = false,
    val replyToText: String? = null,
    val replyToSender: String? = null,
    val reactions: Map<String, String> = emptyMap(), // userId -> emoji (e.g. "❤️", "🔥", "😂")
    val attachmentUrl: String? = null,
    val gamePayload: GameInvitePayload? = null
)

data class GameInvitePayload(
    val gameType: String,
    val roomCode: String,
    val maxPlayers: Int
)

data class ChatConversation(
    val id: String,
    val name: String,
    val isGroup: Boolean,
    val memberCount: Int = 2,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val avatarColorHex: Long = 0xFF00E676,
    val pinned: Boolean = false,
    val typingStatus: String? = null
)

enum class GameType(val displayName: String, val minPlayers: Int, val maxPlayers: Int, val iconDesc: String) {
    LUDO("Ludo Master", 2, 4, "Classic 4-color board"),
    SNAKE_LADDERS("Snakes & Ladders", 2, 4, "Multiplayer dice board"),
    CARROM("Carrom Strike", 2, 4, "Finger-flick pocket board"),
    CAR_RACE("Nitro Highway Race", 2, 4, "High-speed lane racer"),
    TIC_TAC_TOE("Tic-Tac-Toe Pro", 2, 2, "Quick mini-game")
}

data class GamePlayer(
    val userId: String,
    val name: String,
    val score: Int = 0,
    val position: Int = 0,
    val colorHex: Long = 0xFFFFB703,
    val isTurn: Boolean = false,
    val isWinner: Boolean = false,
    val isMuted: Boolean = false,
    val isSpeaking: Boolean = false
)

data class GameRoom(
    val id: String,
    val code: String,
    val gameType: GameType,
    val hostId: String,
    val hostName: String,
    val players: List<GamePlayer>,
    val maxPlayers: Int,
    val status: String, // "WAITING", "PLAYING", "FINISHED"
    val currentTurnIndex: Int = 0,
    val lastDiceRoll: Int? = null,
    val statusMessage: String = "Waiting for players to join...",
    val isVoiceActive: Boolean = true
)

enum class EventCategory {
    TRIP,
    BIRTHDAY,
    MEETUP,
    MOVIE,
    GAMING,
    PARTY
}

enum class RsvpStatus {
    GOING,
    MAYBE,
    NOT_GOING,
    UNANSWERED
}

data class GangEvent(
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val dateTime: String,
    val organizerName: String,
    val category: EventCategory,
    val goingCount: Int,
    val maybeCount: Int,
    val userRsvp: RsvpStatus = RsvpStatus.UNANSWERED
)

data class PollOption(
    val id: String,
    val text: String,
    val votes: Int,
    val percentage: Float = 0f
)

data class GangPoll(
    val id: String,
    val question: String,
    val creatorName: String,
    val isMultipleChoice: Boolean = false,
    val isAnonymous: Boolean = false,
    val totalVotes: Int = 0,
    val options: List<PollOption>,
    val userVotedOptionId: String? = null,
    val closesInText: String = "Closing in 2 days"
)

data class ExpenseSplitItem(
    val userName: String,
    val shareAmount: Double,
    val isSettled: Boolean
)

data class GangExpense(
    val id: String,
    val title: String,
    val totalAmount: Double,
    val paidByName: String,
    val dateText: String,
    val participants: List<ExpenseSplitItem>
)

enum class ComplaintCategory(val label: String) {
    BEHAVIOUR("Behaviour"),
    HARASSMENT("Harassment"),
    MONEY("Money & Expenses"),
    GROUP_CONFLICT("Group Conflict"),
    PRIVACY("Privacy Breach"),
    SPAM("Spam / Flooding"),
    RULE_VIOLATION("Gang Rule Violation"),
    OTHER("Other Issue")
}

enum class ComplaintStatus(val label: String) {
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    RESPONSE_REQUESTED("Response Requested"),
    RESOLVED("Resolved"),
    DISMISSED("Dismissed")
}

data class Complaint(
    val id: String,
    val trackingCode: String, // e.g., MG-024
    val complainantId: String,
    val complainantName: String,
    val accusedName: String,
    val category: ComplaintCategory,
    val description: String,
    val priority: String = "Normal",
    val dateFormatted: String,
    val status: ComplaintStatus = ComplaintStatus.SUBMITTED,
    val adminNotes: String = "",
    val accusedResponse: String = ""
)

data class AuditLog(
    val id: String,
    val actorName: String,
    val actionText: String,
    val timestamp: String,
    val categoryBadge: String
)

data class GangRule(
    val number: Int,
    val title: String,
    val description: String
)
