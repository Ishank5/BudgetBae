package com.humblecoders.fintrack

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class Goal(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val icon: String = "home", // home, flight, emergency, other
    val isActive: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val contributionCount: Int = 0 // Number of times money was added to this goal
)

data class Transaction(
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val type: String = "", // "income" or "expense"
    val category: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
data class Subscription(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val billingCycle: String = "monthly", // "monthly" or "annual"
    val isActive: Boolean = true,
    val lastPaidMonth: String = "", // Format: "yyyy-MM"
    val category: String = "Entertainment",
    val timestamp: Long = System.currentTimeMillis()
)

data class UserBalance(
    val userId: String = "",
    val balance: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)

object FirebaseHelper {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun signUp(email: String, password: String, fullName: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("User ID not found")

            val userBalance = UserBalance(userId = userId, balance = 0.0)
            firestore.collection("balances").document(userId).set(userBalance).await()

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user?.uid ?: throw Exception("User ID not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun addTransaction(
        amount: Double,
        type: String,
        category: String,
        description: String
    ): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("User not logged in")

            val transaction = Transaction(
                userId = userId,
                amount = amount,
                type = type,
                category = category,
                description = description,
                timestamp = System.currentTimeMillis()
            )

            val docRef = firestore.collection("transactions").document()
            transaction.copy(id = docRef.id).let {
                docRef.set(it).await()
            }

            updateBalance(userId, amount, type)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateBalance(userId: String, amount: Double, type: String) {
        val balanceRef = firestore.collection("balances").document(userId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(balanceRef)
            val currentBalance = snapshot.getDouble("balance") ?: 0.0
            val newBalance = if (type == "income") {
                currentBalance + amount
            } else {
                currentBalance - amount
            }
            transaction.update(balanceRef, "balance", newBalance)
            transaction.update(balanceRef, "lastUpdated", System.currentTimeMillis())
        }.await()
    }

    suspend fun getBalance(): Result<Double> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("User not logged in")
            val doc = firestore.collection("balances").document(userId).get().await()
            Result.success(doc.getDouble("balance") ?: 0.0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllTransactions(): Result<List<Transaction>> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("User not logged in")
            val snapshot = firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val transactions = snapshot.documents
                .mapNotNull { it.toObject(Transaction::class.java) }
                .sortedByDescending { it.timestamp }

            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentTransactions(limit: Int = 20): Result<List<Transaction>> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("User not logged in")
            val snapshot = firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val transactions = snapshot.documents
                .mapNotNull { it.toObject(Transaction::class.java) }
                .sortedByDescending { it.timestamp }
                .take(limit)

            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDailySavings(days: Int = 7): Result<List<Pair<String, Double>>> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("User not logged in")

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.add(Calendar.DAY_OF_YEAR, -(days - 1))

            val startTime = calendar.timeInMillis

            val snapshot = firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val transactions = snapshot.documents
                .mapNotNull { it.toObject(Transaction::class.java) }
                .filter { it.timestamp >= startTime }

            val dailyMap = mutableMapOf<String, Double>()
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

            val tempCalendar = Calendar.getInstance()
            tempCalendar.timeInMillis = startTime

            for (i in 0 until days) {
                val dateKey = dateFormat.format(tempCalendar.time)
                dailyMap[dateKey] = 0.0
                tempCalendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            transactions.forEach { transaction ->
                val date = Date(transaction.timestamp)
                val dateKey = dateFormat.format(date)

                if (dailyMap.containsKey(dateKey)) {
                    val current = dailyMap[dateKey] ?: 0.0
                    dailyMap[dateKey] = if (transaction.type == "income") {
                        current + transaction.amount
                    } else {
                        current - transaction.amount
                    }
                }
            }

            val sortedList = dailyMap.entries.sortedBy { entry ->
                val dateFormat2 = SimpleDateFormat("MMM dd", Locale.getDefault())
                dateFormat2.parse(entry.key)?.time ?: 0
            }

            Result.success(sortedList.map { it.key to it.value })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWeeklySavings(): Result<Double> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("User not logged in")

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val startTime = calendar.timeInMillis

            val snapshot = firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val transactions = snapshot.documents
                .mapNotNull { it.toObject(Transaction::class.java) }
                .filter { it.timestamp >= startTime }

            var weeklySavings = 0.0
            transactions.forEach { transaction ->
                weeklySavings += if (transaction.type == "income") {
                    transaction.amount
                } else {
                    -transaction.amount
                }
            }

            Result.success(weeklySavings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addSubscription(
        name: String,
        amount: Double,
        billingCycle: String,
        category: String
    ): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("User not logged in")

            val subscription = Subscription(
                userId = userId,
                name = name,
                amount = amount,
                billingCycle = billingCycle,
                category = category,
                isActive = true,
                lastPaidMonth = "",
                timestamp = System.currentTimeMillis()
            )

            val docRef = firestore.collection("subscriptions").document()
            subscription.copy(id = docRef.id).let {
                docRef.set(it).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllSubscriptions(): Result<List<Subscription>> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("User not logged in")
            val snapshot = firestore.collection("subscriptions")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val subscriptions = snapshot.documents
                .mapNotNull { it.toObject(Subscription::class.java) }
                .sortedByDescending { it.timestamp }

            Result.success(subscriptions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSubscriptionStatus(subscriptionId: String, isActive: Boolean): Result<Unit> {
        return try {
            firestore.collection("subscriptions")
                .document(subscriptionId)
                .update("isActive", isActive)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun paySubscriptionForMonth(subscription: Subscription): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("User not logged in")

            val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

            val transaction = Transaction(
                userId = userId,
                amount = subscription.amount,
                type = "expense",
                category = subscription.category,
                description = "${subscription.name} - Subscription",
                timestamp = System.currentTimeMillis()
            )

            val docRef = firestore.collection("transactions").document()
            transaction.copy(id = docRef.id).let {
                docRef.set(it).await()
            }

            firestore.collection("subscriptions")
                .document(subscription.id)
                .update("lastPaidMonth", currentMonth)
                .await()

            updateBalance(userId, subscription.amount, "expense")

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun addGoal(
        name: String,
        targetAmount: Double,
        icon: String
    ): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("User not logged in")

            val goal = Goal(
                userId = userId,
                name = name,
                targetAmount = targetAmount,
                currentAmount = 0.0,
                icon = icon,
                isActive = true,
                timestamp = System.currentTimeMillis()
            )

            val docRef = firestore.collection("goals").document()
            goal.copy(id = docRef.id).let {
                docRef.set(it).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllGoals(): Result<List<Goal>> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("User not logged in")
            val snapshot = firestore.collection("goals")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val goals = snapshot.documents
                .mapNotNull { it.toObject(Goal::class.java) }
                .sortedByDescending { it.timestamp }

            Result.success(goals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addMoneyToGoal(goalId: String, amount: Double): Result<Unit> {
        return try {
            val goalRef = firestore.collection("goals").document(goalId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(goalRef)
                val currentAmount = snapshot.getDouble("currentAmount") ?: 0.0
                val newAmount = currentAmount + amount
                val contributionCount = (snapshot.getLong("contributionCount") ?: 0L).toInt() + 1
                transaction.update(goalRef, "currentAmount", newAmount)
                transaction.update(goalRef, "contributionCount", contributionCount)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTotalGoalProgress(): Result<Double> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("User not logged in")
            val snapshot = firestore.collection("goals")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val totalProgress = snapshot.documents
                .mapNotNull { it.toObject(Goal::class.java) }
                .sumOf { it.currentAmount }

            Result.success(totalProgress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}