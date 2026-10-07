package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MemberActivityStatus
import com.example.repository.ClubhouseFirestoreRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MemberStatusIndicatorComponent(
    currentStatus: MemberActivityStatus,
    firestoreRepository: ClubhouseFirestoreRepository,
    modifier: Modifier = Modifier,
    isEditable: Boolean = true,
    onStatusChanged: (MemberActivityStatus) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedStatus by remember(currentStatus) { mutableStateOf(currentStatus) }
    var showDropdownMenu by remember { mutableStateOf(false) }
    var isUpdating by remember { mutableStateOf(false) }

    val animatedColor by animateColorAsState(
        targetValue = Color(selectedStatus.colorHex),
        label = "status_color_anim"
    )

    Box(modifier = modifier) {
        Surface(
            color = animatedColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(20.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(animatedColor.copy(alpha = 0.5f))
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable(enabled = isEditable && !isUpdating) {
                    showDropdownMenu = true
                }
                .testTag("member_status_indicator_chip")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pulsing dot indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(animatedColor)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "${selectedStatus.iconEmoji} ${selectedStatus.label}",
                    color = animatedColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                if (isEditable) {
                    Spacer(modifier = Modifier.width(4.dp))
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = animatedColor,
                            strokeWidth = 1.5.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Change Status",
                            tint = animatedColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Dropdown selection for setting live clubhouse presence
        DropdownMenu(
            expanded = showDropdownMenu,
            onDismissRequest = { showDropdownMenu = false },
            modifier = Modifier
                .background(DarkSurface)
                .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(12.dp))
                .testTag("status_selection_dropdown")
        ) {
            MemberActivityStatus.entries.forEach { statusOption ->
                val isCurrent = statusOption == selectedStatus
                val optColor = Color(statusOption.colorHex)

                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(optColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${statusOption.iconEmoji} ${statusOption.label}",
                                    color = TextPrimaryDark,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = statusOption.description,
                                    color = TextSecondaryDark,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    },
                    trailingIcon = {
                        if (isCurrent) {
                            Icon(Icons.Default.Check, contentDescription = "Active", tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                        }
                    },
                    onClick = {
                        showDropdownMenu = false
                        selectedStatus = statusOption
                        coroutineScope.launch {
                            isUpdating = true
                            firestoreRepository.updateActivityStatus(statusOption)
                            onStatusChanged(statusOption)
                            isUpdating = false
                        }
                    }
                )
            }
        }
    }
}
