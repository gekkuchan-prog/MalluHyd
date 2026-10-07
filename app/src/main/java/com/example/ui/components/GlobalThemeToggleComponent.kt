package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
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
import com.example.ui.theme.*
import com.example.viewmodel.ThemeViewModel

@Composable
fun GlobalThemeToggleComponent(
    themeViewModel: ThemeViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by themeViewModel.isDarkTheme.collectAsState()

    val surfaceColor by animateColorAsState(
        targetValue = if (isDark) DarkSurfaceVariant else Color(0xFFE2E8F0),
        label = "theme_toggle_bg"
    )

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { themeViewModel.toggleTheme() }
            .testTag("global_theme_toggle_button")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = "Toggle Theme",
                tint = if (isDark) WarmGoldAccent else Color(0xFFD97706),
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = if (isDark) "Dark Mode" else "Light Mode",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) TextPrimaryDark else Color(0xFF0F172A)
            )
        }
    }
}
