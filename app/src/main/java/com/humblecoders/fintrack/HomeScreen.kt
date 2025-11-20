package com.humblecoders.fintrack

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.random.Random
import com.humblecoders.fintrack.ui.theme.BudgetBaeDarkGreen
import com.humblecoders.fintrack.ui.theme.BudgetBaeGold
import com.humblecoders.fintrack.ui.theme.BudgetBaeTeal

@Composable
fun HomeScreen(
    onAddExpenseClick: () -> Unit,
    onAddIncomeClick: () -> Unit,
    refreshTrigger: Int = 0,
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToIncome: () -> Unit = {},
    onNavigateToSubscriptions: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {}
) {
    var balance by remember { mutableStateOf(0.0) }
    var previousBalance by remember { mutableStateOf(0.0) }
    var dailySavings by remember { mutableStateOf<List<Pair<String, Double>>>(emptyList()) }
    var weeklySavings by remember { mutableStateOf(0.0) }
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var smartSuggestion by remember { mutableStateOf<Pair<String, String>?>(null) }
    var isInitialLoad by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(refreshTrigger) {
        isLoading = true

        launch {
            FirebaseHelper.getBalance().onSuccess {
                val newBalance = it
                android.util.Log.d("HomeScreen", "Balance loaded: previous=$previousBalance, new=$newBalance, isInitialLoad=$isInitialLoad")
                
                // Only check notifications if this is not the initial load and balance actually changed
                if (!isInitialLoad && previousBalance != newBalance) {
                    android.util.Log.d("HomeScreen", "Balance changed, checking notifications...")
                    NotificationHelper.checkAndShowBalanceNotifications(
                        context = context,
                        currentBalance = newBalance,
                        previousBalance = previousBalance
                    )
                }
                // Update previous balance before setting new balance
                if (isInitialLoad) {
                    previousBalance = newBalance
                    android.util.Log.d("HomeScreen", "Initial load, setting previousBalance to $newBalance")
                } else {
                    previousBalance = balance
                    android.util.Log.d("HomeScreen", "Updating previousBalance from $balance to $newBalance")
                }
                balance = newBalance
                isInitialLoad = false
                println("DEBUG: Balance loaded: $it")
            }.onFailure {
                println("DEBUG: Failed to load balance: ${it.message}")
            }
        }

        launch {
            FirebaseHelper.getDailySavings(7).onSuccess {
                dailySavings = it
                println("DEBUG: Daily savings loaded: ${it.size} days")
            }.onFailure {
                println("DEBUG: Failed to load daily savings: ${it.message}")
            }
        }

        launch {
            FirebaseHelper.getWeeklySavings().onSuccess {
                weeklySavings = it
                println("DEBUG: Weekly savings loaded: $it")
            }.onFailure {
                println("DEBUG: Failed to load weekly savings: ${it.message}")
            }
        }

        launch {
            FirebaseHelper.getRecentTransactions(50).onSuccess {
                transactions = it
                println("DEBUG: Transactions loaded: ${it.size} transactions")
                
                // Calculate smart suggestion based on most spent category
                val expenseTransactions = it.filter { transaction -> transaction.type == "expense" }
                if (expenseTransactions.isNotEmpty()) {
                    val categorySpending = expenseTransactions
                        .groupBy { it.category.lowercase() }
                        .mapValues { (_, trans) -> trans.sumOf { it.amount } }
                    
                    val mostSpentCategory = categorySpending.maxByOrNull { it.value }?.key
                    
                    if (mostSpentCategory != null) {
                        val suggestion = getRandomSuggestionForCategory(mostSpentCategory)
                        smartSuggestion = Pair(mostSpentCategory, suggestion)
                    }
                } else {
                    smartSuggestion = null
                }
            }.onFailure {
                println("DEBUG: Failed to load transactions: ${it.message}")
            }
        }

        isLoading = false
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                contentColor = BudgetBaeDarkGreen
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BudgetBaeDarkGreen,
                        selectedTextColor = BudgetBaeDarkGreen,
                        indicatorColor = BudgetBaeDarkGreen.copy(alpha = 0.2f)
                    )
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
                    selected = false,
                    onClick = onNavigateToGoals
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Balance",
                            fontSize = 16.sp,
                            color = Color(0xFF666666)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "₹${String.format("%.2f", balance)}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = BudgetBaeDarkGreen
                        )

                        val percentageChange = if (previousBalance != 0.0) {
                            ((balance - previousBalance) / previousBalance) * 100
                        } else 0.0

                        Text(
                            text = if (percentageChange >= 0)
                                "+₹${String.format("%.2f", balance - previousBalance)} (${String.format("%.1f", percentageChange)}%)"
                            else
                                "-₹${String.format("%.2f", previousBalance - balance)} (${String.format("%.1f", percentageChange)}%)",
                            fontSize = 14.sp,
                            color = if (percentageChange >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onAddExpenseClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = null,
                                    tint = BudgetBaeDarkGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Add Expense",
                                    color = BudgetBaeDarkGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Button(
                                onClick = onAddIncomeClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = BudgetBaeDarkGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Add Income",
                                    color = BudgetBaeDarkGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Daily Overview",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BudgetBaeDarkGreen
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            if (dailySavings.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No data available",
                                        color = Color(0xFF999999),
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    val maxValue = dailySavings.maxOfOrNull { abs(it.second) } ?: 1.0

                                    dailySavings.forEach { (date, value) ->
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Bottom
                                        ) {
                                            val normalizedHeight = if (maxValue > 0.0) {
                                                ((abs(value) / maxValue) * 100).dp
                                            } else {
                                                10.dp
                                            }

                                            val barHeight = normalizedHeight.coerceAtLeast(10.dp)

                                            Box(
                                                modifier = Modifier
                                                    .width(28.dp)
                                                    .height(barHeight)
                                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                    .background(
                                                        if (value >= 0) BudgetBaeDarkGreen.copy(alpha = 0.6f) else Color(0xFFFFCDD2)
                                                    )
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = date.split(" ").lastOrNull() ?: "",
                                                fontSize = 11.sp,
                                                color = Color(0xFF999999)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Weekly Savings",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BudgetBaeDarkGreen
                                    )
                                    Text(
                                        text = "Goal: ₹10,000",
                                        fontSize = 12.sp,
                                        color = Color(0xFF999999),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        progress = {
                                            val progress = (weeklySavings / 10000.0).coerceIn(0.0, 1.0)
                                            progress.toFloat()
                                        },
                                        modifier = Modifier.size(60.dp),
                                        color = BudgetBaeDarkGreen,
                                        strokeWidth = 6.dp,
                                        trackColor = Color(0xFFE0E0E0)
                                    )
                                    Text(
                                        text = "${((weeklySavings / 10000.0) * 100).toInt()}%",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BudgetBaeDarkGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LinearProgressIndicator(
                                progress = {
                                    val progress = (weeklySavings / 10000.0).coerceIn(0.0, 1.0)
                                    progress.toFloat()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = BudgetBaeDarkGreen,
                                trackColor = Color(0xFFE0E0E0)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .border(
                                width = 2.dp,
                                color = BudgetBaeGold.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            BudgetBaeTeal.copy(alpha = 0.08f),
                                            BudgetBaeGold.copy(alpha = 0.05f)
                                        )
                                    )
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
//                                Box(
//                                    modifier = Modifier
//                                        .size(56.dp)
//                                        .clip(CircleShape)
//                                        .background(
//                                            brush = Brush.radialGradient(
//                                                colors = listOf(
//                                                    BudgetBaeGold.copy(alpha = 0.2f),
//                                                    BudgetBaeDarkGreen.copy(alpha = 0.1f)
//                                                )
//                                            )
//                                        )
//                                        .border(
//                                            width = 2.dp,
//                                            color = BudgetBaeGold.copy(alpha = 0.4f),
//                                            shape = CircleShape
//                                        ),
//                                    contentAlignment = Alignment.Center
//                                ) {
////                                    Icon(
////                                        Icons.Default.Lightbulb,
////                                        contentDescription = null,
////                                        tint = BudgetBaeGold,
////                                        modifier = Modifier.size(28.dp)
////                                    )
//                                }

                               // Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "💡",
                                            fontSize = 18.sp
                                        )
                                        Text(
                                            text = "Smart Suggestion",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BudgetBaeDarkGreen
                                        )
                                    }
                                    if (smartSuggestion != null) {
                                        val (category, suggestion) = smartSuggestion!!
                                        Text(
                                            text = suggestion,
                                            fontSize = 13.sp,
                                            color = Color(0xFF333333),
                                            modifier = Modifier.padding(top = 6.dp),
                                            lineHeight = 18.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    } else {
                                        Text(
                                            text = "Start tracking your expenses to get personalized savings tips!",
                                            fontSize = 13.sp,
                                            color = Color(0xFF666666),
                                            modifier = Modifier.padding(top = 6.dp),
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = "Recent Transactions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BudgetBaeDarkGreen
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                if (transactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No transactions yet",
                                color = Color(0xFF999999),
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(transactions) { transaction ->
                        TransactionItem(transaction)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val categoryIcon = when (transaction.category.lowercase()) {
        "grocery", "groceries" -> Icons.Default.ShoppingCart
        "uber", "transportation", "transport" -> Icons.Default.DirectionsCar
        "starbucks", "food", "dining" -> Icons.Default.Restaurant
        else -> Icons.Default.ShoppingBag
    }

    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val date = Date(transaction.timestamp)

    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val transactionCal = Calendar.getInstance().apply { time = date }

    val dateText = when {
        today.get(Calendar.DAY_OF_YEAR) == transactionCal.get(Calendar.DAY_OF_YEAR) &&
                today.get(Calendar.YEAR) == transactionCal.get(Calendar.YEAR) -> "Today"
        yesterday.get(Calendar.DAY_OF_YEAR) == transactionCal.get(Calendar.DAY_OF_YEAR) &&
                yesterday.get(Calendar.YEAR) == transactionCal.get(Calendar.YEAR) -> "Yesterday"
        else -> dateFormat.format(date)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3F2F7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                categoryIcon,
                contentDescription = null,
                tint = BudgetBaeDarkGreen,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.category.replaceFirstChar { it.uppercase() },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = BudgetBaeDarkGreen
            )
            Text(
                text = transaction.description.ifBlank { transaction.type.replaceFirstChar { it.uppercase() } },
                fontSize = 12.sp,
                color = Color(0xFF999999)
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${if (transaction.type == "expense") "-" else "+"}Rs. ${String.format("%.2f", transaction.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (transaction.type == "expense") Color(0xFFF44336) else Color(0xFF4CAF50)
            )
            Text(
                text = dateText,
                fontSize = 12.sp,
                color = Color(0xFF999999)
            )
        }
    }
}

// Smart Suggestions Data
private val smartSuggestions = mapOf(
    "grocery" to listOf(
        "Buy in Bulk: Purchase non-perishable staples (rice, pasta, cleaning supplies) in bulk quantities to reduce the cost-per-unit.",
        "Switch to Generic: Opt for store-brand or generic products instead of premium name brands; the quality is often identical for a lower price.",
        "Meal Prep: Plan your weekly meals before shopping to avoid impulse buys and reduce food waste from unused ingredients."
    ),
    "groceries" to listOf(
        "Buy in Bulk: Purchase non-perishable staples (rice, pasta, cleaning supplies) in bulk quantities to reduce the cost-per-unit.",
        "Switch to Generic: Opt for store-brand or generic products instead of premium name brands; the quality is often identical for a lower price.",
        "Meal Prep: Plan your weekly meals before shopping to avoid impulse buys and reduce food waste from unused ingredients."
    ),
    "transport" to listOf(
        "Route Optimization: Combine multiple errands into a single trip to save on fuel consumption and time.",
        "Alternative Commute: Consider using public transit, carpooling, or biking for short-distance travel to save on fuel and parking fees.",
        "Maintenance: Ensure tires are properly inflated and the engine is tuned; poor vehicle maintenance significantly increases fuel consumption."
    ),
    "transportation" to listOf(
        "Route Optimization: Combine multiple errands into a single trip to save on fuel consumption and time.",
        "Alternative Commute: Consider using public transit, carpooling, or biking for short-distance travel to save on fuel and parking fees.",
        "Maintenance: Ensure tires are properly inflated and the engine is tuned; poor vehicle maintenance significantly increases fuel consumption."
    ),
    "uber" to listOf(
        "Route Optimization: Combine multiple errands into a single trip to save on fuel consumption and time.",
        "Alternative Commute: Consider using public transit, carpooling, or biking for short-distance travel to save on fuel and parking fees.",
        "Maintenance: Ensure tires are properly inflated and the engine is tuned; poor vehicle maintenance significantly increases fuel consumption."
    ),
    "food" to listOf(
        "Limit Frequency: Set a rule to limit dining out or ordering in to weekends or special occasions only.",
        "Skip the Drinks: Avoid ordering sugary drinks or alcohol at restaurants, as these items often have the highest markup percentage.",
        "Loyalty Programs: Use restaurant apps or loyalty programs to earn points and access exclusive discounts on your orders."
    ),
    "food & dining" to listOf(
        "Limit Frequency: Set a rule to limit dining out or ordering in to weekends or special occasions only.",
        "Skip the Drinks: Avoid ordering sugary drinks or alcohol at restaurants, as these items often have the highest markup percentage.",
        "Loyalty Programs: Use restaurant apps or loyalty programs to earn points and access exclusive discounts on your orders."
    ),
    "dining" to listOf(
        "Limit Frequency: Set a rule to limit dining out or ordering in to weekends or special occasions only.",
        "Skip the Drinks: Avoid ordering sugary drinks or alcohol at restaurants, as these items often have the highest markup percentage.",
        "Loyalty Programs: Use restaurant apps or loyalty programs to earn points and access exclusive discounts on your orders."
    ),
    "starbucks" to listOf(
        "Limit Frequency: Set a rule to limit dining out or ordering in to weekends or special occasions only.",
        "Skip the Drinks: Avoid ordering sugary drinks or alcohol at restaurants, as these items often have the highest markup percentage.",
        "Loyalty Programs: Use restaurant apps or loyalty programs to earn points and access exclusive discounts on your orders."
    ),
    "shopping" to listOf(
        "The 24-Hour Rule: Wait 24 hours before purchasing any non-essential item to determine if it is a 'need' or an impulse 'want'.",
        "Off-Season Buying: Buy seasonal items (like winter coats or air conditioners) during the off-season when prices are at their lowest.",
        "Comparison Tools: Use browser extensions or price comparison websites to ensure you are getting the best deal before checking out."
    ),
    "mall" to listOf(
        "The 24-Hour Rule: Wait 24 hours before purchasing any non-essential item to determine if it is a 'need' or an impulse 'want'.",
        "Off-Season Buying: Buy seasonal items (like winter coats or air conditioners) during the off-season when prices are at their lowest.",
        "Comparison Tools: Use browser extensions or price comparison websites to ensure you are getting the best deal before checking out."
    ),
    "entertainment" to listOf(
        "Subscription Audit: Review your streaming and digital subscriptions; cancel any service you haven't used in the last 30 days.",
        "Free Events: Look for free community events, open museum days, or local parks as alternatives to paid venues.",
        "Group Sharing: Utilize family plans or group discounts for digital services and event tickets to split the cost with friends."
    ),
    "netflix" to listOf(
        "Subscription Audit: Review your streaming and digital subscriptions; cancel any service you haven't used in the last 30 days.",
        "Free Events: Look for free community events, open museum days, or local parks as alternatives to paid venues.",
        "Group Sharing: Utilize family plans or group discounts for digital services and event tickets to split the cost with friends."
    ),
    "bills" to listOf(
        "Energy Efficiency: Switch to LED bulbs and unplug 'vampire electronics' (devices that draw power when off) to lower electricity bills.",
        "Negotiate Rates: Call your internet or insurance providers annually to ask about current promotions or cheaper plan options.",
        "Usage Alerts: Set up text or email alerts to track your data or energy usage mid-month to avoid overage charges."
    ),
    "health" to listOf(
        "Generic Meds: Ask your pharmacist or doctor if a generic version of your prescription is available at a lower cost.",
        "Preventative Care: Prioritize annual check-ups and preventative screenings to catch issues early and avoid expensive emergency treatments.",
        "Fitness Alternatives: Consider low-cost alternatives to high-end gyms, such as home workout apps, running, or community recreational centers."
    ),
    "other" to listOf(
        "The 'Cash' Method: Withdraw a fixed amount of cash for miscellaneous spending; once the cash is gone, spending stops for the month.",
        "Categorization Review: Review items in this category frequently; if a specific expense keeps appearing, create a dedicated budget category for it.",
        "Emergency Fund: Direct a portion of undefined spending into a high-yield savings account to build a safety net for unexpected costs."
    )
)

private fun getRandomSuggestionForCategory(category: String): String {
    val normalizedCategory = category.lowercase()
    
    // Try exact match first
    val suggestions = smartSuggestions[normalizedCategory]
        ?: smartSuggestions.entries.firstOrNull { (key, _) ->
            normalizedCategory.contains(key) || key.contains(normalizedCategory)
        }?.value
    
    return if (suggestions != null && suggestions.isNotEmpty()) {
        suggestions[Random.nextInt(suggestions.size)]
    } else {
        // Default suggestion if category not found
        "Review your spending in this category and look for opportunities to save money."
    }
}