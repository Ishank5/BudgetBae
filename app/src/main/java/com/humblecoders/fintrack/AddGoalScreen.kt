package com.humblecoders.fintrack

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.humblecoders.fintrack.ui.theme.BudgetBaeDarkGreen
import com.humblecoders.fintrack.ui.theme.BudgetBaeTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(
    onDismiss: () -> Unit,
    onGoalAdded: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("home") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val icons = listOf(
        GoalIcon("home", Icons.Default.Home, "Home"),
        GoalIcon("flight", Icons.Default.Flight, "Travel"),
        GoalIcon("car", Icons.Default.DirectionsCar, "Car"),
        GoalIcon("education", Icons.Default.School, "Education"),
        GoalIcon("vacation", Icons.Default.BeachAccess, "Vacation"),
        GoalIcon("emergency", Icons.Default.Favorite, "Emergency")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create New Goal",
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
                text = "Goal Name",
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
                placeholder = { Text("e.g. Dream House, Bali Trip") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BudgetBaeTeal,
                    focusedLabelColor = BudgetBaeTeal,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Target Amount",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = targetAmount,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        targetAmount = it
                        errorMessage = null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("0.00") },
                leadingIcon = { Text("₹", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BudgetBaeTeal,
                    focusedLabelColor = BudgetBaeTeal,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Choose Icon",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            icons.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { goalIcon ->
                        GoalIconItem(
                            goalIcon = goalIcon,
                            isSelected = selectedIcon == goalIcon.id,
                            onClick = {
                                selectedIcon = goalIcon.id
                                errorMessage = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
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
                            errorMessage = "Please enter goal name"
                        }
                        targetAmount.isBlank() || targetAmount.toDoubleOrNull() == null || targetAmount.toDouble() <= 0 -> {
                            errorMessage = "Please enter a valid target amount"
                        }
                        else -> {
                            isLoading = true
                            scope.launch {
                                val result = FirebaseHelper.addGoal(
                                    name = name,
                                    targetAmount = targetAmount.toDouble(),
                                    icon = selectedIcon
                                )

                                isLoading = false

                                result.onSuccess {
                                    onGoalAdded()
                                    onDismiss()
                                }.onFailure { exception ->
                                    errorMessage = exception.message ?: "Failed to create goal"
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BudgetBaeTeal
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
                        text = "Create Goal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun GoalIconItem(
    goalIcon: GoalIcon,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
            .background(
                if (isSelected) BudgetBaeTeal else Color.White,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            goalIcon.icon,
            contentDescription = goalIcon.label,
            tint = if (isSelected) Color.White else BudgetBaeTeal,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = goalIcon.label,
            fontSize = 12.sp,
            color = if (isSelected) Color.White else BudgetBaeDarkGreen,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

data class GoalIcon(
    val id: String,
    val icon: ImageVector,
    val label: String
)