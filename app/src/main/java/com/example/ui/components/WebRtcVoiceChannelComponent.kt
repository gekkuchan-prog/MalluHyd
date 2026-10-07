package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.VoicePeerState
import com.example.ui.theme.*

@Composable
fun WebRtcVoiceChannelComponent(
    channelName: String = "Clubhouse Lounge (Live)",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isConnected by remember { mutableStateOf(true) }
    var isMicrophoneMuted by remember { mutableStateOf(false) }
    var isDeafened by remember { mutableStateOf(false) }

    // Live WebRTC Peers in voice room
    var peerList by remember {
        mutableStateOf(
            listOf(
                VoicePeerState("u_adhi", "Adithyan (You)", "AM", isSpeaking = true, isMuted = false, pingMs = 24),
                VoicePeerState("u_kichu", "Kichu (Rahul)", "RK", isSpeaking = false, isMuted = false, pingMs = 32),
                VoicePeerState("u_sneha", "Sneha Menon", "SM", isSpeaking = false, isMuted = true, pingMs = 28),
                VoicePeerState("u_arjun", "Appu (Arjun)", "AV", isSpeaking = false, isMuted = false, pingMs = 45)
            )
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            isMicrophoneMuted = false
        }
    }

    val micButtonColor by animateColorAsState(
        targetValue = if (isMicrophoneMuted) SoftError else EmeraldPrimary,
        label = "mic_color_anim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("webrtc_voice_channel_container"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(EmeraldPrimary.copy(alpha = 0.5f), CyberTeal.copy(alpha = 0.3f))
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Room Connection Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) EmeraldPrimary else SoftError)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = channelName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = if (isConnected) "RTC Audio: Connected • Opus 48kHz Stereo" else "Voice Disconnected",
                            fontSize = 11.sp,
                            color = if (isConnected) CyberTeal else TextMutedDark
                        )
                    }
                }

                // Disconnect / Connect Button
                IconButton(
                    onClick = { isConnected = !isConnected },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) SoftError.copy(alpha = 0.2f) else EmeraldContainer)
                        .testTag("voice_connect_disconnect_btn")
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.CallEnd else Icons.Default.Call,
                        contentDescription = "Toggle Connection",
                        tint = if (isConnected) SoftError else EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Connected Members in Room (Avatar & speaking waves)
            if (isConnected) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    peerList.forEach { peer ->
                        val isSelf = peer.userId == "u_adhi"
                        val effectiveMuted = if (isSelf) isMicrophoneMuted else peer.isMuted
                        val effectiveSpeaking = if (isSelf) !isMicrophoneMuted && peer.isSpeaking else peer.isSpeaking

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.testTag("voice_peer_${peer.userId}")
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(DarkSurfaceVariant)
                                        .border(
                                            width = if (effectiveSpeaking) 2.5.dp else 1.dp,
                                            color = if (effectiveSpeaking) EmeraldPrimary else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = peer.avatarInitials,
                                        fontWeight = FontWeight.Bold,
                                        color = if (effectiveSpeaking) EmeraldPrimary else TextPrimaryDark,
                                        fontSize = 14.sp
                                    )
                                }

                                // Mute indicator badge on avatar
                                if (effectiveMuted) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(SoftError),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MicOff,
                                            contentDescription = "Muted",
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = peer.name.take(6),
                                fontSize = 11.sp,
                                color = if (effectiveSpeaking) EmeraldPrimary else TextSecondaryDark,
                                fontWeight = if (effectiveSpeaking) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Control Bar (Mute Mic, Deafen Sound)
                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Microphone Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (!hasAudioPermission) {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    } else {
                                        isMicrophoneMuted = !isMicrophoneMuted
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("toggle_microphone_status_btn")
                        ) {
                            Icon(
                                imageVector = if (isMicrophoneMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Toggle Mic",
                                tint = micButtonColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isMicrophoneMuted) "Unmute Mic" else "Mute Mic",
                                color = TextPrimaryDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Deafen Speaker Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { isDeafened = !isDeafened }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isDeafened) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "Deafen Audio",
                                tint = if (isDeafened) SoftError else CyberTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isDeafened) "Deafened" else "Audio On",
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                Surface(
                    color = DarkSurfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Channel disconnected. Tap to rejoin clubhouse voice.",
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                        Button(
                            onClick = { isConnected = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Rejoin", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
