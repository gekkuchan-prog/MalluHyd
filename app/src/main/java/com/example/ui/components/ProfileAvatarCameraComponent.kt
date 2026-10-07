package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.UserProfile
import com.example.repository.ClubhouseFirestoreRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun ProfileAvatarCameraComponent(
    userProfile: UserProfile,
    firestoreRepository: ClubhouseFirestoreRepository,
    modifier: Modifier = Modifier,
    onAvatarUpdated: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Camera take picture preview launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            // Convert to lightweight data URI / Base64 string for immediate storage
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val bytes = outputStream.toByteArray()
            val base64String = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)

            coroutineScope.launch {
                isSaving = true
                val result = firestoreRepository.saveUserProfile(
                    userProfile.copy(avatarUri = base64String)
                )
                result.onSuccess {
                    statusMessage = "Avatar synced with Firestore! ✓"
                    onAvatarUpdated(base64String)
                }.onFailure { err ->
                    statusMessage = "Upload error: ${err.localizedMessage}"
                }
                isSaving = false
            }
        }
    }

    // Camera permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            statusMessage = "Camera permission needed to take profile picture"
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("avatar_camera_component"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar circle with camera action badge
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(EmeraldContainer)
                .border(2.dp, Brush.linearGradient(listOf(EmeraldPrimary, CyberTeal)), CircleShape)
                .clickable {
                    if (hasCameraPermission) {
                        cameraLauncher.launch(null)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
                .testTag("avatar_camera_clickable_circle"),
            contentAlignment = Alignment.Center
        ) {
            if (capturedBitmap != null) {
                Image(
                    bitmap = capturedBitmap!!.asImageBitmap(),
                    contentDescription = "Clubhouse Avatar",
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Text(
                    text = userProfile.avatarInitials.ifBlank { "MG" },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = EmeraldPrimary
                )
            }

            // Camera Icon overlay in bottom right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(EmeraldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Take Photo",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = userProfile.fullName.ifBlank { "Gang Member" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryDark
        )

        Text(
            text = "${userProfile.nickname} • Tap avatar to snap photo",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryDark
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Take Photo Action Button
        Button(
            onClick = {
                if (hasCameraPermission) {
                    cameraLauncher.launch(null)
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("snap_avatar_photo_btn"),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Syncing to Firestore...", color = Color.Black, fontSize = 12.sp)
            } else {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SNAP NEW AVATAR", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        statusMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = msg,
                fontSize = 11.sp,
                color = if (msg.contains("error", ignoreCase = true)) SoftError else EmeraldPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
