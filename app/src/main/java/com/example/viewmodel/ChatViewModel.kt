package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Message
import com.example.repository.ClubhouseFirestoreRepository
import com.example.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ChatUiState {
    object Loading : ChatUiState
    data class Success(val messages: List<Message>) : ChatUiState
    data class Error(val message: String) : ChatUiState
}

class ChatViewModel(
    private val firestoreRepository: ClubhouseFirestoreRepository,
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository(),
    private val conversationId: String = "group_main"
) : ViewModel() {

    // Current authenticated user session observation
    val currentAuthUser: StateFlow<FirebaseUser?> = authRepository.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = authRepository.currentUser
        )

    // Message list observed in real-time from Cloud Firestore
    val messagesState: StateFlow<ChatUiState> = firestoreRepository.observeMessages(conversationId)
        .map<List<Message>, ChatUiState> { msgList -> ChatUiState.Success(msgList) }
        .catch { error ->
            emit(ChatUiState.Error(error.localizedMessage ?: "Failed to load clubhouse messages"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ChatUiState.Loading
        )

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _sendError = MutableStateFlow<String?>(null)
    val sendError: StateFlow<String?> = _sendError.asStateFlow()

    /**
     * Sends a new chat message to Cloud Firestore.
     */
    fun sendMessage(text: String) {
        if (text.isBlank() || _isSending.value) return

        val user = currentAuthUser.value
        val senderName = user?.displayName ?: "Gang Member"

        viewModelScope.launch {
            _isSending.value = true
            _sendError.value = null

            val result = firestoreRepository.sendMessage(
                conversationId = conversationId,
                text = text.trim(),
                senderName = senderName
            )

            result.onFailure { error ->
                _sendError.value = error.localizedMessage ?: "Failed to send message"
            }
            _isSending.value = false
        }
    }

    fun clearSendError() {
        _sendError.value = null
    }
}
