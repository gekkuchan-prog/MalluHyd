package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.TicTacToeMatch
import com.example.repository.ClubhouseFirestoreRepository
import com.example.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TicTacToeViewModel(
    private val firestoreRepository: ClubhouseFirestoreRepository,
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = authRepository.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = authRepository.currentUser
        )

    private val _activeMatchId = MutableStateFlow<String?>(null)
    val activeMatchId: StateFlow<String?> = _activeMatchId.asStateFlow()

    val currentMatch: StateFlow<TicTacToeMatch?> = _activeMatchId
        .flatMapLatest { id ->
            if (id != null) firestoreRepository.observeTicTacToeMatch(id) else flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _matchError = MutableStateFlow<String?>(null)
    val matchError: StateFlow<String?> = _matchError.asStateFlow()

    /**
     * Creates a new match room in Firestore and sets active match ID.
     */
    fun hostMatch(hostName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _matchError.value = null
            val result = firestoreRepository.createTicTacToeMatch(hostName)
            result.onSuccess { matchId ->
                _activeMatchId.value = matchId
            }.onFailure { e ->
                _matchError.value = e.localizedMessage ?: "Failed to create match"
            }
            _isLoading.value = false
        }
    }

    /**
     * Joins an existing match as player O.
     */
    fun joinMatch(matchId: String, playerName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _matchError.value = null
            val result = firestoreRepository.joinTicTacToeMatch(matchId, playerName)
            result.onSuccess {
                _activeMatchId.value = matchId
            }.onFailure { e ->
                _matchError.value = e.localizedMessage ?: "Failed to join match"
            }
            _isLoading.value = false
        }
    }

    /**
     * Plays a cell move (0 to 8).
     */
    fun makeMove(cellIndex: Int) {
        val match = currentMatch.value ?: return
        val matchId = _activeMatchId.value ?: return

        viewModelScope.launch {
            val result = firestoreRepository.playTicTacToeMove(matchId, match, cellIndex)
            result.onFailure { e ->
                _matchError.value = e.localizedMessage ?: "Move rejected"
            }
        }
    }

    fun leaveMatch() {
        _activeMatchId.value = null
        _matchError.value = null
    }

    fun clearError() {
        _matchError.value = null
    }
}
