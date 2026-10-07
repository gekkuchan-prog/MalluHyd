package com.example.repository

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class GangRepository {
    // Current Active Authenticated User
    private val _currentUser = MutableStateFlow(
        User(
            id = "user_adhi",
            phoneNumber = "+91 98765 43210",
            fullName = "Adithyan M S",
            nickname = "Adhi (Chief)",
            gender = "Male",
            dateOfBirth = "2005-01-24",
            calculatedAge = 21,
            about = "Gang founder & tech head. Let's conquer the leaderboard! 🚀",
            avatarInitials = "AM",
            role = UserRole.SUPER_ADMIN,
            status = UserStatus.ACTIVE,
            isOnline = true,
            lastSeenText = "Online",
            joinedDate = "Oct 2026",
            gamesPlayed = 42,
            gamesWon = 26
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // Gang Members List
    private val _members = MutableStateFlow<List<User>>(
        listOf(
            User(
                id = "user_adhi",
                phoneNumber = "+91 98765 43210",
                fullName = "Adithyan M S",
                nickname = "Adhi (Chief)",
                gender = "Male",
                dateOfBirth = "2005-01-24",
                calculatedAge = 21,
                about = "Gang founder & tech head. Let's conquer the leaderboard! 🚀",
                avatarInitials = "AM",
                role = UserRole.SUPER_ADMIN,
                status = UserStatus.ACTIVE,
                isOnline = true,
                gamesPlayed = 42,
                gamesWon = 26
            ),
            User(
                id = "user_rahul",
                phoneNumber = "+91 94471 23456",
                fullName = "Rahul Krishna",
                nickname = "Kichu",
                gender = "Male",
                dateOfBirth = "2004-06-15",
                calculatedAge = 22,
                about = "Ludo king of Mallu Gang. Challenge me anytime! 🎲",
                avatarInitials = "RK",
                role = UserRole.ADMIN_1,
                status = UserStatus.ACTIVE,
                isOnline = true,
                gamesPlayed = 38,
                gamesWon = 21
            ),
            User(
                id = "user_sneha",
                phoneNumber = "+91 98950 11223",
                fullName = "Sneha Menon",
                nickname = "Sneh",
                gender = "Female",
                dateOfBirth = "2005-03-12",
                calculatedAge = 21,
                about = "Trip organizer and carrom champ. Kerala road trips incoming! 🌴",
                avatarInitials = "SM",
                role = UserRole.ADMIN_2,
                status = UserStatus.ACTIVE,
                isOnline = false,
                lastSeenText = "15m ago",
                gamesPlayed = 25,
                gamesWon = 14
            ),
            User(
                id = "user_arjun",
                phoneNumber = "+91 97440 99887",
                fullName = "Arjun Vijayan",
                nickname = "Appu",
                gender = "Male",
                dateOfBirth = "2004-11-28",
                calculatedAge = 21,
                about = "Always up for night chai and highway drives ☕🚗",
                avatarInitials = "AV",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE,
                isOnline = true,
                gamesPlayed = 30,
                gamesWon = 10
            ),
            User(
                id = "user_diya",
                phoneNumber = "+91 94000 55443",
                fullName = "Diya Parvathi",
                nickname = "Diya",
                gender = "Female",
                dateOfBirth = "2005-08-04",
                calculatedAge = 21,
                about = "Photographer of the gang! Check out the Goa memories 📸",
                avatarInitials = "DP",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE,
                isOnline = false,
                lastSeenText = "2h ago",
                gamesPlayed = 18,
                gamesWon = 7
            ),
            User(
                id = "user_vishnu",
                phoneNumber = "+91 96333 44112",
                fullName = "Vishnu Prasad",
                nickname = "VP",
                gender = "Male",
                dateOfBirth = "2004-02-19",
                calculatedAge = 22,
                about = "Budget auditor & bill settlement officer 😂",
                avatarInitials = "VP",
                role = UserRole.MEMBER,
                status = UserStatus.ACTIVE,
                isOnline = false,
                lastSeenText = "Yesterday",
                gamesPlayed = 15,
                gamesWon = 4
            )
        )
    )
    val members: StateFlow<List<User>> = _members.asStateFlow()

    // Conversations List
    private val _conversations = MutableStateFlow<List<ChatConversation>>(
        listOf(
            ChatConversation(
                id = "group_main",
                name = "🌴 Mallu Gang Official",
                isGroup = true,
                memberCount = 6,
                lastMessage = "Adhi: 🎲 Ludo room created! Who is ready to roll?",
                lastMessageTime = "10:14 PM",
                unreadCount = 2,
                isOnline = true,
                avatarColorHex = 0xFF00E676,
                pinned = true
            ),
            ChatConversation(
                id = "group_gaming",
                name = "🎮 Gaming Arena & Tournaments",
                isGroup = true,
                memberCount = 5,
                lastMessage = "Kichu: Carrom rematch tonight at 11 PM sharp!",
                lastMessageTime = "09:45 PM",
                unreadCount = 0,
                isOnline = true,
                avatarColorHex = 0xFF00E5FF,
                pinned = true
            ),
            ChatConversation(
                id = "direct_rahul",
                name = "Rahul Krishna (Kichu)",
                isGroup = false,
                memberCount = 2,
                lastMessage = "Did you check the Araku valley trip poll?",
                lastMessageTime = "08:30 PM",
                unreadCount = 0,
                isOnline = true,
                avatarColorHex = 0xFFFFB703
            ),
            ChatConversation(
                id = "direct_sneha",
                name = "Sneha Menon",
                isGroup = false,
                memberCount = 2,
                lastMessage = "Settled my share for the fuel expense! 💸",
                lastMessageTime = "Yesterday",
                unreadCount = 0,
                isOnline = false,
                avatarColorHex = 0xFFFB8500
            ),
            ChatConversation(
                id = "group_trips",
                name = "🚗 Road Trips & Meetups",
                isGroup = true,
                memberCount = 6,
                lastMessage = "Diya: Added 14 new photos to Munnar 2026 album",
                lastMessageTime = "Yesterday",
                unreadCount = 0,
                isOnline = false,
                avatarColorHex = 0xFF9C27B0
            )
        )
    )
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    // Message History per conversation
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(
        mapOf(
            "group_main" to listOf(
                Message(
                    id = "msg_1",
                    conversationId = "group_main",
                    senderId = "user_rahul",
                    senderName = "Rahul Krishna",
                    text = "Machane! Everyone free for a weekend hangout?",
                    timestamp = "10:02 PM",
                    status = MessageStatus.READ,
                    reactions = mapOf("user_adhi" to "🔥")
                ),
                Message(
                    id = "msg_2",
                    conversationId = "group_main",
                    senderId = "user_sneha",
                    senderName = "Sneha Menon",
                    text = "Yes! Check the new Event in the Community tab.",
                    timestamp = "10:05 PM",
                    status = MessageStatus.READ
                ),
                Message(
                    id = "msg_3",
                    conversationId = "group_main",
                    senderId = "user_arjun",
                    senderName = "Arjun Vijayan",
                    text = "First let's settle the gaming score. Ludo or Carrom?",
                    timestamp = "10:09 PM",
                    status = MessageStatus.READ,
                    replyToText = "Machane! Everyone free for a weekend hangout?",
                    replyToSender = "Rahul Krishna"
                ),
                Message(
                    id = "msg_4",
                    conversationId = "group_main",
                    senderId = "user_adhi",
                    senderName = "Adithyan M S",
                    text = "🎮 Created a 4-Player Ludo Master Room. Tap below to jump in!",
                    timestamp = "10:14 PM",
                    type = MessageType.GAME_INVITE,
                    status = MessageStatus.SENT,
                    gamePayload = GameInvitePayload(
                        gameType = "LUDO",
                        roomCode = "MG-LUDO",
                        maxPlayers = 4
                    )
                )
            ),
            "group_gaming" to listOf(
                Message(
                    id = "msg_g1",
                    conversationId = "group_gaming",
                    senderId = "user_rahul",
                    senderName = "Rahul Krishna",
                    text = "Carrom rematch tonight at 11 PM sharp!",
                    timestamp = "09:45 PM",
                    status = MessageStatus.READ
                )
            ),
            "direct_rahul" to listOf(
                Message(
                    id = "msg_d1",
                    conversationId = "direct_rahul",
                    senderId = "user_rahul",
                    senderName = "Rahul Krishna",
                    text = "Did you check the Araku valley trip poll?",
                    timestamp = "08:30 PM",
                    status = MessageStatus.READ
                )
            )
        )
    )
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    // Active Game Rooms
    private val _gameRooms = MutableStateFlow<List<GameRoom>>(
        listOf(
            GameRoom(
                id = "room_ludo_1",
                code = "MG-LUDO",
                gameType = GameType.LUDO,
                hostId = "user_adhi",
                hostName = "Adithyan M S",
                players = listOf(
                    GamePlayer(userId = "user_adhi", name = "Adithyan (Host)", position = 14, colorHex = 0xFF00E676, isTurn = true, isSpeaking = true),
                    GamePlayer(userId = "user_rahul", name = "Rahul K", position = 8, colorHex = 0xFFFFB703),
                    GamePlayer(userId = "user_arjun", name = "Arjun V", position = 4, colorHex = 0xFF00E5FF),
                    GamePlayer(userId = "user_sneha", name = "Sneha M", position = 0, colorHex = 0xFFFF5252)
                ),
                maxPlayers = 4,
                status = "PLAYING",
                lastDiceRoll = 6,
                statusMessage = "Adithyan rolled a 6! Tap token to advance."
            ),
            GameRoom(
                id = "room_snakes_1",
                code = "MG-SNAKE",
                gameType = GameType.SNAKE_LADDERS,
                hostId = "user_rahul",
                hostName = "Rahul Krishna",
                players = listOf(
                    GamePlayer(userId = "user_rahul", name = "Rahul", position = 42, colorHex = 0xFFFFB703, isTurn = true),
                    GamePlayer(userId = "user_adhi", name = "Adithyan", position = 36, colorHex = 0xFF00E676)
                ),
                maxPlayers = 4,
                status = "PLAYING",
                lastDiceRoll = 4,
                statusMessage = "Rahul's turn to roll the dice!"
            ),
            GameRoom(
                id = "room_carrom_1",
                code = "MG-STRIKE",
                gameType = GameType.CARROM,
                hostId = "user_sneha",
                hostName = "Sneha Menon",
                players = listOf(
                    GamePlayer(userId = "user_sneha", name = "Sneha", score = 12, colorHex = 0xFFFF5252, isTurn = true),
                    GamePlayer(userId = "user_diya", name = "Diya", score = 9, colorHex = 0xFF00E5FF)
                ),
                maxPlayers = 2,
                status = "PLAYING",
                statusMessage = "Sneha's turn: Aim striker at queen"
            )
        )
    )
    val gameRooms: StateFlow<List<GameRoom>> = _gameRooms.asStateFlow()

    // Community Events
    private val _events = MutableStateFlow<List<GangEvent>>(
        listOf(
            GangEvent(
                id = "ev_1",
                title = "🌴 Wayanad Camping & Night Trek",
                description = "Tent camping near Chembra peak, barbecue, stargazing and campfire guitar session.",
                location = "Meppadi, Wayanad",
                dateTime = "Sat, 24 Oct 2026 • 04:00 PM",
                organizerName = "Adithyan M S",
                category = EventCategory.TRIP,
                goingCount = 5,
                maybeCount = 1,
                userRsvp = RsvpStatus.GOING
            ),
            GangEvent(
                id = "ev_2",
                title = "🎂 Kichu's Birthday Biryani Party",
                description = "Authentic Thalassery dum biryani feast + gaming tournament at Kichu's home.",
                location = "Kaloor, Kochi",
                dateTime = "Sun, 01 Nov 2026 • 01:00 PM",
                organizerName = "Rahul Krishna",
                category = EventCategory.BIRTHDAY,
                goingCount = 6,
                maybeCount = 0,
                userRsvp = RsvpStatus.GOING
            )
        )
    )
    val events: StateFlow<List<GangEvent>> = _events.asStateFlow()

    // Polls
    private val _polls = MutableStateFlow<List<GangPoll>>(
        listOf(
            GangPoll(
                id = "poll_1",
                question = "Where should our year-end mega road trip be?",
                creatorName = "Sneha Menon",
                isMultipleChoice = false,
                isAnonymous = false,
                totalVotes = 6,
                options = listOf(
                    PollOption("opt_1", "Goa Coastal Cruise", 3, 50f),
                    PollOption("opt_2", "Araku Valley & Vizag", 2, 33.3f),
                    PollOption("opt_3", "Vagamon Pines & Off-road", 1, 16.7f)
                ),
                userVotedOptionId = "opt_1"
            ),
            GangPoll(
                id = "poll_2",
                question = "Next weekend clubhouse tournament game?",
                creatorName = "Adithyan M S",
                isMultipleChoice = false,
                isAnonymous = false,
                totalVotes = 5,
                options = listOf(
                    PollOption("opt_g1", "Ludo Master Championship", 3, 60f),
                    PollOption("opt_g2", "Carrom Doubles Strike", 2, 40f)
                ),
                userVotedOptionId = "opt_g1"
            )
        )
    )
    val polls: StateFlow<List<GangPoll>> = _polls.asStateFlow()

    // Group Expenses
    private val _expenses = MutableStateFlow<List<GangExpense>>(
        listOf(
            GangExpense(
                id = "exp_1",
                title = "Wayanad Villa Advance & Fuel",
                totalAmount = 6000.0,
                paidByName = "Adithyan M S",
                dateText = "05 Oct 2026",
                participants = listOf(
                    ExpenseSplitItem("Adithyan M S", 1000.0, true),
                    ExpenseSplitItem("Rahul Krishna", 1000.0, true),
                    ExpenseSplitItem("Sneha Menon", 1000.0, true),
                    ExpenseSplitItem("Arjun Vijayan", 1000.0, false),
                    ExpenseSplitItem("Diya Parvathi", 1000.0, true),
                    ExpenseSplitItem("Vishnu Prasad", 1000.0, false)
                )
            ),
            GangExpense(
                id = "exp_2",
                title = "Clubhouse Snacks & Coffee",
                totalAmount = 900.0,
                paidByName = "Rahul Krishna",
                dateText = "02 Oct 2026",
                participants = listOf(
                    ExpenseSplitItem("Adithyan M S", 150.0, true),
                    ExpenseSplitItem("Rahul Krishna", 150.0, true),
                    ExpenseSplitItem("Sneha Menon", 150.0, true),
                    ExpenseSplitItem("Arjun Vijayan", 150.0, true),
                    ExpenseSplitItem("Diya Parvathi", 150.0, true),
                    ExpenseSplitItem("Vishnu Prasad", 150.0, true)
                )
            )
        )
    )
    val expenses: StateFlow<List<GangExpense>> = _expenses.asStateFlow()

    // Private Complaint Box
    private val _complaints = MutableStateFlow<List<Complaint>>(
        listOf(
            Complaint(
                id = "cmp_1",
                trackingCode = "MG-024",
                complainantId = "user_arjun",
                complainantName = "Arjun Vijayan",
                accusedName = "Vishnu Prasad",
                category = ComplaintCategory.MONEY,
                description = "Pending fuel share from the Munnar drive (₹500) has not been settled after 3 reminders.",
                priority = "Medium",
                dateFormatted = "04 Oct 2026",
                status = ComplaintStatus.RESPONSE_REQUESTED,
                adminNotes = "Admin 1 (Rahul) notified Vishnu to verify UPI transaction history.",
                accusedResponse = "Will clear by Friday once salary is credited."
            ),
            Complaint(
                id = "cmp_2",
                trackingCode = "MG-025",
                complainantId = "user_diya",
                complainantName = "Diya Parvathi",
                accusedName = "Arjun Vijayan",
                category = ComplaintCategory.BEHAVIOUR,
                description = "Rude language used during competitive Ludo match on Tuesday night.",
                priority = "Low",
                dateFormatted = "02 Oct 2026",
                status = ComplaintStatus.RESOLVED,
                adminNotes = "Super Admin mediated in clubhouse voice room. Arjun apologized and both shook hands.",
                accusedResponse = "Apologized for getting too heated in the game heat."
            )
        )
    )
    val complaints: StateFlow<List<Complaint>> = _complaints.asStateFlow()

    // Admin Audit Logs
    private val _auditLogs = MutableStateFlow<List<AuditLog>>(
        listOf(
            AuditLog("log_1", "Adithyan M S (Super Admin)", "Created game room MG-LUDO (4 players)", "10:14 PM", "GAMES"),
            AuditLog("log_2", "Rahul Krishna (Admin 1)", "Requested response on Complaint MG-024", "04 Oct 11:45 AM", "MODERATION"),
            AuditLog("log_3", "Adithyan M S (Super Admin)", "Resolved Complaint MG-025 following mutual apology", "03 Oct 09:12 PM", "MODERATION"),
            AuditLog("log_4", "Sneha Menon (Admin 2)", "Published official Road Trip Poll", "01 Oct 03:20 PM", "COMMUNITY"),
            AuditLog("log_5", "Adithyan M S (Super Admin)", "Appointed Sneha Menon as Admin 2", "28 Sep 06:00 PM", "ADMINISTRATION")
        )
    )
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    // Gang Rules
    private val _gangRules = MutableStateFlow<List<GangRule>>(
        listOf(
            GangRule(1, "Brotherhood & Mutual Respect", "Treat every brother and sister of the gang with dignity. No personal bullying or derogatory remarks."),
            GangRule(2, "Strict Vault Confidentiality", "What happens in Mallu Gang stays in Mallu Gang. Never screenshot or leak private discussions or photos outside."),
            GangRule(3, "Game Integrity & Fair Play", "No ghosting mid-match, rage quitting without notice, or unfair collusion in tournament games."),
            GangRule(4, "Prompt Financial Settlement", "Clear group expenses within 7 days of trip completion. Respect each other's money."),
            GangRule(5, "Constructive Grievance Redressal", "Use the Private Complaint Box for disputes. Admins guarantee impartial resolution.")
        )
    )
    val gangRules: StateFlow<List<GangRule>> = _gangRules.asStateFlow()

    // --- Actions ---

    fun sendMessage(conversationId: String, text: String, replyTo: Message? = null) {
        val current = _messages.value[conversationId] ?: emptyList()
        val user = _currentUser.value
        val newMsg = Message(
            id = "msg_" + System.currentTimeMillis(),
            conversationId = conversationId,
            senderId = user.id,
            senderName = user.fullName,
            text = text,
            timestamp = "Just now",
            status = MessageStatus.SENT,
            replyToText = replyTo?.text,
            replyToSender = replyTo?.senderName
        )
        val updatedMap = _messages.value.toMutableMap()
        updatedMap[conversationId] = current + newMsg
        _messages.value = updatedMap

        // Update conversation preview
        val convos = _conversations.value.map { conv ->
            if (conv.id == conversationId) {
                conv.copy(
                    lastMessage = "${user.nickname}: $text",
                    lastMessageTime = "Just now"
                )
            } else conv
        }
        _conversations.value = convos
    }

    fun addReaction(conversationId: String, messageId: String, emoji: String) {
        val currentList = _messages.value[conversationId] ?: return
        val currentUserId = _currentUser.value.id
        val updatedList = currentList.map { msg ->
            if (msg.id == messageId) {
                val newReactions = msg.reactions.toMutableMap()
                if (newReactions[currentUserId] == emoji) {
                    newReactions.remove(currentUserId)
                } else {
                    newReactions[currentUserId] = emoji
                }
                msg.copy(reactions = newReactions)
            } else msg
        }
        val updatedMap = _messages.value.toMutableMap()
        updatedMap[conversationId] = updatedList
        _messages.value = updatedMap
    }

    fun createGameRoom(gameType: GameType): GameRoom {
        val user = _currentUser.value
        val code = "MG-${gameType.name.take(4)}-${(100..999).random()}"
        val newRoom = GameRoom(
            id = "room_" + System.currentTimeMillis(),
            code = code,
            gameType = gameType,
            hostId = user.id,
            hostName = user.fullName,
            players = listOf(
                GamePlayer(userId = user.id, name = user.nickname, position = 0, colorHex = 0xFF00E676, isTurn = true)
            ),
            maxPlayers = gameType.maxPlayers,
            status = "WAITING",
            statusMessage = "Waiting for gang members to join ${gameType.displayName}..."
        )
        _gameRooms.value = listOf(newRoom) + _gameRooms.value

        // Post Game invite in Main Group
        val inviteMsg = Message(
            id = "msg_inv_" + System.currentTimeMillis(),
            conversationId = "group_main",
            senderId = user.id,
            senderName = user.fullName,
            text = "🎮 ${user.nickname} created a ${gameType.displayName} room! Tap below to join.",
            timestamp = "Just now",
            type = MessageType.GAME_INVITE,
            status = MessageStatus.SENT,
            gamePayload = GameInvitePayload(
                gameType = gameType.name,
                roomCode = code,
                maxPlayers = gameType.maxPlayers
            )
        )
        val currentMainMsgs = _messages.value["group_main"] ?: emptyList()
        val updatedMap = _messages.value.toMutableMap()
        updatedMap["group_main"] = currentMainMsgs + inviteMsg
        _messages.value = updatedMap

        recordAuditLog(user.fullName, "Created ${gameType.displayName} room ($code)", "GAMES")
        return newRoom
    }

    fun rollDiceInGame(roomId: String) {
        val rooms = _gameRooms.value.toMutableList()
        val index = rooms.indexOfFirst { it.id == roomId }
        if (index != -1) {
            val room = rooms[index]
            val diceRoll = (1..6).random()
            val currentTurn = room.currentTurnIndex
            val player = room.players.getOrNull(currentTurn)

            if (player != null) {
                val newPos = (player.position + diceRoll).coerceAtMost(56) // 56 standard Ludo goal
                val isWin = newPos >= 56
                val updatedPlayers = room.players.mapIndexed { idx, p ->
                    if (idx == currentTurn) {
                        p.copy(position = newPos, isWinner = isWin)
                    } else p
                }
                val nextTurn = if (diceRoll == 6 || isWin) currentTurn else (currentTurn + 1) % room.players.size
                val updatedRoom = room.copy(
                    lastDiceRoll = diceRoll,
                    currentTurnIndex = nextTurn,
                    players = updatedPlayers,
                    status = if (isWin) "COMPLETED" else "PLAYING",
                    statusMessage = if (isWin) "${player.name} WON THE MATCH! 🎉" else "${player.name} rolled a $diceRoll! Moved to step $newPos."
                )
                rooms[index] = updatedRoom
                _gameRooms.value = rooms
            }
        }
    }

    fun updateRsvp(eventId: String, newRsvp: RsvpStatus) {
        val updated = _events.value.map { ev ->
            if (ev.id == eventId) {
                var going = ev.goingCount
                var maybe = ev.maybeCount
                if (ev.userRsvp == RsvpStatus.GOING) going--
                if (ev.userRsvp == RsvpStatus.MAYBE) maybe--

                if (newRsvp == RsvpStatus.GOING) going++
                if (newRsvp == RsvpStatus.MAYBE) maybe++

                ev.copy(
                    userRsvp = newRsvp,
                    goingCount = going.coerceAtLeast(0),
                    maybeCount = maybe.coerceAtLeast(0)
                )
            } else ev
        }
        _events.value = updated
    }

    fun votePoll(pollId: String, optionId: String) {
        val updated = _polls.value.map { poll ->
            if (poll.id == pollId) {
                val newTotal = poll.totalVotes + 1
                val newOptions = poll.options.map { opt ->
                    val votes = if (opt.id == optionId) opt.votes + 1 else opt.votes
                    opt.copy(votes = votes, percentage = (votes.toFloat() / newTotal) * 100f)
                }
                poll.copy(
                    totalVotes = newTotal,
                    options = newOptions,
                    userVotedOptionId = optionId
                )
            } else poll
        }
        _polls.value = updated
    }

    fun submitComplaint(
        accusedName: String,
        category: ComplaintCategory,
        description: String
    ): Complaint {
        val user = _currentUser.value
        val tracking = "MG-0${(_complaints.value.size + 26)}"
        val newComplaint = Complaint(
            id = "cmp_" + System.currentTimeMillis(),
            trackingCode = tracking,
            complainantId = user.id,
            complainantName = user.fullName,
            accusedName = accusedName,
            category = category,
            description = description,
            priority = "Normal",
            dateFormatted = "Today",
            status = ComplaintStatus.SUBMITTED
        )
        _complaints.value = listOf(newComplaint) + _complaints.value
        recordAuditLog(user.fullName, "Submitted confidential complaint $tracking", "MODERATION")
        return newComplaint
    }

    fun updateComplaintStatus(complaintId: String, newStatus: ComplaintStatus, adminNote: String) {
        val admin = _currentUser.value
        val updated = _complaints.value.map { cmp ->
            if (cmp.id == complaintId) {
                cmp.copy(
                    status = newStatus,
                    adminNotes = if (adminNote.isNotBlank()) adminNote else cmp.adminNotes
                )
            } else cmp
        }
        _complaints.value = updated
        recordAuditLog(admin.fullName, "Updated Complaint status to ${newStatus.label}", "MODERATION")
    }

    fun addMemberByPhone(phone: String, fullName: String, nickname: String): Boolean {
        val admin = _currentUser.value
        // Only Super Admin or Admin can add
        if (admin.role != UserRole.SUPER_ADMIN && admin.role != UserRole.ADMIN_1 && admin.role != UserRole.ADMIN_2) {
            return false
        }
        val newUser = User(
            id = "user_" + UUID.randomUUID().toString().take(6),
            phoneNumber = phone,
            fullName = fullName,
            nickname = nickname,
            gender = "Unspecified",
            dateOfBirth = "2005-01-01",
            calculatedAge = 21,
            about = "New member invited to Mallu Gang.",
            avatarInitials = fullName.take(2).uppercase(),
            role = UserRole.MEMBER,
            status = UserStatus.ACTIVE,
            isOnline = false,
            lastSeenText = "Joined today"
        )
        _members.value = _members.value + newUser
        recordAuditLog(admin.fullName, "Invited new member: $fullName ($phone)", "MEMBERSHIP")
        return true
    }

    fun toggleMemberStatus(userId: String) {
        val admin = _currentUser.value
        if (admin.role != UserRole.SUPER_ADMIN && admin.role != UserRole.ADMIN_1) return

        val updated = _members.value.map { m ->
            if (m.id == userId) {
                val newStatus = if (m.status == UserStatus.ACTIVE) UserStatus.SUSPENDED else UserStatus.ACTIVE
                recordAuditLog(admin.fullName, "${if (newStatus == UserStatus.SUSPENDED) "Suspended" else "Reactivated"} member ${m.fullName}", "ADMINISTRATION")
                m.copy(status = newStatus)
            } else m
        }
        _members.value = updated
    }

    fun appointAdminRole(userId: String, newRole: UserRole) {
        val current = _currentUser.value
        if (current.role != UserRole.SUPER_ADMIN) return // Only Super Admin can appoint
        if (userId == current.id) return // Cannot alter own ownership casually

        val updated = _members.value.map { m ->
            if (m.id == userId) {
                recordAuditLog(current.fullName, "Appointed ${m.fullName} as ${newRole.title}", "ADMINISTRATION")
                m.copy(role = newRole)
            } else m
        }
        _members.value = updated
    }

    fun addExpense(title: String, totalAmount: Double, paidByName: String, participantNames: List<String>): GangExpense {
        val share = if (participantNames.isNotEmpty()) totalAmount / participantNames.size else totalAmount
        val participants = participantNames.map { name ->
            ExpenseSplitItem(
                userName = name,
                shareAmount = share,
                isSettled = name == paidByName
            )
        }
        val newExpense = GangExpense(
            id = "exp_" + System.currentTimeMillis(),
            title = title,
            totalAmount = totalAmount,
            paidByName = paidByName,
            dateText = "Today",
            participants = participants
        )
        _expenses.value = listOf(newExpense) + _expenses.value
        recordAuditLog(paidByName, "Added group expense: $title (₹${totalAmount.toInt()})", "EXPENSES")
        return newExpense
    }

    fun toggleExpenseSettlement(expenseId: String, participantName: String) {
        val updated = _expenses.value.map { exp ->
            if (exp.id == expenseId) {
                val updatedParticipants = exp.participants.map { part ->
                    if (part.userName == participantName) {
                        part.copy(isSettled = !part.isSettled)
                    } else part
                }
                exp.copy(participants = updatedParticipants)
            } else exp
        }
        _expenses.value = updated
    }

    fun switchActiveProfile(user: User) {
        _currentUser.value = user
    }

    private fun recordAuditLog(actor: String, action: String, category: String) {
        val log = AuditLog(
            id = "log_" + System.currentTimeMillis(),
            actorName = actor,
            actionText = action,
            timestamp = "Just now",
            categoryBadge = category
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }
}
