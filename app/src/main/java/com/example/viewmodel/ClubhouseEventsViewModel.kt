package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ClubhouseFirestoreEvent
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

sealed interface EventsUiState {
    object Loading : EventsUiState
    data class Success(val events: List<ClubhouseFirestoreEvent>) : EventsUiState
    data class Error(val message: String) : EventsUiState
}

class ClubhouseEventsViewModel(
    private val firestoreRepository: ClubhouseFirestoreRepository,
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    // Current user observation
    val currentAuthUser: StateFlow<FirebaseUser?> = authRepository.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = authRepository.currentUser
        )

    // Realtime stream of social gaming events from Cloud Firestore
    val eventsUiState: StateFlow<EventsUiState> = firestoreRepository.observeClubhouseEvents()
        .map<List<ClubhouseFirestoreEvent>, EventsUiState> { eventList ->
            EventsUiState.Success(eventList)
        }
        .catch { error ->
            emit(EventsUiState.Error(error.localizedMessage ?: "Failed to load events"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = EventsUiState.Loading
        )

    private val _isCreatingEvent = MutableStateFlow(false)
    val isCreatingEvent: StateFlow<Boolean> = _isCreatingEvent.asStateFlow()

    private val _operationError = MutableStateFlow<String?>(null)
    val operationError: StateFlow<String?> = _operationError.asStateFlow()

    /**
     * Publishes a new social gaming tournament / meetup to Firestore.
     */
    fun scheduleGamingEvent(
        title: String,
        description: String,
        location: String,
        dateTime: String,
        gameType: String = "Ludo Master",
        maxParticipants: Int = 4,
        onSuccess: (String) -> Unit = {}
    ) {
        if (title.isBlank() || _isCreatingEvent.value) return

        val user = currentAuthUser.value
        val organizerName = user?.displayName ?: "Gang Member"

        viewModelScope.launch {
            _isCreatingEvent.value = true
            _operationError.value = null

            val newEvent = ClubhouseFirestoreEvent(
                title = title.trim(),
                description = description.trim(),
                location = location.trim(),
                dateTime = dateTime.trim(),
                organizerName = organizerName,
                category = "GAMING",
                gameType = gameType,
                maxParticipants = maxParticipants,
                goingCount = 1
            )

            val result = firestoreRepository.createSocialGamingEvent(newEvent)
            result.onSuccess { eventId ->
                onSuccess(eventId)
            }.onFailure { error ->
                _operationError.value = error.localizedMessage ?: "Failed to schedule event"
            }
            _isCreatingEvent.value = false
        }
    }

    fun clearError() {
        _operationError.value = null
    }
}
