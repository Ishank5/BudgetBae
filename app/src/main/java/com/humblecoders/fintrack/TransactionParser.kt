package com.humblecoders.fintrack

/**
 * Data class to hold parsed transaction information
 */
data class ParsedTransaction(
    val amount: Double?,
    val type: String?, // "expense" or "income"
    val category: String?
)

object TransactionParser {
    /**
     * Parse raw text from receipt/screenshot to extract transaction details
     */
    fun parseTransaction(rawText: String): ParsedTransaction {
        val normalizedText = rawText.lowercase()
        
        // Extract amount
        val amount = extractAmount(rawText)
        
        // Detect transaction type
        val type = detectTransactionType(normalizedText)
        
        // Detect category
        val category = detectCategory(normalizedText)
        
        return ParsedTransaction(amount, type, category)
    }
    
    /**
     * Extract amount from text using regex pattern for Indian Rupee
     * Pattern: ₹ followed by digits, commas, and decimals
     */
    private fun extractAmount(text: String): Double? {
        // Years to exclude (2020-2030)
        val yearRange = (2020..2030).toSet()
        
        // Split text into lines for better context detection
        val lines = text.split("\n").map { it.trim() }
        
        val foundAmounts = mutableListOf<Pair<Double, Int>>() // Pair of (amount, priority)
        
        // Pattern 1: Amounts with currency symbols (highest priority = 100)
        val currencyPatterns = listOf(
            Regex("₹\\s*([0-9,]+\\.[0-9]{2})"),           // ₹1,234.56
            Regex("₹\\s*([0-9,]+)"),                       // ₹1,234
            Regex("([0-9,]+\\.[0-9]{2})\\s*₹"),           // 1,234.56₹
            Regex("([0-9,]+)\\s*₹"),                       // 1,234₹
            Regex("rs\\.?\\s*([0-9,]+\\.[0-9]{2})", RegexOption.IGNORE_CASE),
            Regex("rs\\.?\\s*([0-9,]+)", RegexOption.IGNORE_CASE),
            Regex("inr\\s*([0-9,]+\\.[0-9]{2})", RegexOption.IGNORE_CASE),
            Regex("inr\\s*([0-9,]+)", RegexOption.IGNORE_CASE)
        )
        
        for (pattern in currencyPatterns) {
            pattern.findAll(text).forEach { match ->
                val amountStr = match.groupValues[1].replace(",", "").trim()
                val amount = amountStr.toDoubleOrNull()
                if (amount != null && amount > 0 && amount < 100000000) {
                    val amountInt = amount.toInt()
                    if (amountInt !in yearRange) {
                        foundAmounts.add(Pair(amount, 100))
                    }
                }
            }
        }
        
        // Pattern 2: Standalone numbers on their own line (priority = 80)
        // This catches cases like "10" on a line by itself
        lines.forEach { line ->
            val standalonePattern = Regex("^\\s*([0-9,]+(?:\\.[0-9]{2})?)\\s*$")
            standalonePattern.find(line)?.let { match ->
                val amountStr = match.groupValues[1].replace(",", "").trim()
                val amount = amountStr.toDoubleOrNull()
                if (amount != null && amount > 0) {
                    val amountInt = amount.toInt()
                    // Exclude years and very large numbers
                    if (amountInt !in yearRange && amount < 1000000) {
                        // Check if line is near transaction keywords
                        val lineIndex = lines.indexOf(line)
                        val context = lines.subList(
                            maxOf(0, lineIndex - 2),
                            minOf(lines.size, lineIndex + 3)
                        ).joinToString(" ").lowercase()
                        
                        val hasTransactionContext = context.contains(Regex(
                            "transaction|paid|amount|sent|received|transfer|upi|gpay|payment"
                        ))
                        
                        if (hasTransactionContext || amount <= 999) {
                            foundAmounts.add(Pair(amount, 80))
                        }
                    }
                }
            }
        }
        
        // Pattern 3: Numbers near transaction keywords (priority = 70)
        val keywordPattern = Regex(
            "(?:transaction|paid|amount|rs|₹|inr|sent|received|transfer|upi)[\\s:]*([0-9,]+(?:\\.[0-9]{2})?)",
            RegexOption.IGNORE_CASE
        )
        keywordPattern.findAll(text).forEach { match ->
            val amountStr = match.groupValues[1].replace(",", "").trim()
            val amount = amountStr.toDoubleOrNull()
            if (amount != null && amount > 0 && amount < 100000000) {
                val amountInt = amount.toInt()
                if (amountInt !in yearRange) {
                    foundAmounts.add(Pair(amount, 70))
                }
            }
        }
        
        // Pattern 4: Amounts with decimals (priority = 60)
        val decimalPattern = Regex("([0-9,]+)\\.[0-9]{2}")
        decimalPattern.findAll(text).forEach { match ->
            val amountStr = match.groupValues[1].replace(",", "").trim()
            val amount = amountStr.toDoubleOrNull()
            if (amount != null && amount > 0 && amount < 1000000) {
                val amountInt = amount.toInt()
                if (amountInt !in yearRange) {
                    foundAmounts.add(Pair(amount, 60))
                }
            }
        }
        
        // Pattern 5: Small standalone numbers 1-999 (priority = 50)
        // These are likely amounts, not years or IDs
        val smallNumberPattern = Regex("\\b([0-9]{1,3})\\b")
        smallNumberPattern.findAll(text).forEach { match ->
            val amountStr = match.groupValues[1]
            val amount = amountStr.toDoubleOrNull()
            if (amount != null && amount > 0 && amount <= 999) {
                val amountInt = amount.toInt()
                // Exclude years
                if (amountInt !in yearRange) {
                    // Check if it's in a date context (Nov 14, 2025 or 14 Nov 2025)
                    val matchStart = match.range.first
                    val contextStart = maxOf(0, matchStart - 20)
                    val contextEnd = minOf(text.length, matchStart + 20)
                    val context = text.substring(contextStart, contextEnd).lowercase()
                    
                    val isInDate = context.contains(Regex(
                        "(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\s*$amountInt|$amountInt\\s*(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)"
                    ))
                    
                    if (!isInDate) {
                        foundAmounts.add(Pair(amount, 50))
                    }
                }
            }
        }
        
        if (foundAmounts.isEmpty()) {
            return null
        }
        
        // Sort by priority (higher first), then by amount (smaller first for same priority)
        // We prefer smaller amounts as they're more likely to be transaction amounts
        val sortedAmounts = foundAmounts.sortedWith(
            compareByDescending<Pair<Double, Int>> { it.second }
                .thenBy { it.first }
        )
        
        // Return the highest priority amount
        return sortedAmounts.first().first
    }
    
