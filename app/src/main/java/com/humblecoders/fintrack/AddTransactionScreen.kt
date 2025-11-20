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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.humblecoders.fintrack.ui.theme.BudgetBaeDarkGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    type: String, // "expense" or "income"
    onDismiss: () -> Unit,
    onTransactionAdded: () -> Unit,
    onTransactionAddedWithCategory: (String, String) -> Unit = { _, _ -> } // type, category
) {
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    val categories = if (type == "expense") {
        listOf(
            Category("Grocery", Icons.Default.ShoppingCart),
            Category("Transport", Icons.Default.DirectionsCar),
            Category("Food", Icons.Default.Restaurant),
            Category("Shopping", Icons.Default.ShoppingBag),
            Category("Entertainment", Icons.Default.Movie),
            Category("Bills", Icons.Default.Receipt),
            Category("Health", Icons.Default.LocalHospital),
            Category("Other", Icons.Default.Category)
        )
    } else {
        listOf(
            Category("Salary", Icons.Default.AttachMoney),
            Category("Business", Icons.Default.Business),
            Category("Investment", Icons.Default.TrendingUp),
            Category("Gift", Icons.Default.CardGiftcard),
            Category("Other", Icons.Default.Category)
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (type == "expense") "Add Expense" else "Add Income",
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
                leadingIcon = { Text("₹", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BudgetBaeDarkGreen,
                    focusedLabelColor = BudgetBaeDarkGreen,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )
            
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { category ->
                        CategoryItem(
                            category = category,
                            isSelected = selectedCategory == category.name,
                            onClick = { 
                                selectedCategory = category.name
                                errorMessage = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(4 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Description (Optional)",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add a note") },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BudgetBaeDarkGreen,
                    focusedLabelColor = BudgetBaeDarkGreen,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )
            
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
                        amount.isBlank() || amount.toDoubleOrNull() == null || amount.toDouble() <= 0 -> {
                            errorMessage = "Please enter a valid amount"
                        }
                        selectedCategory.isBlank() -> {
                            errorMessage = "Please select a category"
                        }
                        else -> {
                            isLoading = true
                            scope.launch {
                                val result = FirebaseHelper.addTransaction(
                                    amount = amount.toDouble(),
                                    type = type,
                                    category = selectedCategory,
                                    description = description
                                )
                                
                                isLoading = false
                                
                                result.onSuccess {
                                    onTransactionAdded()
                                    onTransactionAddedWithCategory(type, selectedCategory)
                                    onDismiss()
                                }.onFailure { exception ->
                                    errorMessage = exception.message ?: "Failed to add transaction"
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
                        text = if (type == "expense") "Add Expense" else "Add Income",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
            .background(
                if (isSelected) BudgetBaeDarkGreen else Color.White,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            category.icon,
            contentDescription = category.name,
            tint = if (isSelected) Color.White else BudgetBaeDarkGreen,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name,
            fontSize = 11.sp,
            color = if (isSelected) Color.White else BudgetBaeDarkGreen,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

data class Category(
    val name: String,
    val icon: ImageVector
)
