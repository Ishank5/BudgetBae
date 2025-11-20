package com.humblecoders.fintrack

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.humblecoders.fintrack.ui.theme.BudgetBaeDarkGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionScreen(
    onDismiss: () -> Unit,
    onSubscriptionAdded: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedBillingCycle by remember { mutableStateOf("monthly") }
    var selectedCategory by remember { mutableStateOf("Entertainment") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val categories = listOf(
        "Entertainment",
        "Productivity",
        "Fitness",
        "Music",
        "Storage",
        "Software",
        "News",
        "Other"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Subscription",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            Text(
                text = "Subscription Name",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Netflix Premium") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BudgetBaeDarkGreen,
                    focusedLabelColor = BudgetBaeDarkGreen,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Amount",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BudgetBaeDarkGreen,
                    focusedLabelColor = BudgetBaeDarkGreen,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Billing Cycle",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BillingCycleChip(
                    label = "Monthly",
                    isSelected = selectedBillingCycle == "monthly",
                    onClick = { selectedBillingCycle = "monthly" },
                    modifier = Modifier.weight(1f)
                )

                BillingCycleChip(
                    label = "Annual",
                    isSelected = selectedBillingCycle == "annual",
                    onClick = { selectedBillingCycle = "annual" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Category",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            categories.chunked(4).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { category ->
                        CategoryChipSmall(
                            label = category,
                            isSelected = selectedCategory == category,
                            onClick = {
                                selectedCategory = category
                                errorMessage = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(4 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    when {
                        name.isBlank() -> {
                            errorMessage = "Please enter subscription name"
                        }
                        amount.isBlank() || amount.toDoubleOrNull() == null || amount.toDouble() <= 0 -> {
                            errorMessage = "Please enter a valid amount"
                        }
                        else -> {
                            isLoading = true
                            scope.launch {
                                val result = FirebaseHelper.addSubscription(
                                    name = name,
                                    amount = amount.toDouble(),
                                    billingCycle = selectedBillingCycle,
                                    category = selectedCategory
                                )

                                isLoading = false

                                result.onSuccess {
                                    onSubscriptionAdded()
                                    onDismiss()
                                }.onFailure { exception ->
                                    errorMessage = exception.message ?: "Failed to add subscription"
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BudgetBaeDarkGreen
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Add Subscription",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun BillingCycleChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick)
            .background(
                if (isSelected) BudgetBaeDarkGreen else Color.White,
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (isSelected) Color.White else BudgetBaeDarkGreen,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun CategoryChipSmall(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clickable(onClick = onClick)
            .background(
                if (isSelected) BudgetBaeDarkGreen else Color.White,
                RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) Color.White else BudgetBaeDarkGreen,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}