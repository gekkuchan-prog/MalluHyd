package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import com.example.repository.GangRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class MainTab(val title: String) {
    HOME("Home"),
    CHATS("Chats"),
    GAMES("Games"),
    COMMUNITY("Clubhouse"),
    ADMIN("Admin")
}

class GangViewModel(
    val repository: GangRepository = GangRepository()
) : ViewModel() {

    // Global Navigation State
    private val _selectedTab = MutableStateFlow(MainTab.HOME)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    // Active sub-screen: null = root tab, or "chat_detail", "game_room", "submit_complaint", "profile", "add_member", "rules"
    private val _activeSubScreen = MutableStateFlow<String?>(null)
    val activeSubScreen: StateFlow<String?> = _activeSubScreen.asStateFlow()

    // Sub-screen arguments
    private val _activeConversationId = MutableStateFlow<String?>("group_main")
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _activeGameRoomId = MutableStateFlow<String?>("room_ludo_1")
    val activeGameRoomId: StateFlow<String?> = _activeGameRoomId.asStateFlow()

    // Search query for global / screen search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Voice in-room controls
    private val _isVoiceMuted = MutableStateFlow(false)
    val isVoiceMuted: StateFlow<Boolean> = _isVoiceMuted.asStateFlow()

    private val _isVoiceConnected = MutableStateFlow(true)
    val isVoiceConnected: StateFlow<Boolean> = _isVoiceConnected.asStateFlow()

    // Expose repository state
    val currentUser = repository.currentUser
    val members = repository.members
    val conversations = repository.conversations
    val messages = repository.messages
    val gameRooms = repository.gameRooms
    val events = repository.events
    val polls = repository.polls
    val expenses = repository.expenses
    val complaints = repository.complaints
    val auditLogs = repository.auditLogs
    val gangRules = repository.gangRules

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
        _activeSubScreen.value = null
    }

    fun openChat(conversationId: String) {
        _activeConversationId.value = conversationId
        _activeSubScreen.value = "chat_detail"
    }

    fun openGameRoom(roomId: String) {
        _activeGameRoomId.value = roomId
        _activeSubScreen.value = "game_room"
    }

    fun openCreateGame(gameType: GameType) {
        val room = repository.createGameRoom(gameType)
        _activeGameRoomId.value = room.id
        _activeSubScreen.value = "game_room"
    }

    fun navigateTo(subScreen: String) {
        _activeSubScreen.value = subScreen
    }

    fun navigateBack() {
        _activeSubScreen.value = null
    }

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun toggleVoiceMute() {
        _isVoiceMuted.value = !_isVoiceMuted.value
    }

    fun toggleVoiceRoom() {
        _isVoiceConnected.value = !_isVoiceConnected.value
    }

    fun sendMessage(text: String, replyTo: Message? = null) {
        val convId = _activeConversationId.value ?: "group_main"
        if (text.isNotBlank()) {
            repository.sendMessage(convId, text, replyTo)
        }
    }

    fun addReaction(messageId: String, emoji: String) {
        val convId = _activeConversationId.value ?: "group_main"
        repository.addReaction(convId, messageId, emoji)
    }

    fun rollDice() {
        val roomId = _activeGameRoomId.value ?: return
        repository.rollDiceInGame(roomId)
    }

    fun updateRsvp(eventId: String, status: RsvpStatus) {
        repository.updateRsvp(eventId, status)
    }

    fun votePoll(pollId: String, optionId: String) {
        repository.votePoll(pollId, optionId)
    }

    fun submitComplaint(accusedName: String, category: ComplaintCategory, description: String) {
        repository.submitComplaint(accusedName, category, description)
        _activeSubScreen.value = null
    }

    fun updateComplaintStatus(complaintId: String, status: ComplaintStatus, note: String) {
        repository.updateComplaintStatus(complaintId, status, note)
    }

    fun addMember(phone: String, fullName: String, nickname: String): Boolean {
        return repository.addMemberByPhone(phone, fullName, nickname)
    }

    fun toggleMemberStatus(userId: String) {
        repository.toggleMemberStatus(userId)
    }

    fun addExpense(title: String, amount: Double, paidBy: String, participants: List<String>) {
        repository.addExpense(title, amount, paidBy, participants)
    }

    fun toggleExpenseSettlement(expenseId: String, participantName: String) {
        repository.toggleExpenseSettlement(expenseId, participantName)
    }

    fun appointAdminRole(userId: String, role: UserRole) {
        repository.appointAdminRole(userId, role)
    }

    fun switchProfile(user: User) {
        repository.switchActiveProfile(user)
    }
}
