package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GangExpense
import com.example.ui.theme.*
import com.example.viewmodel.GangViewModel

@Composable
fun SharedExpenseSummaryComponent(
    viewModel: GangViewModel,
    modifier: Modifier = Modifier
) {
    val expenses by viewModel.expenses.collectAsState()
    val members by viewModel.members.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showAddExpenseDialog by remember { mutableStateOf(false) }

    // Summary calculations
    val totalClubhouseSpending = expenses.sumOf { it.totalAmount }
    val totalSettled = expenses.sumOf { exp ->
        exp.participants.filter { it.isSettled }.sumOf { it.shareAmount }
    }
    val totalPending = totalClubhouseSpending - totalSettled

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBg)
            .testTag("shared_expenses_container")
    ) {
        // High-level Financial Summary Overview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("expenses_summary_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(WarmGoldAccent.copy(alpha = 0.5f), CyberTeal.copy(alpha = 0.3f))
                )
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "💰 Gang Split Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Fair share calculator for trips & clubhouse bills",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                    }

                    Button(
                        onClick = { showAddExpenseDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_expense_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Bill", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ExpenseStatColumn(
                        label = "Total Pool",
                        amount = "₹${totalClubhouseSpending.toInt()}",
                        color = WarmGoldAccent
                    )
                    ExpenseStatColumn(
                        label = "Settled",
                        amount = "₹${totalSettled.toInt()}",
                        color = EmeraldPrimary
                    )
                    ExpenseStatColumn(
                        label = "Pending",
                        amount = "₹${totalPending.toInt()}",
                        color = if (totalPending > 0) SoftError else EmeraldPrimary
                    )
                }
            }
        }

        // Categorized Expense Pie Chart Data Visualization
        ExpenseCategoryPieChartComponent(
            expenses = expenses,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Section Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Expenses (${expenses.size})",
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark,
                fontSize = 15.sp
            )
            Text(
                text = "Tap participant to toggle settlement",
                color = TextMutedDark,
                fontSize = 11.sp
            )
        }

        // Expense Item Cards List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("expense_items_lazy_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(expenses, key = { it.id }) { expense ->
                ExpenseDetailCard(
                    expense = expense,
                    onToggleParticipant = { participantName ->
                        viewModel.toggleExpenseSettlement(expense.id, participantName)
                    }
                )
            }
        }
    }

    // Modal dialog to add a new shared expense
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            defaultPaidBy = currentUser.fullName,
            availableMembers = members.map { it.fullName },
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { title, amount, paidBy, selectedParticipants ->
                viewModel.addExpense(title, amount, paidBy, selectedParticipants)
                showAddExpenseDialog = false
            }
        )
    }
}

@Composable
private fun ExpenseStatColumn(label: String, amount: String, color: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(text = label, fontSize = 11.sp, color = TextSecondaryDark)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = amount, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
private fun ExpenseDetailCard(
    expense: GangExpense,
    onToggleParticipant: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_card_${expense.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.title,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Paid by ${expense.paidByName} • ${expense.dateText}",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }

                Surface(
                    color = WarmAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "₹${expense.totalAmount.toInt()}",
                        color = WarmGoldAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = DarkSurfaceVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Member Shares (₹${(expense.totalAmount / expense.participants.size.coerceAtLeast(1)).toInt()}/person):",
                fontSize = 12.sp,
                color = CyberTeal,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Participant rows with toggle settlement status
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                expense.participants.forEach { item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleParticipant(item.userName) },
                        color = if (item.isSettled) DarkSurfaceVariant.copy(alpha = 0.5f) else DarkSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (item.isSettled) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (item.isSettled) EmeraldPrimary else TextMutedDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.userName,
                                    fontSize = 13.sp,
                                    color = if (item.isSettled) TextSecondaryDark else TextPrimaryDark
                                )
                            }

                            Text(
                                text = if (item.isSettled) "₹${item.shareAmount.toInt()} Settled ✓" else "₹${item.shareAmount.toInt()} Pending",
                                color = if (item.isSettled) EmeraldPrimary else SoftError,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddExpenseDialog(
    defaultPaidBy: String,
    availableMembers: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, paidBy: String, participants: List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var paidBy by remember { mutableStateOf(defaultPaidBy) }
    var selectedMembers by remember { mutableStateOf(availableMembers.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Shared Expense", fontWeight = FontWeight.Bold, color = TextPrimaryDark)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title") },
                    placeholder = { Text("e.g. Fuel for Wayanad trip") },
                    modifier = Modifier.fillMaxWidth().testTag("add_expense_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimaryDark
                    )
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Total Amount (₹)") },
                    placeholder = { Text("e.g. 2400") },
                    modifier = Modifier.fillMaxWidth().testTag("add_expense_amount_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimaryDark
                    )
                )

                Text(
                    text = "Split equally among (${selectedMembers.size} members):",
                    fontSize = 12.sp,
                    color = CyberTeal,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    availableMembers.forEach { memberName ->
                        val isChecked = selectedMembers.contains(memberName)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    selectedMembers = if (isChecked) {
                                        if (selectedMembers.size > 1) selectedMembers - memberName else selectedMembers
                                    } else {
                                        selectedMembers + memberName
                                    }
                                },
                            color = if (isChecked) EmeraldContainer.copy(alpha = 0.4f) else DarkSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = memberName, color = TextPrimaryDark, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val validAmount = amountText.toDoubleOrNull()
            Button(
                onClick = {
                    if (title.isNotBlank() && validAmount != null && validAmount > 0) {
                        onConfirm(title, validAmount, paidBy, selectedMembers.toList())
                    }
                },
                enabled = title.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("confirm_add_expense_btn")
            ) {
                Text("Add Expense", color = Color.Black, fontWeight = FontWeight.Bold)
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
