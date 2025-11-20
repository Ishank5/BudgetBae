# Budget BAE - FinTrack

**Your Money Wingmate** - Making money management simple, fun, and stress-free.

Budget BAE is a comprehensive personal finance management Android application built with Jetpack Compose and Firebase. The app helps users track their income, expenses, subscriptions, and savings goals with an intuitive and modern interface.

## 📱 Features

### Core Features

#### 1. **Transaction Management**
- **Add Expenses**: Track your spending with categories, descriptions, and amounts
- **Add Income**: Record your earnings and income sources
- **Transaction History**: View all your transactions in a clean, organized list
- **Category-based Organization**: Transactions are automatically categorized (Grocery, Transportation, Food & Dining, Shopping, etc.)
- **Smart Date Display**: Shows "Today", "Yesterday", or formatted dates for better readability

#### 2. **Balance Tracking**
- **Real-time Balance**: View your current balance with automatic calculations
- **Balance Change Indicators**: See percentage and absolute changes in your balance
- **Balance Notifications**: Get notified when your balance changes significantly
- **Daily Overview**: Visual chart showing daily savings/spending over the past 7 days

#### 3. **Smart Savings Insights**
- **Weekly Savings Goal**: Track progress towards a weekly savings goal (default: ₹10,000)
- **Daily Savings Chart**: Visual representation of your daily financial activity
- **Smart Suggestions**: AI-powered spending tips based on your most spent categories
  - Personalized recommendations for categories like Grocery, Transportation, Food, Shopping, etc.
  - Context-aware suggestions to help you save money

#### 4. **Subscription Management**
- **Track Subscriptions**: Add and manage recurring subscriptions
- **Billing Cycles**: Support for monthly and annual billing cycles
- **Category Organization**: Organize subscriptions by category (Entertainment, etc.)
- **Active/Inactive Status**: Mark subscriptions as active or inactive

#### 5. **Financial Goals**
- **Goal Creation**: Set savings goals with target amounts
- **Progress Tracking**: Monitor your progress towards each goal
- **Multiple Goal Types**: Support for different goal icons (home, flight, emergency, other)
- **Contribution Tracking**: Track how many times you've contributed to each goal

#### 6. **Receipt Scanning (OCR)**
- **Image Sharing Integration**: Share receipt images directly to the app
- **ML Kit Text Recognition**: Automatically extract text from receipt images
- **Smart Transaction Parsing**: Automatically detect transaction type (income/expense) and amount
- **Auto-fill Forms**: Pre-fill transaction forms with extracted data

#### 7. **User Authentication**
- **Email/Password Sign Up**: Create a new account with email and password
- **Secure Login**: Firebase Authentication for secure user management
- **Auto-login**: Automatic login for returning users
- **User-specific Data**: All data is tied to user accounts for privacy

#### 8. **Modern UI/UX**
- **Material Design 3**: Beautiful, modern interface following Material Design guidelines
- **Custom Theme**: Branded color scheme with BudgetBaeDarkGreen, BudgetBaeGold, and BudgetBaeTeal
- **Bottom Navigation**: Easy navigation between Home, Expenses, Income, Subscriptions, and Goals
- **Responsive Layouts**: Optimized for various screen sizes
- **Splash Screen**: Branded splash screen on app launch

## 🏗️ Project Structure

```
FinTrack/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/humblecoders/fintrack/
│   │   │   │   ├── MainActivity.kt              # Main activity and navigation setup
│   │   │   │   ├── HomeScreen.kt                # Home screen with balance, charts, and recent transactions
│   │   │   │   ├── ExpenseScreen.kt             # Expenses list and management
│   │   │   │   ├── IncomeScreen.kt              # Income list and management
│   │   │   │   ├── SubscriptionScreen.kt        # Subscriptions management
│   │   │   │   ├── GoalsScreen.kt               # Financial goals management
│   │   │   │   ├── AddTransactionScreen.kt      # Add/edit expense or income
│   │   │   │   ├── AddSubscriptionScreen.kt     # Add/edit subscription
│   │   │   │   ├── AddGoalScreen.kt             # Add/edit financial goal
│   │   │   │   ├── LoginScreen.kt               # User login
│   │   │   │   ├── SignUpScreen.kt              # User registration
│   │   │   │   ├── WelcomeScreen.kt             # Welcome/landing screen
│   │   │   │   ├── GetStartedScreen.kt          # Onboarding screen
│   │   │   │   ├── SplashScreen.kt              # App splash screen
│   │   │   │   ├── FirebaseHelper.kt            # Firebase operations (auth, Firestore)
│   │   │   │   ├── ReceiptScanner.kt            # ML Kit OCR for receipt scanning
│   │   │   │   ├── TransactionParser.kt         # Parse extracted text from receipts
│   │   │   │   ├── NotificationHelper.kt        # Balance change notifications
│   │   │   │   ├── ToastHelper.kt               # Toast message utilities
│   │   │   │   └── ui/
│   │   │   │       └── theme/
│   │   │   │           ├── Color.kt             # App color definitions
│   │   │   │           ├── Theme.kt             # Material theme configuration
│   │   │   │           └── Type.kt              # Typography definitions
│   │   │   ├── res/                             # Resources (drawables, values, etc.)
│   │   │   └── AndroidManifest.xml              # App manifest
│   │   ├── androidTest/                         # Android instrumentation tests
│   │   └── test/                                # Unit tests
│   ├── build.gradle.kts                         # App-level build configuration
│   └── google-services.json                     # Firebase configuration
├── gradle/
│   ├── libs.versions.toml                       # Dependency version catalog
│   └── wrapper/                                 # Gradle wrapper files
├── build.gradle.kts                             # Project-level build configuration
├── settings.gradle.kts                          # Project settings
└── README.md                                    # This file
```

