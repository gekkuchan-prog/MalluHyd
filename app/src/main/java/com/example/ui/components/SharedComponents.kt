package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole
import com.example.ui.theme.*

@Composable
fun MalluGangHeader(
    title: String,
    subtitle: String? = null,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    actionIcon: ImageVector? = null,
    onActionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkSurface,
        tonalElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .testTag("back_button")
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = EmeraldPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(EmeraldPrimary, CyberTeal)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "MG",
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark,
                    maxLines = 1
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark,
                        maxLines = 1
                    )
                }
            }

            if (actionIcon != null) {
                IconButton(
                    onClick = onActionClick,
                    modifier = Modifier.testTag("header_action_button")
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = "Action",
                        tint = CyberTeal
                    )
                }
            }
        }
    }
}

@Composable
fun RoleBadge(role: UserRole) {
    val (bg, textCol, label) = when (role) {
        UserRole.SUPER_ADMIN -> Triple(WarmAmber.copy(alpha = 0.2f), WarmGoldAccent, "👑 Super Admin")
        UserRole.ADMIN_1, UserRole.ADMIN_2 -> Triple(CyberTeal.copy(alpha = 0.2f), CyberTeal, "🛡️ Admin")
        UserRole.MEMBER -> Triple(EmeraldPrimary.copy(alpha = 0.15f), EmeraldPrimary, "Gang Member")
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(textCol.copy(alpha = 0.5f), textCol.copy(alpha = 0.2f)))
        )
    ) {
        Text(
            text = label,
            color = textCol,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun GangAvatar(
    initials: String,
    sizeDp: Int = 44,
    colorHex: Long = 0xFF00E676,
    isOnline: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(sizeDp.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color(colorHex)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = (sizeDp / 2.6).sp
            )
        }

        if (isOnline) {
            Box(
                modifier = Modifier
                    .size((sizeDp / 3.2).dp.coerceAtLeast(10.dp))
                    .clip(CircleShape)
                    .background(DeepNavyBg)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(EmeraldPrimary)
                )
            }
        }
    }
}