    /**
     * Detect transaction type (Income vs Expense) based on keywords
     */
    private fun detectTransactionType(text: String): String? {
        val normalizedText = text.lowercase()
        
        // Pattern 1: Check for "From:" and "To:" patterns (common in UPI/GPay)
        // "From: [Someone]" means money coming FROM them = INCOME
        // "To: [User's name]" means money going TO user = INCOME
        // "To: [Someone else]" means money going TO them = EXPENSE
        
        val fromPattern = Regex("from\\s*:?\\s*([^\\n]+)", RegexOption.IGNORE_CASE)
        val toPattern = Regex("to\\s*:?\\s*([^\\n]+)", RegexOption.IGNORE_CASE)
        
        val fromMatches = fromPattern.findAll(text)
        val toMatches = toPattern.findAll(text)
        
        // Check if there's a "From:" pattern - this usually indicates income
        for (match in fromMatches) {
            val fromText = match.groupValues[1].lowercase()
            // If "From:" appears and it's not "From: [User's name]", it's income
            if (fromText.isNotBlank()) {
                android.util.Log.d("TransactionParser", "Found 'From:' pattern: $fromText")
                return "income"
            }
        }
        
        // Check "To:" pattern - need to determine if it's to user or from user
        // In GPay screenshots, if "To:" appears with a name, check context
        for (match in toMatches) {
            val toText = match.groupValues[1].lowercase()
            if (toText.isNotBlank()) {
                android.util.Log.d("TransactionParser", "Found 'To:' pattern: $toText")
                // If we also have "From:" in the text, "To:" likely means expense
                // But if only "To:" exists and it's near transaction keywords, it might be income
                // For now, if we see both patterns, prioritize "From:" (income)
                // If only "To:" exists, we'll check other keywords below
            }
        }
        
        // Pattern 2: Expense indicators
        val expenseKeywords = listOf(
            "paid to",
            "paid",
            "debit",
            "sent to",
            "payment to",
            "transferred to",
            "to:",  // "To: [Someone]" usually means expense
            "you paid",
            "payment sent"
        )
        
        // Pattern 3: Income indicators
        val incomeKeywords = listOf(
            "received from",
            "received",
            "credited to",
            "credited",
            "bank transfer",
            "credit",
            "money received",
            "added to",
            "from:",  // "From: [Someone]" usually means income
            "you received",
            "money added"
        )
        
        // Check for expense keywords
        for (keyword in expenseKeywords) {
            if (normalizedText.contains(keyword, ignoreCase = true)) {
                // But exclude if "From:" also appears (From takes priority)
                if (!normalizedText.contains("from:", ignoreCase = true)) {
                    android.util.Log.d("TransactionParser", "Detected expense from keyword: $keyword")
                    return "expense"
                }
            }
        }
        
        // Check for income keywords
        for (keyword in incomeKeywords) {
            if (normalizedText.contains(keyword, ignoreCase = true)) {
                android.util.Log.d("TransactionParser", "Detected income from keyword: $keyword")
                return "income"
            }
        }
        
        // Pattern 4: If we have "To:" but no "From:", check the context
        // In UPI receipts, "To: [Name]" usually means you sent money (expense)
        // But if it's "To: [Your name]", it means you received (income)
        if (toMatches.any() && !fromMatches.any()) {
            // Default to expense if only "To:" is present (most common case)
            android.util.Log.d("TransactionParser", "Only 'To:' pattern found, defaulting to expense")
            return "expense"
        }
        
        return null
    }
    