## 🛠️ Tech Stack

### Core Technologies
- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern declarative UI framework
- **Material Design 3**: UI component library
- **Android Navigation Component**: Navigation between screens

### Backend & Services
- **Firebase Authentication**: User authentication and management
- **Cloud Firestore**: NoSQL database for storing user data
- **Firebase ML Kit**: Text recognition for receipt scanning

### Key Libraries
- `androidx.compose.material3`: Material Design 3 components
- `androidx.navigation:navigation-compose`: Navigation for Compose
- `com.google.firebase:firebase-auth`: Firebase Authentication
- `com.google.firebase:firebase-firestore`: Cloud Firestore
- `com.google.mlkit:text-recognition`: ML Kit text recognition

### Architecture
- **MVVM Pattern**: Separation of concerns with ViewModels (implicit)
- **Compose UI**: Declarative UI with state management
- **Coroutines**: Asynchronous operations and Firebase callbacks
- **State Management**: Using Compose's `remember` and `mutableStateOf`

## 📊 Data Models

### Transaction
```kotlin
data class Transaction(
    val id: String,
    val userId: String,
    val amount: Double,
    val type: String,        // "income" or "expense"
    val category: String,
    val description: String,
    val timestamp: Long
)
```

### Subscription
```kotlin
data class Subscription(
    val id: String,
    val userId: String,
    val name: String,
    val amount: Double,
    val billingCycle: String,  // "monthly" or "annual"
    val isActive: Boolean,
    val lastPaidMonth: String, // Format: "yyyy-MM"
    val category: String,
    val timestamp: Long
)
```

### Goal
```kotlin
data class Goal(
    val id: String,
    val userId: String,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val icon: String,          // "home", "flight", "emergency", "other"
    val isActive: Boolean,
    val timestamp: Long,
    val contributionCount: Int
)
```

### UserBalance
```kotlin
data class UserBalance(
    val userId: String,
    val balance: Double,
    val lastUpdated: Long
)
```

## 🚀 Setup Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android SDK (API 24 minimum, API 35 target)
- Firebase project with Authentication and Firestore enabled

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd FinTrack
   ```

2. **Set up Firebase**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Enable Authentication (Email/Password)
   - Enable Cloud Firestore
   - Download `google-services.json` and place it in `app/` directory

3. **Configure Firebase**
   - Ensure `google-services.json` is in the correct location
   - The app will automatically initialize Firebase on startup

4. **Build and Run**
   - Open the project in Android Studio
   - Sync Gradle files
   - Run the app on an emulator or physical device (API 24+)

### Firebase Setup Details

#### Firestore Collections Structure
- `transactions`: User transaction records
- `subscriptions`: User subscription records
- `goals`: User financial goals
- `balances`: User balance information

#### Security Rules
Make sure to set up appropriate Firestore security rules to protect user data:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{collection}/{document} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
    }
  }
}
```

## 📱 App Flow

1. **Splash Screen** → Shows app branding
2. **Get Started** → Onboarding screen
3. **Welcome** → Login/Sign up options
4. **Authentication** → Login or Sign up
5. **Home Screen** → Main dashboard with:
   - Current balance
   - Daily overview chart
   - Weekly savings progress
   - Smart suggestions
   - Recent transactions
6. **Navigation** → Access to:
   - Expenses screen
   - Income screen
   - Subscriptions screen
   - Goals screen

## 🎨 Design System

### Colors
- **BudgetBaeDarkGreen**: Primary brand color
- **BudgetBaeGold**: Accent color
- **BudgetBaeTeal**: Secondary accent color

### Typography
- Headlines: Bold, 18-36sp
- Body: Regular, 12-16sp
- Labels: Secondary color, 12-14sp

### Components
- Cards with rounded corners (16dp radius)
- Material 3 buttons and navigation
- Custom progress indicators
- Icon-based category representation

## 🔔 Notifications

The app supports balance change notifications:
- Notifications are shown when balance changes significantly
- Requires notification permission on Android 13+ (API 33+)
- Notification channel is automatically created on app launch

## 📸 Receipt Scanning

The app supports receipt scanning via image sharing:
1. Share a receipt image to the app from any app
2. ML Kit extracts text from the image
3. Transaction parser identifies amount and type
4. Transaction form is auto-filled with extracted data
5. User can review and save the transaction

## 🔒 Privacy & Security

- All user data is stored securely in Firebase
- User authentication required for all operations
- Data is isolated per user account
- No data sharing between users

## 📝 License

This project is private and proprietary.

## 👥 Contributors

Developed by Humble Coders

## 📧 Support

For issues or questions, please contact the development team.

---

**Budget BAE** - Your Money Wingmate 💰

