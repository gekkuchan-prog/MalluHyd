package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GangExpense
import com.example.ui.theme.*

data class ExpenseCategorySlice(
    val categoryName: String,
    val amount: Double,
    val color: Color,
    val iconEmoji: String,
    val percentage: Float = 0f
)

@Composable
fun ExpenseCategoryPieChartComponent(
    expenses: List<GangExpense>,
    modifier: Modifier = Modifier
) {
    // Categorize expenses into standard clubhouse categories: Snacks & Food, Travel & Trips, Utilities, Gaming Equipment
    val categorySlices = remember(expenses) {
        val total = expenses.sumOf { it.totalAmount }.coerceAtLeast(1.0)

        // Classify each expense by title keywords
        var snacksAmount = 0.0
        var tripsAmount = 0.0
        var utilitiesAmount = 0.0
        var gamingEquipmentAmount = 0.0

        expenses.forEach { exp ->
            val titleLower = exp.title.lowercase()
            when {
                titleLower.contains("snack") || titleLower.contains("tea") || titleLower.contains("coffee") || titleLower.contains("biryani") || titleLower.contains("food") -> {
                    snacksAmount += exp.totalAmount
                }
                titleLower.contains("fuel") || titleLower.contains("trip") || titleLower.contains("villa") || titleLower.contains("wayanad") || titleLower.contains("drive") -> {
                    tripsAmount += exp.totalAmount
                }
                titleLower.contains("ludo") || titleLower.contains("carrom") || titleLower.contains("board") || titleLower.contains("equipment") || titleLower.contains("game") -> {
                    gamingEquipmentAmount += exp.totalAmount
                }
                else -> {
                    utilitiesAmount += exp.totalAmount
                }
            }
        }

        // Default distribution fallback if empty
        if (snacksAmount == 0.0 && tripsAmount == 0.0 && utilitiesAmount == 0.0 && gamingEquipmentAmount == 0.0) {
            tripsAmount = 6000.0
            snacksAmount = 1800.0
            gamingEquipmentAmount = 1200.0
            utilitiesAmount = 500.0
        }

        val effectiveTotal = (snacksAmount + tripsAmount + utilitiesAmount + gamingEquipmentAmount).coerceAtLeast(1.0)

        listOf(
            ExpenseCategorySlice(
                categoryName = "Trips & Travel",
                amount = tripsAmount,
                color = EmeraldPrimary,
                iconEmoji = "🚗",
                percentage = (tripsAmount / effectiveTotal).toFloat() * 100f
            ),
            ExpenseCategorySlice(
                categoryName = "Snacks & Refreshments",
                amount = snacksAmount,
                color = WarmGoldAccent,
                iconEmoji = "☕",
                percentage = (snacksAmount / effectiveTotal).toFloat() * 100f
            ),
            ExpenseCategorySlice(
                categoryName = "Clubhouse Equipment",
                amount = gamingEquipmentAmount,
                color = CyberTeal,
                iconEmoji = "🎮",
                percentage = (gamingEquipmentAmount / effectiveTotal).toFloat() * 100f
            ),
            ExpenseCategorySlice(
                categoryName = "Utilities & Misc",
                amount = utilitiesAmount,
                color = SoftError,
                iconEmoji = "💡",
                percentage = (utilitiesAmount / effectiveTotal).toFloat() * 100f
            )
        ).filter { it.amount > 0 }
    }

    var selectedSlice by remember { mutableStateOf<ExpenseCategorySlice?>(null) }
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(categorySlices) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("expense_pie_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📊 Expense Category Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "Distribution across clubhouse spending",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark
                    )
                }

                Surface(
                    color = CyberTeal.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Recharts / M3",
                        color = CyberTeal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Native High-Precision Donut / Pie Chart Render
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .testTag("pie_chart_canvas_box"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val diameter = size.minDimension
                    val strokeWidth = 38.dp.toPx()
                    val arcSize = Size(diameter - strokeWidth, diameter - strokeWidth)
                    val arcOffset = Offset(strokeWidth / 2, strokeWidth / 2)

                    var startAngle = -90f
                    val sweepProgress = animatedProgress.value

                    categorySlices.forEach { slice ->
                        val targetSweep = (slice.percentage / 100f) * 360f
                        val actualSweep = targetSweep * sweepProgress

                        drawArc(
                            color = slice.color,
                            startAngle = startAngle,
                            sweepAngle = actualSweep,
                            useCenter = false,
                            topLeft = arcOffset,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )
                        startAngle += targetSweep
                    }
                }

                // Center Label displaying selected slice or grand total
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val active = selectedSlice
                    if (active != null) {
                        Text(
                            text = active.iconEmoji,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "₹${active.amount.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = active.color
                        )
                        Text(
                            text = "${active.percentage.toInt()}%",
                            fontSize = 11.sp,
                            color = TextSecondaryDark,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        val total = categorySlices.sumOf { it.amount }
                        Text(
                            text = "Total",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                        Text(
                            text = "₹${total.toInt()}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "${categorySlices.size} Categories",
                            fontSize = 10.sp,
                            color = CyberTeal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive Category Legend Items
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categorySlices.forEach { slice ->
                    val isSelected = selectedSlice == slice
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedSlice = if (isSelected) null else slice
                            },
                        color = if (isSelected) slice.color.copy(alpha = 0.2f) else DarkSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(slice.color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${slice.iconEmoji} ${slice.categoryName}",
                                    fontSize = 13.sp,
                                    color = TextPrimaryDark,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹${slice.amount.toInt()}",
                                    fontSize = 13.sp,
                                    color = slice.color,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${slice.percentage.toInt()}%)",
                                    fontSize = 11.sp,
                                    color = TextSecondaryDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
