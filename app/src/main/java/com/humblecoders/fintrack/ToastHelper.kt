package com.humblecoders.fintrack

import android.content.Context
import android.widget.Toast
import kotlin.random.Random

object ToastHelper {
    private val expenseMessages = mapOf(
        "grocery" to listOf(
            "Pantry stocked! Your future self will thank you. 🥦",
            "Great job keeping the kitchen ready for the week. 🛒"
        ),
        "groceries" to listOf(
            "Pantry stocked! Your future self will thank you. 🥦",
            "Great job keeping the kitchen ready for the week. 🛒"
        ),
        "transport" to listOf(
            "Drive safely! Logging this trip expense now. 🚗",
            "Getting around costs money, but time is valuable. 🚦"
        ),
        "transportation" to listOf(
            "Drive safely! Logging this trip expense now. 🚗",
            "Getting around costs money, but time is valuable. 🚦"
        ),
        "uber" to listOf(
            "Drive safely! Logging this trip expense now. 🚗",
            "Getting around costs money, but time is valuable. 🚦"
        ),
        "food" to listOf(
            "Bon appétit! Hope the meal tastes amazing. 🍔",
            "Treating yourself is important! Just balance the rest. 🍕"
        ),
        "food & dining" to listOf(
            "Bon appétit! Hope the meal tastes amazing. 🍔",
            "Treating yourself is important! Just balance the rest. 🍕"
        ),
        "dining" to listOf(
            "Bon appétit! Hope the meal tastes amazing. 🍔",
            "Treating yourself is important! Just balance the rest. 🍕"
        ),
        "starbucks" to listOf(
            "Bon appétit! Hope the meal tastes amazing. 🍔",
            "Treating yourself is important! Just balance the rest. 🍕"
        ),
        "shopping" to listOf(
            "New things! Hope this purchase brings you joy. ✨",
            "Retail therapy complete! Your expense is recorded. 🛍️"
        ),
        "mall" to listOf(
            "New things! Hope this purchase brings you joy. ✨",
            "Retail therapy complete! Your expense is recorded. 🛍️"
        ),
        "entertainment" to listOf(
            "Memories are priceless, hope you have a blast! 🍿",
            "Time to relax and recharge! You deserve the fun. 🎮"
        ),
        "netflix" to listOf(
            "Memories are priceless, hope you have a blast! 🍿",
            "Time to relax and recharge! You deserve the fun. 🎮"
        ),
        "bills" to listOf(
            "Adulting win! Another bill crossed off the list. ✅",
            "Keeping the lights on! Essential expense paid. 💡"
        ),
        "health" to listOf(
            "Investing in yourself is the best choice! 💪",
            "Health is wealth! Good job taking care of you. 🧘"
        ),
        "other" to listOf(
            "Tracking every penny counts! Good habit. 📝",
            "Got it! Miscellaneous expense saved securely. 🔍"
        )
    )

    private val incomeMessages = mapOf(
        "salary" to listOf(
            "Payday has arrived! Time to celebrate (responsibly). 💸",
            "Hard work pays off! Your balance just got a boost. 🦅"
        ),
        "business" to listOf(
            "Hustle mode on! Your business is thriving. 💼",
            "Another win for the empire! Profit recorded. 📈"
        ),
        "investment" to listOf(
            "Making your money work for you! Smart move. 🧠",
            "Planting seeds for the future! Growth incoming. 🌱"
        ),
        "gift" to listOf(
            "What a generous gift! Enjoy the extra abundance. 🎁",
            "Unexpected blessings are the best kind! ✨"
        ),
        "other" to listOf(
            "Every little bit adds up! Income tracked. 💵",
            "Nice! Adding this extra cash to your stash. 🏦"
        )
    )

    private val goalMessages = listOf(
        "Every rupee counts! You're one step closer to your goal! 🎯",
        "Progress made! Keep the momentum going! 💪",
        "Small steps, big dreams! You're doing great! ✨",
        "Your future self will thank you for this! 🌟",
        "Building wealth, one contribution at a time! 💰",
        "Consistency is key! You're on the right track! 🚀",
        "Every contribution brings you closer to success! 🏆",
        "You're making it happen! Keep going! 💎"
    )

    fun showTransactionToast(context: Context, type: String, category: String) {
        val normalizedCategory = category.lowercase()
        val messages = if (type.lowercase() == "expense") {
            expenseMessages[normalizedCategory]
                ?: expenseMessages.entries.firstOrNull { (key, _) ->
                    normalizedCategory.contains(key) || key.contains(normalizedCategory)
                }?.value
                ?: expenseMessages["other"]
        } else {
            incomeMessages[normalizedCategory]
                ?: incomeMessages.entries.firstOrNull { (key, _) ->
                    normalizedCategory.contains(key) || key.contains(normalizedCategory)
                }?.value
                ?: incomeMessages["other"]
        }

        val message = messages?.let { it[Random.nextInt(it.size)] } ?: "Transaction recorded! ✅"
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun showGoalToast(context: Context) {
        val message = goalMessages[Random.nextInt(goalMessages.size)]
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}

