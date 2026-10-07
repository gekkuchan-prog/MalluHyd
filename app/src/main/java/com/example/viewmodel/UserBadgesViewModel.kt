package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.UserBadgeItem
import com.example.model.UserBadges
import com.example.repository.ClubhouseFirestoreRepository
import com.example.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserBadgesViewModel(
    private val firestoreRepository: ClubhouseFirestoreRepository,
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = authRepository.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = authRepository.currentUser
        )

    private val targetUserId: String
        get() = currentUser.value?.uid ?: "user_default"

    val userBadgesState: StateFlow<UserBadges> = firestoreRepository.observeUserBadges(targetUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UserBadges(userId = targetUserId)
        )

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    /**
     * Increments badge progress or unlocks an achievement in Cloud Firestore.
     */
    fun progressBadge(badgeId: String) {
        val current = userBadgesState.value
        val updatedList = current.badges.map { b ->
            if (b.badgeId == badgeId) {
                val newProgress = (b.progress + 1).coerceAtMost(b.maxProgress)
                val unlocked = newProgress >= b.maxProgress
                b.copy(progress = newProgress, isUnlocked = unlocked)
            } else b
        }
        val newPoints = current.gangPoints + 50
        val updatedBadges = current.copy(
            badges = updatedList,
            totalBadgesUnlocked = updatedList.count { it.isUnlocked },
            gangPoints = newPoints
        )

        viewModelScope.launch {
            _isSyncing.value = true
            firestoreRepository.saveUserBadges(updatedBadges)
            _isSyncing.value = false
        }
    }
}
