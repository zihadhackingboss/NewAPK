package com.example.ai

object OfflineTranslator {

    private val commonPhrasesEnToBn = mapOf(
        "i love you" to "আমি তোমাকে ভালোবাসি",
        "how are you" to "আপনি কেমন আছেন?",
        "how are you?" to "আপনি কেমন আছেন?",
        "good morning" to "শুভ সকাল",
        "good night" to "শুভ রাত্রি",
        "thank you" to "ধন্যবাদ",
        "thank you very much" to "আপনাকে অনেক ধন্যবাদ",
        "welcome" to "স্বাগতম",
        "you are welcome" to "आपको স্বাগতম",
        "sorry" to "দুঃখিত",
        "i am sorry" to "আমি আন্তরিকভাবে দুঃখিত",
        "hello" to "হ্যালো",
        "hi" to "হাই",
        "bye" to "বিদায়",
        "see you later" to "পরে দেখা হবে",
        "what is your name" to "আপনার নাম কি?",
        "my name is" to "আমার নাম",
        "where are you from" to "আপনি কোথায় থাকেন?",
        "nice to meet you" to "আপনার সাথে দেখা হয়ে ভালো লাগলো",
        "yes" to "হ্যাঁ",
        "no" to "না",
        "please" to "দয়া করে",
        "help" to "সাহায্য",
        "good luck" to "শুভকামনা",
        "congratulations" to "অভিনন্দন",
        "happy birthday" to "শুভ জন্মদিন",
        "i agree" to "আমি একমত",
        "what happened" to "কী হয়েছে?",
        "take care" to "নিজের খেয়াল রাখবেন"
    )

    private val commonPhrasesBnToEn = mapOf(
        "আমি তোমাকে ভালোবাসি" to "I love you",
        "কেমন আছেন" to "How are you",
        "শুভ সকাল" to "Good morning",
        "শুভ রাত্রি" to "Good night",
        "ধন্যবাদ" to "Thank you",
        "হ্যালো" to "Hello",
        "হাই" to "Hi",
        "বিদায়" to "Goodbye",
        "আপনার নাম কি" to "What is your name",
        "হ্যাঁ" to "Yes",
        "না" to "No",
        "কী হয়েছে" to "What happened"
    )

    private val phoneticDictionary = mapOf(
        "ami" to "আমি",
        "tumi" to "তুমি",
        "apni" to "আপনি",
        "tomake" to "তোমাকে",
        "bhalobashi" to "ভালোবাসি",
        "bhalo" to "ভালো",
        "achi" to "আছি",
        "kemon" to "কেমন",
        "acho" to "আছো",
        "kore" to "করে",
        "dhonnobad" to "ধন্যবাদ",
        "khobor" to "খবর",
        "shubho" to "শুভ",
        "shokal" to "সকাল",
        "ratri" to "রাত্রি",
        "shunchen" to "শুনছেন",
        "ki" to "কী",
        "korcho" to "করছো",
        "naam" to "নাম",
        "amar" to "আমার",
        "amader" to "আমাদের",
        "kaj" to "কাজ",
        "bari" to "বাড়ি",
        "basha" to "বাসা",
        "bondhu" to "বন্ধু",
        "khabar" to "খাবার",
        "khabo" to "খাবো",
        "jabona" to "যাব না",
        "jabo" to "যাব"
    )

    fun translateEnToBn(input: String): String {
        val trimmed = input.trim().lowercase()
        // Check exact match
        commonPhrasesEnToBn[trimmed]?.let { return it }

        // Word by word conversion fallback
        val words = input.split(" ")
        val translatedWords = words.map { word ->
            val cleanWord = word.lowercase().replace(Regex("[^a-zA-Z0-9]"), "")
            commonPhrasesEnToBn[cleanWord]
                ?: phoneticDictionary[cleanWord]
                ?: phoneticEnToBnWord(cleanWord)
        }
        return translatedWords.joinToString(" ")
    }

    fun translateBnToEn(input: String): String {
        val trimmed = input.trim()
        commonPhrasesBnToEn[trimmed]?.let { return it }
        return "I love you ❤️" // Default fallback or phrase match
    }

    private fun phoneticEnToBnWord(word: String): String {
        if (word.isBlank()) return ""
        var result = word
            .replace("kemon", "কেমন")
            .replace("bhalo", "ভালো")
            .replace("bashi", "বাসি")
            .replace("ami", "আমি")
            .replace("tumi", "তুমি")
            .replace("tomake", "তোমাকে")
            .replace("dhonnobad", "ধন্যবাদ")
            .replace("shubho", "শুভ")
            .replace("shokal", "সকাল")
        return result
    }

    fun getNextWordPredictions(currentText: String): List<String> {
        val trimmed = currentText.trim().lowercase()
        return when {
            trimmed.endsWith("i love") || trimmed.endsWith("love") -> listOf("you", "you ❤️", "it", "my family")
            trimmed.endsWith("how are") -> listOf("you?", "you doing?", "things?")
            trimmed.endsWith("good") -> listOf("morning!", "night!", "luck!", "job!")
            trimmed.endsWith("thank") -> listOf("you!", "you so much!", "you very much!")
            trimmed.endsWith("আমি") -> listOf("তোমাকে", "ভালো", "আছি", "যাব")
            trimmed.endsWith("শুভ") -> listOf("সকাল", "রাত্রি", "জন্মদিন", "কামনা")
            else -> listOf("you", "and", "the", "in", "to", "is")
        }
    }

    fun getEmojiForText(text: String): List<String> {
        val lower = text.lowercase()
        val emojis = mutableListOf<String>()
        if (lower.contains("love") || lower.contains("ভালোবাসা") || lower.contains("ভালোবাসি")) {
            emojis.addAll(listOf("❤️", "💖", "😍", "🥰", "💕"))
        }
        if (lower.contains("happy") || lower.contains("smile") || lower.contains("ভালো") || lower.contains("শুভ")) {
            emojis.addAll(listOf("😊", "😄", "✨", "🎉", "👍"))
        }
        if (lower.contains("sad") || lower.contains("sorry") || lower.contains("দুঃখিত")) {
            emojis.addAll(listOf("🥺", "😔", "🙏", "💔"))
        }
        if (lower.contains("fire") || lower.contains("awesome") || lower.contains("cool")) {
            emojis.addAll(listOf("🔥", "😎", "💯", "🚀"))
        }
        if (emojis.isEmpty()) {
            emojis.addAll(listOf("❤️", "😊", "👍", "🔥", "🙏", "✨"))
        }
        return emojis.distinct()
    }
}
