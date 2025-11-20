package com.humblecoders.fintrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.*
import com.humblecoders.fintrack.ui.theme.BudgetBaeDarkGreen
import com.humblecoders.fintrack.ui.theme.BudgetBaeGold
import com.humblecoders.fintrack.ui.theme.BudgetBaeTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    refreshTrigger: Int = 0,
    onNavigateToHome: () -> Unit = {},
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToIncome: () -> Unit = {},
    onNavigateToSubscriptions: () -> Unit = {},
    onAddGoalClick: () -> Unit = {}
) {
    var goals by remember { mutableStateOf<List<Goal>>(emptyList()) }
    var totalProgress by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    var streakCount by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun loadGoals() {
        scope.launch {
            isLoading = true

            FirebaseHelper.getAllGoals().onSuccess {
                goals = it
                // Calculate total streak count (sum of all contribution counts)
                streakCount = it.sumOf { goal -> goal.contributionCount }
            }

            FirebaseHelper.getTotalGoalProgress().onSuccess {
                totalProgress = it
            }

            isLoading = false
        }
    }

    LaunchedEffect(refreshTrigger) {
        loadGoals()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Goals",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = onAddGoalClick) {
                        Icon(Icons.Default.Add, contentDescription = "Add Goal")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = false,
                    onClick = onNavigateToHome
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Expenses") },
                    label = { Text("Expenses") },
                    selected = false,
                    onClick = onNavigateToExpenses
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Income") },
                    label = { Text("Income") },
                    selected = false,
                    onClick = onNavigateToIncome
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Subscriptions") },
                    label = { 
                        Text(
                            "Subscriptions",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    selected = false,
                    onClick = onNavigateToSubscriptions
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Flag, contentDescription = "Goals") },
                    label = { Text("Goals") },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BudgetBaeDarkGreen,
                        selectedTextColor = BudgetBaeDarkGreen,
                        indicatorColor = BudgetBaeDarkGreen.copy(alpha = 0.2f)
                    )
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BudgetBaeDarkGreen)
                    }
                }
            } else {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = BudgetBaeTeal
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(BudgetBaeGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🔥",
                                    fontSize = 28.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$streakCount Contribution${if (streakCount != 1) "s" else ""}!",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Text(
                                    text = "Keep saving to unlock rewards!",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val nextMilestone = ((streakCount / 7) + 1) * 7
                                val progressToNext = streakCount % 7
                                Text(
                                    text = "$progressToNext/7 contributions until next reward",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = { (progressToNext / 7f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = BudgetBaeGold,
                                    trackColor = Color.White.copy(alpha = 0.3f)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = "🏅", fontSize = 20.sp)
                                Text(text = "🏆", fontSize = 20.sp)
                                Text(text = "👑", fontSize = 20.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "₹${String.format("%,.0f", totalProgress)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = BudgetBaeDarkGreen
                            )
                            Text(
                                text = "Total Progress",
                                fontSize = 14.sp,
                                color = Color(0xFF999999)
                            )
                        }

                        IconButton(onClick = { /* More options */ }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = BudgetBaeDarkGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                items(goals) { goal ->
                    GoalItem(
                        goal = goal,
                        onAddMoneyClick = { amount ->
                            scope.launch {
                                FirebaseHelper.addMoneyToGoal(goal.id, amount)
                                loadGoals()
                                ToastHelper.showGoalToast(context)
                            }
                        }
                    )
                }

                item {
                    Button(
                        onClick = onAddGoalClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BudgetBaeTeal
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "+ Create New Goal",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun GoalItem(
    goal: Goal,
    onAddMoneyClick: (Double) -> Unit
) {
    var showAddMoneyDialog by remember { mutableStateOf(false) }

    val progress = if (goal.targetAmount > 0) {
        (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0)
    } else 0.0

    val motivationalMessage = when {
        progress >= 0.9 -> "So close!"
        progress >= 0.75 -> "Almost there!"
        progress >= 0.5 -> "Halfway there!"
        progress >= 0.25 -> "Keep going!"
        else -> "Just started!"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = BudgetBaeTeal
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(BudgetBaeGold),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getGoalIcon(goal.icon),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "₹${String.format("%,.0f", goal.currentAmount)} / ₹${String.format("%,.0f", goal.targetAmount)}",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progress.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )

                Text(
                    text = motivationalMessage,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = { showAddMoneyDialog = true },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BudgetBaeGold)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Money",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (showAddMoneyDialog) {
        AddMoneyDialog(
            goalName = goal.name,
            onDismiss = { showAddMoneyDialog = false },
            onConfirm = { amount ->
                onAddMoneyClick(amount)
                showAddMoneyDialog = false
            }
        )
    }
}

@Composable
fun AddMoneyDialog(
    goalName: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Money to $goalName",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter amount to add",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amount = it
                            errorMessage = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00") },
                    leadingIcon = { Text("₹", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    singleLine = true,
                    isError = errorMessage != null
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    when {
                        amount.isBlank() || amountValue == null || amountValue <= 0 -> {
                            errorMessage = "Please enter a valid amount"
                        }
                        else -> {
                            onConfirm(amountValue)
                        }
                    }
                }
            ) {
                Text("Add", color = Color(0xFF3F3D56))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getGoalIcon(icon: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (icon) {
        "home" -> Icons.Default.Home
        "flight" -> Icons.Default.Flight
        "emergency" -> Icons.Default.Favorite
        "car" -> Icons.Default.DirectionsCar
        "education" -> Icons.Default.School
        "vacation" -> Icons.Default.BeachAccess
        else -> Icons.Default.Flag
    }
}