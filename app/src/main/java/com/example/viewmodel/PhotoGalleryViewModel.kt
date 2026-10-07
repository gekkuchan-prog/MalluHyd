package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ClubhousePhoto
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

sealed interface PhotoGalleryUiState {
    object Loading : PhotoGalleryUiState
    data class Success(val photos: List<ClubhousePhoto>) : PhotoGalleryUiState
    data class Error(val message: String) : PhotoGalleryUiState
}

class PhotoGalleryViewModel(
    private val firestoreRepository: ClubhouseFirestoreRepository,
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = authRepository.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = authRepository.currentUser
        )

    val galleryState: StateFlow<PhotoGalleryUiState> = firestoreRepository.observeClubhousePhotos()
        .map<List<ClubhousePhoto>, PhotoGalleryUiState> { photos ->
            PhotoGalleryUiState.Success(photos)
        }
        .catch { e ->
            emit(PhotoGalleryUiState.Error(e.localizedMessage ?: "Failed to load clubhouse gallery"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = PhotoGalleryUiState.Loading
        )

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError.asStateFlow()

    fun uploadPhoto(
        title: String,
        albumName: String,
        imageUrl: String,
        location: String,
        onSuccess: () -> Unit = {}
    ) {
        if (title.isBlank() || imageUrl.isBlank() || _isUploading.value) return

        val user = currentUser.value
        val name = user?.displayName ?: "Gang Member"

        viewModelScope.launch {
            _isUploading.value = true
            _uploadError.value = null

            val result = firestoreRepository.uploadPhoto(
                title = title,
                albumName = albumName,
                imageUrl = imageUrl,
                uploaderName = name,
                location = location
            )

            result.onSuccess {
                onSuccess()
            }.onFailure { e ->
                _uploadError.value = e.localizedMessage ?: "Failed to upload photo"
            }
            _isUploading.value = false
        }
    }

    fun toggleLike(photo: ClubhousePhoto) {
        viewModelScope.launch {
            firestoreRepository.togglePhotoLike(photo.id, photo)
        }
    }

    fun clearError() {
        _uploadError.value = null
    }
}
