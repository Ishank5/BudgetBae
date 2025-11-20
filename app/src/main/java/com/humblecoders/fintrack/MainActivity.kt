package com.humblecoders.fintrack

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.humblecoders.fintrack.ui.theme.FinTrackTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var sharedImageUriState = mutableStateOf<Uri?>(null)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            android.util.Log.d("MainActivity", "Notification permission granted")
        } else {
            android.util.Log.d("MainActivity", "Notification permission denied")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        
        // Handle share intent
        handleShareIntent(intent)
        
        // Create notification channel for balance notifications
        NotificationHelper.createNotificationChannel(this)
        
        // Request notification permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    android.util.Log.d("MainActivity", "Notification permission already granted")
                }
                else -> {
                    android.util.Log.d("MainActivity", "Requesting notification permission")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            FinTrackTheme {
                FinTrackApp(sharedImageUriState = sharedImageUriState)
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the intent
        handleShareIntent(intent)
    }
    
    private fun handleShareIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            }
            sharedImageUriState.value = imageUri
            android.util.Log.d("MainActivity", "Received shared image: $imageUri")
        }
    }
}

@Composable
fun FinTrackApp(sharedImageUriState: MutableState<Uri?>) {
    val navController = rememberNavController()
    var refreshHome by remember { mutableStateOf(0) }
    var showSplash by remember { mutableStateOf(true) }

    val startDestination = if (FirebaseHelper.getCurrentUserId() != null) {
        "home"
    } else {
        "get_started"
    }

    if (showSplash) {
        SplashScreen(
            onNavigate = {
                showSplash = false
            }
        )
    } else {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable("get_started") {
                GetStartedScreen(
                    onGetStartedClick = {
                        navController.navigate("welcome") {
                            popUpTo("get_started") { inclusive = true }
                        }
                    }
                )
            }

            composable("welcome") {
                WelcomeScreen(
                onSignUpClick = {
                    navController.navigate("signup")
                },
                onLogInClick = {
                    navController.navigate("login")
                }
                )
            }

            composable("login") {
                LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSignUpClick = {
                    navController.navigate("signup") {
                        popUpTo("login") { inclusive = true }
                    }
                }
                )
            }

            composable("signup") {
                SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onLogInClick = {
                    navController.navigate("login") {
                        popUpTo("signup") { inclusive = true }
                    }
                }
                )
            }

            composable("home") {
                var showAddExpense by remember { mutableStateOf(false) }
                var showAddIncome by remember { mutableStateOf(false) }
                val context = LocalContext.current
                var pendingImageUri by remember { mutableStateOf<Uri?>(null) }
                
                // Check if there's a pending shared image
                LaunchedEffect(sharedImageUriState.value) {
                    sharedImageUriState.value?.let { uri ->
                        // First, scan the image to detect transaction type
                        try {
                            val textResult = ReceiptScanner.extractTextFromUri(context, uri)
                            textResult.onSuccess { extractedText ->
                                val parsed = TransactionParser.parseTransaction(extractedText)
                                
                                // Open the appropriate screen based on detected type
                                pendingImageUri = uri
                                if (parsed.type == "income") {
                                    showAddIncome = true
                                    android.util.Log.d("MainActivity", "Detected income, opening income screen")
                                } else {
                                    // Default to expense if type is null or expense
                                    showAddExpense = true
                                    android.util.Log.d("MainActivity", "Detected expense or unknown, opening expense screen")
                                }
                            }.onFailure {
                                // If scanning fails, default to expense screen
                                android.util.Log.w("MainActivity", "Failed to scan image: ${it.message}")
                                pendingImageUri = uri
                                showAddExpense = true
                            }
                        } catch (e: Exception) {
                            // If scanning fails, default to expense screen
                            android.util.Log.w("MainActivity", "Error scanning image: ${e.message}")
                            pendingImageUri = uri
                            showAddExpense = true
                        }
                        sharedImageUriState.value = null // Clear after using
                    }
                }

                HomeScreen(
                onAddExpenseClick = { showAddExpense = true },
                onAddIncomeClick = { showAddIncome = true },
                refreshTrigger = refreshHome,
                onNavigateToExpenses = {
                    navController.navigate("expenses")
                },
                onNavigateToIncome = {
                    navController.navigate("income")
                },
                onNavigateToSubscriptions = {
                    navController.navigate("subscriptions")
                },
                onNavigateToGoals = {
                    navController.navigate("goals")
                }
            )

            if (showAddExpense) {
                AddTransactionScreen(
                    type = "expense",
                    onDismiss = { 
                        showAddExpense = false
                        pendingImageUri = null // Clear any pending image
                    },
                    onTransactionAdded = {
                        refreshHome++
                    },
                    onTransactionAddedWithCategory = { type, category ->
                        ToastHelper.showTransactionToast(context, type, category)
                    },
                    initialImageUri = pendingImageUri
                    )
                }

                if (showAddIncome) {
                    AddTransactionScreen(
                    type = "income",
                    onDismiss = { 
                        showAddIncome = false
                        pendingImageUri = null // Clear any pending image
                    },
                    onTransactionAdded = {
                        refreshHome++
                    },
                    onTransactionAddedWithCategory = { type, category ->
                        ToastHelper.showTransactionToast(context, type, category)
                    },
                    initialImageUri = pendingImageUri
                    )
                }
            }

            composable("expenses") {
                ExpenseScreen(
                refreshTrigger = refreshHome,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToIncome = {
                    navController.navigate("income") {
                        popUpTo("expenses") { inclusive = true }
                    }
                },
                onNavigateToSubscriptions = {
                    navController.navigate("subscriptions") {
                        popUpTo("expenses") { inclusive = true }
                    }
                },
                onNavigateToGoals = {
                    navController.navigate("goals") {
                        popUpTo("expenses") { inclusive = true }
                    }
                }
                )
            }

            composable("income") {
                IncomeScreen(
                refreshTrigger = refreshHome,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToExpenses = {
                    navController.navigate("expenses") {
                        popUpTo("income") { inclusive = true }
                    }
                },
                onNavigateToSubscriptions = {
                    navController.navigate("subscriptions") {
                        popUpTo("income") { inclusive = true }
                    }
                },
                onNavigateToGoals = {
                    navController.navigate("goals") {
                        popUpTo("income") { inclusive = true }
                    }
                }
                )
            }

            composable("subscriptions") {
                var showAddSubscription by remember { mutableStateOf(false) }

                SubscriptionScreen(
                refreshTrigger = refreshHome,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToExpenses = {
                    navController.navigate("expenses") {
                        popUpTo("subscriptions") { inclusive = true }
                    }
                },
                onNavigateToIncome = {
                    navController.navigate("income") {
                        popUpTo("subscriptions") { inclusive = true }
                    }
                },
                onNavigateToGoals = {
                    navController.navigate("goals") {
                        popUpTo("subscriptions") { inclusive = true }
                    }
                },
                onAddSubscriptionClick = { showAddSubscription = true                 }
                )

                if (showAddSubscription) {
                    AddSubscriptionScreen(
                    onDismiss = { showAddSubscription = false },
                    onSubscriptionAdded = {
                        refreshHome++
                    }
                    )
                }
            }

            composable("goals") {
                var showAddGoal by remember { mutableStateOf(false) }

                GoalsScreen(
                refreshTrigger = refreshHome,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToExpenses = {
                    navController.navigate("expenses") {
                        popUpTo("goals") { inclusive = true }
                    }
                },
                onNavigateToIncome = {
                    navController.navigate("income") {
                        popUpTo("goals") { inclusive = true }
                    }
                },
                onNavigateToSubscriptions = {
                    navController.navigate("subscriptions") {
                        popUpTo("goals") { inclusive = true }
                    }
                },
                onAddGoalClick = { showAddGoal = true                 }
                )

                if (showAddGoal) {
                    AddGoalScreen(
                    onDismiss = { showAddGoal = false },
                    onGoalAdded = {
                        refreshHome++
                    }
                    )
                }
            }
        }
    }
}