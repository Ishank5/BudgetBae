package com.humblecoders.fintrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.humblecoders.fintrack.ui.theme.BudgetBaeDarkGreen
import com.humblecoders.fintrack.ui.theme.BudgetBaeTeal
import com.humblecoders.fintrack.ui.theme.BudgetBaeGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    refreshTrigger: Int = 0,
    onNavigateToHome: () -> Unit = {},
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToIncome: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onAddSubscriptionClick: () -> Unit = {}
) {
    var subscriptions by remember { mutableStateOf<List<Subscription>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var sortBy by remember { mutableStateOf("Cost (High to Low)") }
    var showSortMenu by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun loadSubscriptions() {
        scope.launch {
            isLoading = true
            FirebaseHelper.getAllSubscriptions().onSuccess {
                subscriptions = when (sortBy) {
                    "Cost (High to Low)" -> it.sortedByDescending { sub -> sub.amount }
                    "Cost (Low to High)" -> it.sortedBy { sub -> sub.amount }
                    "Name (A-Z)" -> it.sortedBy { sub -> sub.name }
                    else -> it
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(refreshTrigger, sortBy) {
        loadSubscriptions()
    }

    val activeSubscriptions = subscriptions.filter { it.isActive }
    val totalMonthly = activeSubscriptions.filter { it.billingCycle == "monthly" }.sumOf { it.amount }
    val totalAnnual = activeSubscriptions.sumOf {
        if (it.billingCycle == "monthly") it.amount * 12 else it.amount
    }

    val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Subscription Manager",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSubscriptionClick,
                containerColor = BudgetBaeDarkGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Subscription")
            }
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
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BudgetBaeDarkGreen,
                        selectedTextColor = BudgetBaeDarkGreen,
                        indicatorColor = BudgetBaeDarkGreen.copy(alpha = 0.2f)
                    )
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
                            .background(Color.White)
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Total Monthly Spending",
                            fontSize = 14.sp,
                            color = Color(0xFF999999)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Rs. ${String.format("%.2f", totalMonthly)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = BudgetBaeDarkGreen
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Total Annual Spending",
                            fontSize = 14.sp,
                            color = Color(0xFF999999)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Rs. ${String.format("%.2f", totalAnnual)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = BudgetBaeDarkGreen
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            horizontalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val monthlyHeight = if (totalAnnual.toInt() == 0) {
                                    ((totalMonthly / (totalAnnual / 12)) * 100).dp.coerceAtLeast(20.dp)
                                } else 20.dp

                                Box(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(monthlyHeight)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(BudgetBaeDarkGreen)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Monthly",
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666)
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val annualHeight = 100.dp

                                Box(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(annualHeight)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(BudgetBaeTeal)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Annual",
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sort by:",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Box {
                            OutlinedButton(
                                onClick = { showSortMenu = true },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Text(
                                    text = sortBy,
                                    fontSize = 14.sp,
                                    color = BudgetBaeDarkGreen
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = BudgetBaeDarkGreen
                                )
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Cost (High to Low)") },
                                    onClick = {
                                        sortBy = "Cost (High to Low)"
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Cost (Low to High)") },
                                    onClick = {
                                        sortBy = "Cost (Low to High)"
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Name (A-Z)") },
                                    onClick = {
                                        sortBy = "Name (A-Z)"
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(subscriptions) { subscription ->
                    SubscriptionItem(
                        subscription = subscription,
                        isPaidThisMonth = subscription.lastPaidMonth == currentMonth,
                        onToggleChange = { isActive ->
                            scope.launch {
                                FirebaseHelper.updateSubscriptionStatus(subscription.id, isActive)
                                loadSubscriptions()
                            }
                        },
                        onPayClick = {
                            scope.launch {
                                FirebaseHelper.paySubscriptionForMonth(subscription)
                                loadSubscriptions()
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun SubscriptionItem(
    subscription: Subscription,
    isPaidThisMonth: Boolean,
    onToggleChange: (Boolean) -> Unit,
    onPayClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    getSubscriptionIcon(subscription.name),
                    contentDescription = null,
                    tint = Color(0xFF3F3D56),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subscription.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF3F3D56)
                )

                Text(
                    text = "Rs. ${String.format("%.0f", subscription.amount)}/${subscription.billingCycle}",
                    fontSize = 14.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (subscription.isActive) {
                    if (isPaidThisMonth) {
                        Text(
                            text = "Paid",
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        TextButton(
                            onClick = onPayClick,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Pay for this month",
                                fontSize = 12.sp,
                                color = BudgetBaeDarkGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Switch(
                checked = subscription.isActive,
                onCheckedChange = onToggleChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = BudgetBaeDarkGreen,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFE0E0E0)
                )
            )
        }
    }
}

fun getSubscriptionIcon(name: String): ImageVector {
    return when (name.lowercase()) {
        "netflix", "netflix premium" -> Icons.Default.Tv
        "amazon prime", "amazon" -> Icons.Default.ShoppingBag
        "spotify", "spotify premium" -> Icons.Default.MusicNote
        "motiff subscription", "motiff" -> Icons.Default.Brush
        "youtube", "youtube premium" -> Icons.Default.PlayArrow
        "apple music" -> Icons.Default.MusicNote
        else -> Icons.Default.Subscriptions
    }
}