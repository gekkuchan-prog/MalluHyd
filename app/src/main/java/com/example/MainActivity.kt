package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMutedDark
import com.example.viewmodel.GangViewModel
import com.example.viewmodel.MainTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current
            val firestoreRepo = remember { com.example.repository.ClubhouseFirestoreRepository(context) }
            val themeViewModel: com.example.viewmodel.ThemeViewModel = viewModel {
                com.example.viewmodel.ThemeViewModel(firestoreRepo)
            }
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MalluGangApp(themeViewModel = themeViewModel)
            }
        }
    }
}

@Composable
fun MalluGangApp(
    viewModel: GangViewModel = viewModel(),
    themeViewModel: com.example.viewmodel.ThemeViewModel? = null
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val activeSubScreen by viewModel.activeSubScreen.collectAsState()
    val activeConversationId by viewModel.activeConversationId.collectAsState()
    val activeGameRoomId by viewModel.activeGameRoomId.collectAsState()

    // Handle physical back button or gesture navigation
    BackHandler(enabled = activeSubScreen != null) {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Show bottom navigation bar when on root tab screen
            if (activeSubScreen == null) {
                NavigationBar(
                    containerColor = DeepNavyBg,
                    contentColor = EmeraldPrimary,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.testTag("main_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = selectedTab == MainTab.HOME,
                        onClick = { viewModel.selectTab(MainTab.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldPrimary,
                            unselectedIconColor = TextMutedDark,
                            unselectedTextColor = TextMutedDark
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == MainTab.CHATS,
                        onClick = { viewModel.selectTab(MainTab.CHATS) },
                        icon = { Icon(Icons.Default.Chat, contentDescription = "Chats") },
                        label = { Text("Chats") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldPrimary,
                            unselectedIconColor = TextMutedDark,
                            unselectedTextColor = TextMutedDark
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == MainTab.GAMES,
                        onClick = { viewModel.selectTab(MainTab.GAMES) },
                        icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Games") },
                        label = { Text("Games") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldPrimary,
                            unselectedIconColor = TextMutedDark,
                            unselectedTextColor = TextMutedDark
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == MainTab.COMMUNITY,
                        onClick = { viewModel.selectTab(MainTab.COMMUNITY) },
                        icon = { Icon(Icons.Default.Groups, contentDescription = "Clubhouse") },
                        label = { Text("Clubhouse") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldPrimary,
                            unselectedIconColor = TextMutedDark,
                            unselectedTextColor = TextMutedDark
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == MainTab.ADMIN,
                        onClick = { viewModel.selectTab(MainTab.ADMIN) },
                        icon = { Icon(Icons.Default.Security, contentDescription = "Admin") },
                        label = { Text("Admin") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldPrimary,
                            unselectedIconColor = TextMutedDark,
                            unselectedTextColor = TextMutedDark
                        )
                    )
                }
            }
        },
        containerColor = DeepNavyBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeSubScreen) {
                "chat_detail" -> {
                    ChatDetailScreen(
                        conversationId = activeConversationId ?: "group_main",
                        viewModel = viewModel,
                        onBackClick = { viewModel.navigateBack() },
                        onOpenGameRoom = { rId -> viewModel.openGameRoom(rId) }
                    )
                }
                "game_room" -> {
                    GameRoomScreen(
                        roomId = activeGameRoomId ?: "room_ludo_1",
                        viewModel = viewModel,
                        onBackClick = { viewModel.navigateBack() }
                    )
                }
                "submit_complaint" -> {
                    SubmitComplaintScreen(
                        viewModel = viewModel,
                        onBackClick = { viewModel.navigateBack() }
                    )
                }
                "add_member" -> {
                    AddMemberScreen(
                        viewModel = viewModel,
                        onBackClick = { viewModel.navigateBack() }
                    )
                }
                "rules" -> {
                    GangRulesScreen(
                        viewModel = viewModel,
                        onBackClick = { viewModel.navigateBack() }
                    )
                }
                "profile" -> {
                    UserProfileScreen(
                        viewModel = viewModel,
                        onBackClick = { viewModel.navigateBack() }
                    )
                }
                else -> {
                    // Root Main Tabs
                    when (selectedTab) {
                        MainTab.HOME -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToChat = { convId -> viewModel.openChat(convId) },
                                onNavigateToGame = { rId -> viewModel.openGameRoom(rId) },
                                onOpenRules = { viewModel.navigateTo("rules") },
                                onOpenComplaint = { viewModel.navigateTo("submit_complaint") },
                                onOpenProfile = { viewModel.navigateTo("profile") }
                            )
                        }
                        MainTab.CHATS -> {
                            // Direct Chat screen uses the main group or active conversation
                            ChatDetailScreen(
                                conversationId = "group_main",
                                viewModel = viewModel,
                                onBackClick = { viewModel.selectTab(MainTab.HOME) },
                                onOpenGameRoom = { rId -> viewModel.openGameRoom(rId) }
                            )
                        }
                        MainTab.GAMES -> {
                            GameRoomScreen(
                                roomId = activeGameRoomId ?: "room_ludo_1",
                                viewModel = viewModel,
                                onBackClick = { viewModel.selectTab(MainTab.HOME) }
                            )
                        }
                        MainTab.COMMUNITY -> {
                            CommunityScreen(
                                viewModel = viewModel,
                                onOpenRules = { viewModel.navigateTo("rules") },
                                onOpenComplaint = { viewModel.navigateTo("submit_complaint") }
                            )
                        }
                        MainTab.ADMIN -> {
                            AdminDashboardScreen(
                                viewModel = viewModel,
                                onNavigateToAddMember = { viewModel.navigateTo("add_member") },
                                onNavigateToRules = { viewModel.navigateTo("rules") }
                            )
                        }
                    }
                }
            }
        }
    }
}
