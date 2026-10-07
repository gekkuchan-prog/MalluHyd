package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ComplaintCategory
import com.example.model.GangRule
import com.example.model.User
import com.example.ui.components.GangAvatar
import com.example.ui.components.MalluGangHeader
import com.example.ui.components.RoleBadge
import com.example.ui.theme.*
import com.example.viewmodel.GangViewModel

@Composable
fun SubmitComplaintScreen(
    viewModel: GangViewModel,
    onBackClick: () -> Unit
) {
    var accusedName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ComplaintCategory.BEHAVIOUR) }
    var description by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MalluGangHeader(
                title = "Confidential Complaint Box",
                subtitle = "Directly routed to Super Admin & Admins",
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        containerColor = DeepNavyBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .testTag("submit_complaint_form"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔒 Safe Space & Zero Retaliation Guarantee",
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your submission is strictly accessible to appointed gang admins. Accused members do not see who filed without explicit administrative mediation.",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }
            }

            OutlinedTextField(
                value = accusedName,
                onValueChange = { accusedName = it },
                label = { Text("Person or Context Complained Against") },
                placeholder = { Text("e.g. Vishnu Prasad or Munnar Drive") },
                modifier = Modifier.fillMaxWidth().testTag("complaint_accused_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    focusedBorderColor = EmeraldPrimary,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark
                )
            )

            Text("Category of Grievance:", fontWeight = FontWeight.Bold, color = TextPrimaryDark, fontSize = 13.sp)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ComplaintCategory.values().take(4).forEach { cat ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCategory = cat },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedCategory == cat) EmeraldContainer else DarkSurfaceVariant
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = cat.label, color = if (selectedCategory == cat) EmeraldPrimary else TextPrimaryDark)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Detailed Explanation") },
                placeholder = { Text("State the issue clearly and reference any violated rules...") },
                modifier = Modifier.fillMaxWidth().height(120.dp).testTag("complaint_description_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    focusedBorderColor = EmeraldPrimary,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark
                )
            )

            Button(
                onClick = {
                    if (accusedName.isNotBlank() && description.isNotBlank()) {
                        viewModel.submitComplaint(accusedName, selectedCategory, description)
                        isSubmitted = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_complaint_confirm_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("SUBMIT FORMAL GRIEVANCE", color = Color.Black, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun AddMemberScreen(
    viewModel: GangViewModel,
    onBackClick: () -> Unit
) {
    var phoneInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var nicknameInput by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            MalluGangHeader(
                title = "Invite Gang Member",
                subtitle = "Super Admin Exclusive Provisioning",
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        containerColor = DeepNavyBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .testTag("add_member_form"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📱 Phone Number Verification Required",
                        fontWeight = FontWeight.Bold,
                        color = CyberTeal,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Super Admins invite verified numbers. The user verifies their own device via SMS OTP before full access is unlocked. No fake identity accounts.",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }
            }

            OutlinedTextField(
                value = phoneInput,
                onValueChange = { phoneInput = it },
                label = { Text("Phone Number") },
                placeholder = { Text("+91 9XXXX XXXXX") },
                modifier = Modifier.fillMaxWidth().testTag("add_member_phone_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    focusedBorderColor = EmeraldPrimary
                )
            )

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Full Legal Name") },
                placeholder = { Text("e.g. Gokul Suresh") },
                modifier = Modifier.fillMaxWidth().testTag("add_member_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    focusedBorderColor = EmeraldPrimary
                )
            )

            OutlinedTextField(
                value = nicknameInput,
                onValueChange = { nicknameInput = it },
                label = { Text("Gang Nickname") },
                placeholder = { Text("e.g. Gopi") },
                modifier = Modifier.fillMaxWidth().testTag("add_member_nickname_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    focusedBorderColor = EmeraldPrimary
                )
            )

            resultMessage?.let { msg ->
                Text(text = msg, color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    if (phoneInput.isNotBlank() && nameInput.isNotBlank()) {
                        val ok = viewModel.addMember(phoneInput, nameInput, nicknameInput.ifBlank { nameInput })
                        if (ok) {
                            resultMessage = "✅ Official invitation dispatched to $phoneInput!"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("confirm_add_member_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("SEND GANG INVITATION", color = Color.Black, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun GangRulesScreen(
    viewModel: GangViewModel,
    onBackClick: () -> Unit
) {
    val rules by viewModel.gangRules.collectAsState()

    Scaffold(
        topBar = {
            MalluGangHeader(
                title = "Mallu Gang Constitution",
                subtitle = "Official clubhouse code of honor",
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        containerColor = DeepNavyBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("gang_rules_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(rules) { r ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = WarmAmber.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "RULE #${r.number.toString().padStart(2, '0')}",
                                    color = WarmGoldAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = r.title, fontWeight = FontWeight.Bold, color = TextPrimaryDark, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = r.description, fontSize = 13.sp, color = TextSecondaryDark, lineHeight = 19.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun UserProfileScreen(
    viewModel: GangViewModel,
    onBackClick: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val members by viewModel.members.collectAsState()

    Scaffold(
        topBar = {
            MalluGangHeader(
                title = "Member Profile",
                subtitle = "Clubhouse credentials & persona",
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        containerColor = DeepNavyBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .testTag("user_profile_container"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val repo = remember { com.example.repository.ClubhouseFirestoreRepository(context) }
                    val profileModel = remember(currentUser) {
                        com.example.model.UserProfile(
                            userId = currentUser.id,
                            fullName = currentUser.fullName,
                            nickname = currentUser.nickname,
                            phoneNumber = currentUser.phoneNumber,
                            about = currentUser.about,
                            avatarInitials = currentUser.avatarInitials,
                            role = currentUser.role.name
                        )
                    }

                    com.example.ui.components.ProfileAvatarCameraComponent(
                        userProfile = profileModel,
                        firestoreRepository = repo
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Clubhouse Activity Status Indicator & Picker
                    com.example.ui.components.MemberStatusIndicatorComponent(
                        currentStatus = com.example.model.MemberActivityStatus.fromString(profileModel.activityStatus),
                        firestoreRepository = repo
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Global Material 3 Theme Toggle (Persisted in Firestore)
                    val themeVm = remember { com.example.viewmodel.ThemeViewModel(repo) }
                    com.example.ui.components.GlobalThemeToggleComponent(themeViewModel = themeVm)

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = currentUser.about, fontSize = 13.sp, color = TextPrimaryDark)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(label = "Age (Auto)", value = "${currentUser.calculatedAge} yrs")
                        StatItem(label = "Games Played", value = "${currentUser.gamesPlayed}")
                        StatItem(label = "Victories", value = "${currentUser.gamesWon} 🏆")
                    }
                }
            }

            // Search & Filter Clubhouse Members Bar
            var searchQuery by remember { mutableStateOf("") }
            val filteredMembers = remember(searchQuery, members) {
                if (searchQuery.isBlank()) members
                else members.filter {
                    it.fullName.contains(searchQuery, ignoreCase = true) ||
                    it.nickname.contains(searchQuery, ignoreCase = true)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🔍 Search Clubhouse Members",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTeal
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name (e.g. Adhi, Rahul, Sneha)...", fontSize = 12.sp, color = TextMutedDark) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = CyberTeal, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMutedDark)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("member_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedBorderColor = CyberTeal,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Matching Members (${filteredMembers.size}):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (filteredMembers.isEmpty()) {
                        Text(
                            text = "No member found matching '$searchQuery'",
                            fontSize = 12.sp,
                            color = TextMutedDark,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        filteredMembers.forEach { m ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { viewModel.switchProfile(m) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (currentUser.id == m.id) EmeraldContainer else DarkSurface
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        GangAvatar(initials = m.avatarInitials, sizeDp = 28, isOnline = m.isOnline)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(text = m.fullName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                                            Text(text = "${m.nickname} • ${m.role.title}", fontSize = 10.sp, color = TextSecondaryDark)
                                        }
                                    }

                                    if (currentUser.id == m.id) {
                                        Surface(
                                            color = EmeraldPrimary,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "Active Profile",
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
        Text(text = label, fontSize = 11.sp, color = TextSecondaryDark)
    }
}