    /**
     * Detect category based on keywords in the text
     */
    private fun detectCategory(text: String): String? {
        val categoryKeywords = mapOf(
            "Food" to listOf(
                // Food delivery apps
                "swiggy", "zomato", "uber eats", "ubereats", "foodpanda", "dunzo",
                // Restaurants & fast food
                "mcdonalds", "mcd", "burger", "pizza", "dominos", "pizza hut", "kfc", 
                "subway", "starbucks", "cafe", "coffee", "restaurant", "dining",
                // General food terms
                "food", "meal", "lunch", "dinner", "breakfast", "snack", "eat"
            ),
            "Transport" to listOf(
                // Ride-hailing apps
                "uber", "ola", "rapido", "in-drive", "indrive",
                // Fuel & vehicle
                "petrol", "fuel", "diesel", "gas", "gasoline", "bpcl", "hpcl", "ioc",
                // Public transport
                "metro", "bus", "train", "railway", "irctc", "auto", "rickshaw", "tuk-tuk",
                // Travel related
                "travel", "travelling", "trip", "journey", "commute", "commuting",
                // Transport services
                "taxi", "cab", "transport", "transportation", "booking", "make my trip", "makemytrip",
                // Vehicle related
                "parking", "toll", "tollgate", "fastag"
            ),
            "Bills" to listOf(
                // Telecom
                "jio", "airtel", "vodafone", "idea", "bsnl", "vi", "recharge", "prepaid", "postpaid",
                // Utilities
                "electricity", "bescom", "tneb", "msedcl", "water", "municipal", "corporation",
                // Internet & TV
                "internet", "wifi", "broadband", "act", "airtel fiber", "jio fiber", "tata sky", "dish tv",
                // General
                "phone", "mobile", "bill", "utility", "utilities", "payment", "dues"
            ),
            "Entertainment" to listOf(
                // Streaming services
                "netflix", "prime", "prime video", "hotstar", "disney", "sony liv", "zee5", "voot",
                // Movies & shows
                "cinema", "bookmyshow", "pvr", "inox", "carnival", "movie", "theatre", "theater",
                // Music & gaming
                "spotify", "youtube", "youtube premium", "gaming", "playstation", "xbox", "steam",
                // General
                "entertainment", "streaming", "subscription"
            ),
            "Health" to listOf(
                // Medical services
                "pharmacy", "apollo", "fortis", "max", "medanta", "hospital", "clinic", "doctor", "dr",
                // Health products
                "medicine", "medicines", "pharma", "pharmaceutical", "1mg", "netmeds", "practo",
                // Fitness
                "fitness", "gym", "health", "wellness", "yoga", "pilates", "workout"
            ),
            "Shopping" to listOf(
                // E-commerce
                "amazon", "flipkart", "myntra", "nykaa", "meesho", "ajio", "snapdeal", "paytm mall",
                // Retail stores
                "d-mart", "dmart", "reliance", "big bazaar", "spencer", "more", "hypercity",
                // General
                "shopping", "mall", "store", "purchase", "buy", "retail", "fashion", "clothing"
            ),
            "Grocery" to listOf(
                // Grocery delivery
                "bigbasket", "grofers", "zepto", "blinkit", "instamart", "swiggy instamart",
                // Stores
                "grocery", "groceries", "supermarket", "hypermarket",
                // Food items
                "vegetables", "fruits", "vegetable", "fruit", "milk", "bread", "eggs", "rice", "wheat"
            )
        )
        
        // Check each category (order matters - check more specific categories first)
        // Use word boundaries to avoid partial matches
        for ((category, keywords) in categoryKeywords) {
            for (keyword in keywords) {
                // Use word boundary regex to match whole words only
                val pattern = Regex("\\b${Regex.escape(keyword)}\\b", RegexOption.IGNORE_CASE)
                if (pattern.containsMatchIn(text)) {
                    android.util.Log.d("TransactionParser", "Detected category '$category' from keyword: $keyword")
                    return category
                }
            }
        }
        
        return null
    }
}

