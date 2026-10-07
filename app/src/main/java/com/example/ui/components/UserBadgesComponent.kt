package com.example.ui.components

import androidx.compose.foundation.background
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
import com.example.model.UserBadgeItem
import com.example.ui.theme.*
import com.example.viewmodel.UserBadgesViewModel

@Composable
fun UserBadgesComponent(
    viewModel: UserBadgesViewModel,
    modifier: Modifier = Modifier
) {
    val badgesState by viewModel.userBadgesState.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var selectedBadgeDetail by remember { mutableStateOf<UserBadgeItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("user_badges_container")
    ) {
        // Badges Header & Gang Points Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🏆 Clubhouse Achievements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Text(
                    text = "${badgesState.totalBadgesUnlocked} Unlocked • Synced with Cloud Firestore",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )
            }

            Surface(
                color = WarmAmber.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(WarmGoldAccent.copy(alpha = 0.6f), WarmAmber.copy(alpha = 0.2f)))
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⭐ ${badgesState.gangPoints}", color = WarmGoldAccent, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "PTS", color = TextSecondaryDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Grid of Badges
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp)
                .testTag("badges_grid")
        ) {
            items(badgesState.badges, key = { it.badgeId }) { badge ->
                BadgeGridCard(
                    badge = badge,
                    onClick = { selectedBadgeDetail = badge }
                )
            }
        }
    }

    // Detail & Achievement Dialog
    selectedBadgeDetail?.let { activeBadge ->
        AlertDialog(
            onDismissRequest = { selectedBadgeDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = activeBadge.iconEmoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = activeBadge.title, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = activeBadge.description, color = TextSecondaryDark, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        color = if (activeBadge.isUnlocked) EmeraldContainer else DarkSurfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (activeBadge.isUnlocked) "UNLOCKED ✓" else "IN PROGRESS (${activeBadge.progress}/${activeBadge.maxProgress})",
                            color = if (activeBadge.isUnlocked) EmeraldPrimary else CyberTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (!activeBadge.isUnlocked) {
                        LinearProgressIndicator(
                            progress = { (activeBadge.progress.toFloat() / activeBadge.maxProgress.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = EmeraldPrimary,
                            trackColor = DarkSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                if (!activeBadge.isUnlocked) {
                    Button(
                        onClick = {
                            viewModel.progressBadge(activeBadge.badgeId)
                            selectedBadgeDetail = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Progress (+1)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedBadgeDetail = null }) {
                    Text("Close", color = TextSecondaryDark)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun BadgeGridCard(
    badge: UserBadgeItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("badge_card_${badge.badgeId}"),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) DarkSurfaceVariant else DarkSurfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (badge.isUnlocked) CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(listOf(EmeraldPrimary.copy(alpha = 0.5f), Color.Transparent))
        ) else null
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (badge.isUnlocked) EmeraldContainer else DeepNavyBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge.iconEmoji,
                    fontSize = 22.sp,
                    color = if (badge.isUnlocked) Color.Unspecified else TextMutedDark
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = badge.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (badge.isUnlocked) TextPrimaryDark else TextSecondaryDark,
                maxLines = 1
            )

            Text(
                text = if (badge.isUnlocked) "Unlocked" else "${badge.progress}/${badge.maxProgress}",
                fontSize = 10.sp,
                color = if (badge.isUnlocked) EmeraldPrimary else TextMutedDark
            )
        }
    }
}
