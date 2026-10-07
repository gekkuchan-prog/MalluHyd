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
import com.example.model.*
import com.example.ui.components.GangAvatar
import com.example.ui.components.MalluGangHeader
import com.example.ui.components.RoleBadge
import com.example.ui.theme.*
import com.example.viewmodel.GangViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: GangViewModel,
    onNavigateToAddMember: () -> Unit,
    onNavigateToRules: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val members by viewModel.members.collectAsState()
    val complaints by viewModel.complaints.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var selectedAdminTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Members (${members.size})", "Complaints (${complaints.size})", "Audit Logs")

    var memberDirectorySearchQuery by remember { mutableStateOf("") }
    val filteredMembers = remember(memberDirectorySearchQuery, members) {
        if (memberDirectorySearchQuery.isBlank()) members
        else members.filter {
            it.fullName.contains(memberDirectorySearchQuery, ignoreCase = true) ||
            it.nickname.contains(memberDirectorySearchQuery, ignoreCase = true)
        }
    }

    var selectedComplaintForReview by remember { mutableStateOf<Complaint?>(null) }
    var reviewNoteText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            MalluGangHeader(
                title = "Gang Administration",
                subtitle = "3-Tier RBAC • Audit Trail • Moderation",
                actionIcon = Icons.Default.PersonAdd,
                onActionClick = onNavigateToAddMember
            )
        },
        containerColor = DeepNavyBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedAdminTab,
                containerColor = DarkSurface,
                contentColor = EmeraldPrimary,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedAdminTab == index,
                        onClick = { selectedAdminTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedAdminTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedAdminTab == index) EmeraldPrimary else TextSecondaryDark
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("admin_dashboard_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedAdminTab) {
                    0 -> { // Member Management
                        item {
                            // Floating Search Bar Card with real-time filtering
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("floating_member_search_bar"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(CyberTeal.copy(alpha = 0.5f))
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search Members",
                                        tint = CyberTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    OutlinedTextField(
                                        value = memberDirectorySearchQuery,
                                        onValueChange = { memberDirectorySearchQuery = it },
                                        placeholder = { Text("Filter members by name or nickname...", color = TextMutedDark, fontSize = 13.sp) },
                                        modifier = Modifier.weight(1f).testTag("directory_search_textfield"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedTextColor = TextPrimaryDark,
                                            unfocusedTextColor = TextPrimaryDark
                                        ),
                                        singleLine = true
                                    )
                                    if (memberDirectorySearchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { memberDirectorySearchQuery = "" },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear Search", tint = TextSecondaryDark)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = onNavigateToAddMember,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_add_member_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("INVITE MEMBER BY PHONE", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (filteredMembers.isEmpty()) {
                            item {
                                Surface(
                                    color = DarkSurfaceVariant,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                ) {
                                    Text(
                                        text = "No members match '$memberDirectorySearchQuery'",
                                        color = TextSecondaryDark,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }

                        items(filteredMembers, key = { it.id }) { member ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        GangAvatar(initials = member.avatarInitials, sizeDp = 44, isOnline = member.isOnline)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = member.fullName, fontWeight = FontWeight.Bold, color = TextPrimaryDark, fontSize = 15.sp)
                                            Text(text = "${member.nickname} • ${member.phoneNumber}", fontSize = 12.sp, color = TextSecondaryDark)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            RoleBadge(role = member.role)
                                        }

                                        // Status badge
                                        Surface(
                                            color = if (member.status == UserStatus.ACTIVE) EmeraldContainer else SoftError.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = member.status.name,
                                                color = if (member.status == UserStatus.ACTIVE) EmeraldPrimary else SoftError,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Action buttons for Admins
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Appoint Admin (Super Admin only capability)
                                        if (currentUser.role == UserRole.SUPER_ADMIN && member.role != UserRole.SUPER_ADMIN) {
                                            TextButton(
                                                onClick = {
                                                    val nextRole = if (member.role == UserRole.MEMBER) UserRole.ADMIN_1 else UserRole.MEMBER
                                                    viewModel.appointAdminRole(member.id, nextRole)
                                                }
                                            ) {
                                                Text(
                                                    text = if (member.role == UserRole.MEMBER) "Promote to Admin" else "Demote to Member",
                                                    fontSize = 12.sp,
                                                    color = CyberTeal
                                                )
                                            }
                                        }

                                        // Suspend / Reactivate
                                        if (member.role != UserRole.SUPER_ADMIN) {
                                            TextButton(
                                                onClick = { viewModel.toggleMemberStatus(member.id) }
                                            ) {
                                                Text(
                                                    text = if (member.status == UserStatus.ACTIVE) "Suspend" else "Reactivate",
                                                    fontSize = 12.sp,
                                                    color = if (member.status == UserStatus.ACTIVE) SoftError else EmeraldPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> { // Confidential Complaints Box
                        items(complaints) { cmp ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedComplaintForReview = cmp },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "Case ${cmp.trackingCode}", fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 15.sp)
                                        Surface(
                                            color = WarmAmber.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = cmp.status.label,
                                                color = WarmGoldAccent,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Complainant: ${cmp.complainantName} → Against: ${cmp.accusedName}", fontSize = 12.sp, color = CyberTeal)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "Category: ${cmp.category.label}", fontSize = 12.sp, color = TextSecondaryDark, fontWeight = FontWeight.SemiBold)
                                    Text(text = cmp.description, fontSize = 13.sp, color = TextPrimaryDark)
                                    if (cmp.adminNotes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "Admin Note: ${cmp.adminNotes}", fontSize = 11.sp, color = WarmGoldAccent)
                                    }
                                    if (cmp.accusedResponse.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Accused Response: ${cmp.accusedResponse}", fontSize = 11.sp, color = EmeraldPrimary)
                                    }
                                }
                            }
                        }
                    }

                    2 -> { // Admin Audit Logs
                        items(auditLogs) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = CyberTeal.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = log.categoryBadge,
                                            color = CyberTeal,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = log.actionText, fontSize = 13.sp, color = TextPrimaryDark, fontWeight = FontWeight.SemiBold)
                                        Text(text = "${log.actorName} • ${log.timestamp}", fontSize = 11.sp, color = TextSecondaryDark)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Review Action Dialog for Complaints
    selectedComplaintForReview?.let { activeCase ->
        AlertDialog(
            onDismissRequest = { selectedComplaintForReview = null },
            title = { Text("Moderate Case ${activeCase.trackingCode}", fontWeight = FontWeight.Bold, color = TextPrimaryDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Complainant: ${activeCase.complainantName}", fontSize = 12.sp, color = TextSecondaryDark)
                    Text("Accused: ${activeCase.accusedName}", fontSize = 12.sp, color = TextSecondaryDark)
                    Text("Issue: ${activeCase.description}", fontSize = 13.sp, color = TextPrimaryDark)
                    OutlinedTextField(
                        value = reviewNoteText,
                        onValueChange = { reviewNoteText = it },
                        placeholder = { Text("Add administrative resolution note...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.updateComplaintStatus(activeCase.id, ComplaintStatus.RESOLVED, reviewNoteText)
                                selectedComplaintForReview = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Mark Resolved", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                viewModel.updateComplaintStatus(activeCase.id, ComplaintStatus.RESPONSE_REQUESTED, reviewNoteText)
                                selectedComplaintForReview = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberTeal)
                        ) {
                            Text("Request Response", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedComplaintForReview = null }) {
                    Text("Close", color = TextSecondaryDark)
                }
            },
            containerColor = DarkSurface
        )
    }
}
