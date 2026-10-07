package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClubhousePhoto
import com.example.ui.theme.*
import com.example.viewmodel.PhotoGalleryUiState
import com.example.viewmodel.PhotoGalleryViewModel

@Composable
fun SharedPhotoGalleryComponent(
    viewModel: PhotoGalleryViewModel,
    modifier: Modifier = Modifier
) {
    val galleryState by viewModel.galleryState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val uploadError by viewModel.uploadError.collectAsState()

    var showUploadDialog by remember { mutableStateOf(false) }
    var selectedPhotoDetail by remember { mutableStateOf<ClubhousePhoto?>(null) }
    var activeAlbumFilter by remember { mutableStateOf("ALL") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBg)
            .testTag("shared_photo_gallery_container")
    ) {
        // Gallery Header with Upload Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "📸 Clubhouse Photo Vault",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Text(
                    text = "Shared memories from trips, gaming & meetups",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )
            }

            Button(
                onClick = { showUploadDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("upload_photo_btn")
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Photo", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Album Pills Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "Wayanad 2026", "Road Trips", "Clubhouse").forEach { albumTag ->
                val isSelected = activeAlbumFilter == albumTag
                FilterChip(
                    selected = isSelected,
                    onClick = { activeAlbumFilter = albumTag },
                    label = { Text(albumTag, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color.Black,
                        containerColor = DarkSurfaceVariant,
                        labelColor = TextSecondaryDark
                    )
                )
            }
        }

        // Error message banner
        uploadError?.let { err ->
            Surface(
                color = SoftError.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = err, color = SoftError, fontSize = 12.sp)
                    IconButton(onClick = { viewModel.clearError() }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = SoftError)
                    }
                }
            }
        }

        // Gallery Grid View
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (val state = galleryState) {
                is PhotoGalleryUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EmeraldPrimary, modifier = Modifier.size(36.dp))
                    }
                }
                is PhotoGalleryUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Card(colors = CardDefaults.cardColors(containerColor = DarkSurface)) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⚠️ Gallery Error", fontWeight = FontWeight.Bold, color = SoftError, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(state.message, color = TextSecondaryDark, fontSize = 12.sp)
                            }
                        }
                    }
                }
                is PhotoGalleryUiState.Success -> {
                    val filteredList = state.photos.filter {
                        activeAlbumFilter == "ALL" || it.albumName.contains(activeAlbumFilter, ignoreCase = true)
                    }

                    if (filteredList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No memories in this album yet.", color = TextSecondaryDark, fontSize = 14.sp)
                                Text("Be the first to upload a trip snapshot!", color = TextMutedDark, fontSize = 12.sp)
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("photo_gallery_grid"),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredList, key = { it.id }) { photo ->
                                val isLiked = photo.likedByUids.contains(currentUser?.uid ?: "")
                                PhotoGridCard(
                                    photo = photo,
                                    isLiked = isLiked,
                                    onCardClick = { selectedPhotoDetail = photo },
                                    onLikeClick = { viewModel.toggleLike(photo) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog to Upload a Memory Image URL
    if (showUploadDialog) {
        UploadPhotoDialog(
            isUploading = isUploading,
            onDismiss = { showUploadDialog = false },
            onConfirm = { title, album, url, loc ->
                viewModel.uploadPhoto(title, album, url, loc) {
                    showUploadDialog = false
                }
            }
        )
    }

    // Modal Photo Detail Viewer
    selectedPhotoDetail?.let { photo ->
        PhotoDetailDialog(
            photo = photo,
            onDismiss = { selectedPhotoDetail = null },
            onLike = { viewModel.toggleLike(photo) }
        )
    }
}

@Composable
private fun PhotoGridCard(
    photo: ClubhousePhoto,
    isLiked: Boolean,
    onCardClick: () -> Unit,
    onLikeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCardClick() }
            .testTag("gallery_photo_card_${photo.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Photo Container / Placeholder with subtle gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(DarkSurfaceVariant, Color(0xFF162536))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Landscape,
                        contentDescription = null,
                        tint = EmeraldPrimary.copy(alpha = 0.6f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = photo.albumName,
                        fontSize = 10.sp,
                        color = CyberTeal,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Location badge in top-left
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "📍 ${photo.location}",
                        color = TextPrimaryDark,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = photo.title,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "by ${photo.uploaderName}",
                    fontSize = 11.sp,
                    color = TextSecondaryDark,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = photo.albumName,
                            fontSize = 9.sp,
                            color = CyberTeal,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onLikeClick() }
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) SoftError else TextMutedDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${photo.likesCount}",
                            fontSize = 11.sp,
                            color = if (isLiked) SoftError else TextSecondaryDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UploadPhotoDialog(
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (title: String, album: String, url: String, location: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var album by remember { mutableStateOf("Wayanad 2026") }
    var url by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Meppadi, Wayanad") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Memory Photo to Vault", fontWeight = FontWeight.Bold, color = TextPrimaryDark)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Photo Title / Caption") },
                    placeholder = { Text("e.g. Chembra Sunset Vista") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimaryDark
                    )
                )

                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text("Album Name") },
                    placeholder = { Text("e.g. Wayanad 2026 / Road Trips") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimaryDark
                    )
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Image URL / Storage Reference") },
                    placeholder = { Text("https://... or photo URI") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimaryDark
                    )
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    placeholder = { Text("e.g. Munnar, Kerala") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimaryDark
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalUrl = url.ifBlank { "https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?auto=format&fit=crop&w=600&q=80" }
                    onConfirm(title, album, finalUrl, location)
                },
                enabled = title.isNotBlank() && !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black)
                } else {
                    Text("Add to Vault", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
private fun PhotoDetailDialog(
    photo: ClubhousePhoto,
    onDismiss: () -> Unit,
    onLike: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(photo.title, fontWeight = FontWeight.Bold, color = TextPrimaryDark, fontSize = 16.sp)
                Text("📍 ${photo.location}", fontSize = 11.sp, color = CyberTeal)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Landscape,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Text(
                    text = "Album: ${photo.albumName} • Uploaded by ${photo.uploaderName}",
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = SoftError, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${photo.likesCount} Gang Members Loved this photo", fontSize = 12.sp, color = TextPrimaryDark, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onLike,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Like Photo ❤️", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondaryDark)
            }
        },
        containerColor = DarkSurface
    )
}
