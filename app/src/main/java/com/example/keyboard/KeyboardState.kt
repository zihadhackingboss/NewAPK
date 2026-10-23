package com.example.keyboard

enum class LanguageMode {
    ENGLISH, BANGLA, DUAL
}

enum class KeyboardTab {
    KEYS, EMOJI, CLIPBOARD, STICKERS, THEMES, SETTINGS, DICTIONARY
}

enum class ThemeMode(val title: String, val primaryHex: Long, val bgHex: Long, val keyBgHex: Long, val keyTextHex: Long) {
    MATERIAL_YOU("Material You", 0xFF6366F1, 0xFF0F172A, 0xFF1E293B, 0xFFF8FAFC),
    MIDNIGHT_BLUE("Midnight Blue", 0xFF38BDF8, 0xFF0284C7, 0xFF0F172A, 0xFFF0F9FF),
    CYBER_NEON("Cyber Neon", 0xFF00F0FF, 0xFF120E24, 0xFF221A3A, 0xFF00F0FF),
    AMOLED_BLACK("AMOLED Black", 0xFF22C55E, 0xFF000000, 0xFF18181B, 0xFFFFFFFF),
    SUNSET_GOLD("Sunset Gold", 0xFFF59E0B, 0xFF1C1917, 0xFF292524, 0xFFFEF3C7),
    MINIMAL_LIGHT("Minimal Light", 0xFF4F46E5, 0xFFF8FAFC, 0xFFFFFFFF, 0xFF0F172A)
}

enum class KeyboardHeight(val dpValue: Int) {
    SHORT(220), NORMAL(260), TALL(300)
}

data class KeyboardState(
    val currentMode: LanguageMode = LanguageMode.DUAL,
    val activeTab: KeyboardTab = KeyboardTab.KEYS,
    val currentTextDraft: String = "",
    val liveTranslation: String = "",
    val isShifted: Boolean = false,
    val isSymbols: Boolean = false,
    val isNumbersRowVisible: Boolean = true,
    val isAITranslationEnabled: Boolean = true,
    val isVoiceActive: Boolean = false,
    val isListeningVoice: Boolean = false,
    val voiceRecognizedText: String = "",
    val isOneHanded: Boolean = false,
    val isOneHandLeft: Boolean = true,
    val isFloating: Boolean = false,
    val currentTheme: ThemeMode = ThemeMode.MATERIAL_YOU,
    val height: KeyboardHeight = KeyboardHeight.NORMAL,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val autoCorrectEnabled: Boolean = true,
    val suggestions: List<String> = listOf("I love you ❤️", "How are you?", "Thank you!", "Good morning"),
    val emojiSuggestions: List<String> = listOf("❤️", "😊", "👍", "🔥", "✨", "😍")
)
