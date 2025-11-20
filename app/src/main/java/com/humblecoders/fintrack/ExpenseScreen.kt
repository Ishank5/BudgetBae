package com.humblecoders.fintrack

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import com.humblecoders.fintrack.ui.theme.BudgetBaeDarkGreen
import com.humblecoders.fintrack.ui.theme.BudgetBaeTeal
import com.humblecoders.fintrack.ui.theme.BudgetBaeGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    refreshTrigger: Int = 0,
    onNavigateToHome: () -> Unit = {},
    onNavigateToIncome: () -> Unit = {},
    onNavigateToSubscriptions: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {}
) {
    var allTransactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var filteredTransactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Date Range") }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    fun filterTransactionsByMonth() {
        val calendar = Calendar.getInstance()
        calendar.time = selectedMonth.time
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfMonth = calendar.timeInMillis

        filteredTransactions = allTransactions.filter { it.timestamp in startOfMonth..endOfMonth }
    }

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        FirebaseHelper.getAllTransactions().onSuccess { transactions ->
            allTransactions = transactions.filter { it.type == "expense" }
            filterTransactionsByMonth()
        }
        isLoading = false
    }

    LaunchedEffect(selectedMonth) {
        filterTransactionsByMonth()
    }

    val totalExpenses = filteredTransactions.sumOf { it.amount }

    val categoryData = filteredTransactions.groupBy { it.category }
        .mapValues { (_, trans) -> trans.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Expenses",
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
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BudgetBaeDarkGreen,
                        selectedTextColor = BudgetBaeDarkGreen,
                        indicatorColor = BudgetBaeDarkGreen.copy(alpha = 0.2f)
                    )
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
                            .fillMaxHeight()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BudgetBaeDarkGreen)
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Total Expenses",
                                fontSize = 14.sp,
                                color = Color(0xFF999999)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Rs. ${String.format("%.2f", totalExpenses)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = BudgetBaeDarkGreen
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            TextButton(
                                onClick = { showMonthPicker = true },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = monthFormat.format(selectedMonth.time),
                                    fontSize = 14.sp,
                                    color = Color(0xFF999999)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color(0xFF999999),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                if (categoryData.isNotEmpty()) {
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
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                PieChart(
                                    data = categoryData,
                                    modifier = Modifier
                                        .size(200.dp)
                                        .padding(20.dp)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                categoryData.take(4).forEach { (category, amount) ->
                                    CategoryLegendItem(
                                        category = category,
                                        amount = amount,
                                        percentage = (amount / totalExpenses * 100).toInt(),
                                        color = getCategoryColor(category)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == "Date Range",
                            onClick = { selectedFilter = "Date Range" },
                            label = { Text("Date Range") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BudgetBaeDarkGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == "Merchant",
                            onClick = { selectedFilter = "Merchant" },
                            label = { Text("Merchant") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BudgetBaeDarkGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == "Tags",
                            onClick = { selectedFilter = "Tags" },
                            label = { Text("Tags") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BudgetBaeDarkGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                items(filteredTransactions) { transaction ->
                    TransactionItem(transaction)
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    if (showMonthPicker) {
        MonthPickerDialog(
            currentMonth = selectedMonth,
            onMonthSelected = { month ->
                selectedMonth = month
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }
}

@Composable
fun CategoryLegendItem(
    category: String,
    amount: Double,
    percentage: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$category $percentage%",
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
fun PieChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.second }

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val centerX = size.width / 2
        val centerY = size.height / 2
        val strokeWidth = radius * 0.4f

        var startAngle = -90f

        data.forEach { (category, value) ->
            val sweepAngle = (value / total * 360).toFloat()
            val color = getCategoryColor(category)

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(
                    centerX - radius + strokeWidth / 2,
                    centerY - radius + strokeWidth / 2
                ),
                size = Size(
                    (radius - strokeWidth / 2) * 2,
                    (radius - strokeWidth / 2) * 2
                ),
                style = Stroke(width = strokeWidth)
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
fun MonthPickerDialog(
    currentMonth: Calendar,
    onMonthSelected: (Calendar) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedYear by remember { mutableStateOf(currentMonth.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(currentMonth.get(Calendar.MONTH)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Month") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear-- }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Year")
                    }
                    Text(
                        text = selectedYear.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { selectedYear++ }) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Year")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val months = listOf(
                    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
                )

                months.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEachIndexed { index, monthName ->
                            val monthIndex = months.indexOf(monthName)
                            FilterChip(
                                selected = selectedMonth == monthIndex,
                                onClick = { selectedMonth = monthIndex },
                                label = { Text(monthName) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BudgetBaeDarkGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.YEAR, selectedYear)
                    calendar.set(Calendar.MONTH, selectedMonth)
                    onMonthSelected(calendar)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "grocery", "groceries" -> BudgetBaeDarkGreen
        "food & dining", "food", "starbucks" -> BudgetBaeTeal
        "transportation", "uber", "transport" -> BudgetBaeGold
        "shopping", "mall" -> BudgetBaeTeal.copy(alpha = 0.6f)
        "entertainment", "netflix" -> BudgetBaeGold.copy(alpha = 0.7f)
        "bills" -> BudgetBaeDarkGreen.copy(alpha = 0.7f)
        "health" -> Color(0xFF4CAF50)
        else -> BudgetBaeTeal.copy(alpha = 0.4f)
    }
}