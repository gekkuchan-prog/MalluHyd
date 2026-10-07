package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class ThemeViewModel(
    private val firestoreRepository: ClubhouseFirestoreRepository,
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = authRepository.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = authRepository.currentUser
        )

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    init {
        // Observe profile theme preferences if signed in
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    firestoreRepository.observeUserProfile(user.uid).collect { profile ->
                        if (profile != null) {
                            _isDarkTheme.value = profile.isDarkTheme
                        }
                    }
                }
            }
        }
    }

    /**
     * Toggles global theme between Light and Dark mode, saving the setting in Firestore.
     */
    fun toggleTheme() {
        val newSetting = !_isDarkTheme.value
        _isDarkTheme.value = newSetting

        viewModelScope.launch {
            firestoreRepository.updateThemePreference(newSetting)
        }
    }
}
