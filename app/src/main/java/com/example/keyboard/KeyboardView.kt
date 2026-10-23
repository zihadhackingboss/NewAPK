package com.example.keyboard

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClipboardEntity

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyboardView(
    state: KeyboardState,
    clipboardItems: List<ClipboardEntity>,
    onKeyClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onSpaceClick: () -> Unit,
    onEnterClick: () -> Unit,
    onShiftClick: () -> Unit,
    onSymbolToggle: () -> Unit,
    onModeChange: (LanguageMode) -> Unit,
    onTabChange: (KeyboardTab) -> Unit,
    onSuggestionClick: (String) -> Unit,
    onToggleAI: () -> Unit,
    onVoiceClick: () -> Unit,
    onToggleOneHand: () -> Unit,
    onToggleFloating: () -> Unit,
    onThemeSelect: (ThemeMode) -> Unit,
    onPinClipboard: (Long, Boolean) -> Unit,
    onClearClipboard: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val theme = state.currentTheme
    val bgColor = Color(theme.bgHex)
    val keyBgColor = Color(theme.keyBgHex)
    val keyTextColor = Color(theme.keyTextHex)
    val primaryColor = Color(theme.primaryHex)

    fun performHaptic() {
        if (!state.vibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                @Suppress("DEPRECATION")
                vibrator.vibrate(30)
            }
        } catch (_: Exception) {}
    }

    // Outer layout wrapper (handles One-Handed or Normal)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(state.height.dpValue.dp + 110.dp)
            .background(bgColor)
            .padding(vertical = 4.dp, horizontal = if (state.isOneHanded) 12.dp else 4.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Dual Mode Live Translation Preview Banner (When user has draft text)
            if (state.currentMode == LanguageMode.DUAL && (state.currentTextDraft.isNotBlank() || state.liveTranslation.isNotBlank())) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = primaryColor.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(primaryColor.copy(alpha = 0.4f)))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Dual Mode Output Preview",
                                style = MaterialTheme.typography.labelSmall,
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "English: ${state.currentTextDraft.ifBlank { "..." }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = keyTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Bangla: ${state.liveTranslation.ifBlank { "Translating..." }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Top Toolbar / Candidate Bar
            ToolbarCandidateBar(
                state = state,
                primaryColor = primaryColor,
                keyBgColor = keyBgColor,
                keyTextColor = keyTextColor,
                onModeChange = {
                    performHaptic()
                    onModeChange(it)
                },
                onTabChange = {
                    performHaptic()
                    onTabChange(it)
                },
                onToggleAI = {
                    performHaptic()
                    onToggleAI()
                },
                onVoiceClick = {
                    performHaptic()
                    onVoiceClick()
                },
                onToggleOneHand = {
                    performHaptic()
                    onToggleOneHand()
                },
                onToggleFloating = {
                    performHaptic()
                    onToggleFloating()
                },
                onSuggestionClick = { suggestion ->
                    performHaptic()
                    onSuggestionClick(suggestion)
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Main Content depending on active tab
            when (state.activeTab) {
                KeyboardTab.KEYS -> {
                    MainKeypad(
                        state = state,
                        keyBgColor = keyBgColor,
                        keyTextColor = keyTextColor,
                        primaryColor = primaryColor,
                        onKeyClick = {
                            performHaptic()
                            onKeyClick(it)
                        },
                        onBackspaceClick = {
                            performHaptic()
                            onBackspaceClick()
                        },
                        onSpaceClick = {
                            performHaptic()
                            onSpaceClick()
                        },
                        onEnterClick = {
                            performHaptic()
                            onEnterClick()
                        },
                        onShiftClick = {
                            performHaptic()
                            onShiftClick()
                        },
                        onSymbolToggle = {
                            performHaptic()
                            onSymbolToggle()
                        },
                        onTabChange = {
                            performHaptic()
                            onTabChange(it)
                        }
                    )
                }
                KeyboardTab.EMOJI -> {
                    EmojiPanel(
                        keyBgColor = keyBgColor,
                        keyTextColor = keyTextColor,
                        onEmojiSelect = {
                            performHaptic()
                            onKeyClick(it)
                        },
                        onClose = {
                            performHaptic()
                            onTabChange(KeyboardTab.KEYS)
                        }
                    )
                }
                KeyboardTab.STICKERS -> {
                    StickersPanel(
                        keyBgColor = keyBgColor,
                        keyTextColor = keyTextColor,
                        primaryColor = primaryColor,
                        onStickerSelect = {
                            performHaptic()
                            onSuggestionClick(it)
                        },
                        onClose = {
                            performHaptic()
                            onTabChange(KeyboardTab.KEYS)
                        }
                    )
                }
                KeyboardTab.CLIPBOARD -> {
                    ClipboardPanel(
                        clipboardItems = clipboardItems,
                        keyBgColor = keyBgColor,
                        keyTextColor = keyTextColor,
                        primaryColor = primaryColor,
                        onItemClick = { text ->
                            performHaptic()
                            onSuggestionClick(text)
                        },
                        onPinClick = { id, pinned ->
                            performHaptic()
                            onPinClipboard(id, pinned)
                        },
                        onClearAll = {
                            performHaptic()
                            onClearClipboard()
                        },
                        onClose = {
                            performHaptic()
                            onTabChange(KeyboardTab.KEYS)
                        }
                    )
                }
                KeyboardTab.THEMES -> {
                    ThemePanel(
                        currentTheme = state.currentTheme,
                        onThemeSelect = { mode ->
                            performHaptic()
                            onThemeSelect(mode)
                        },
                        onClose = {
                            performHaptic()
                            onTabChange(KeyboardTab.KEYS)
                        }
                    )
                }
                KeyboardTab.SETTINGS -> {
                    QuickSettingsPanel(
                        state = state,
                        keyBgColor = keyBgColor,
                        keyTextColor = keyTextColor,
                        primaryColor = primaryColor,
                        onOpenSettings = onOpenSettings,
                        onClose = {
                            performHaptic()
                            onTabChange(KeyboardTab.KEYS)
                        }
                    )
                }
                else -> {
                    onTabChange(KeyboardTab.KEYS)
                }
            }
        }
    }
}

@Composable
fun ToolbarCandidateBar(
    state: KeyboardState,
    primaryColor: Color,
    keyBgColor: Color,
    keyTextColor: Color,
    onModeChange: (LanguageMode) -> Unit,
    onTabChange: (KeyboardTab) -> Unit,
    onToggleAI: () -> Unit,
    onVoiceClick: () -> Unit,
    onToggleOneHand: () -> Unit,
    onToggleFloating: () -> Unit,
    onSuggestionClick: (String) -> Unit
) {
    Column {
        // Row 1: Action Icons & Mode Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Mode Selectors (EN, Bangla, Dual)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LanguageModePill(
                    label = "EN",
                    isSelected = state.currentMode == LanguageMode.ENGLISH,
                    primaryColor = primaryColor,
                    keyBgColor = keyBgColor,
                    keyTextColor = keyTextColor,
                    onClick = { onModeChange(LanguageMode.ENGLISH) }
                )
                LanguageModePill(
                    label = "বাংলা",
                    isSelected = state.currentMode == LanguageMode.BANGLA,
                    primaryColor = primaryColor,
                    keyBgColor = keyBgColor,
                    keyTextColor = keyTextColor,
                    onClick = { onModeChange(LanguageMode.BANGLA) }
                )
                LanguageModePill(
                    label = "Dual",
                    isSelected = state.currentMode == LanguageMode.DUAL,
                    primaryColor = primaryColor,
                    keyBgColor = keyBgColor,
                    keyTextColor = keyTextColor,
                    onClick = { onModeChange(LanguageMode.DUAL) }
                )
            }

            // Quick Tool Icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // AI Toggle Button
                IconButton(
                    onClick = onToggleAI,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (state.isAITranslationEnabled) primaryColor.copy(alpha = 0.2f) else keyBgColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Translation",
                        tint = if (state.isAITranslationEnabled) primaryColor else keyTextColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Voice Mic Button
                IconButton(
                    onClick = onVoiceClick,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (state.isListeningVoice) primaryColor else keyBgColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (state.isListeningVoice) Color.White else keyTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Clipboard
                IconButton(
                    onClick = { onTabChange(KeyboardTab.CLIPBOARD) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = "Clipboard", tint = keyTextColor, modifier = Modifier.size(18.dp))
                }

                // Emojis & Stickers
                IconButton(
                    onClick = { onTabChange(KeyboardTab.EMOJI) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Mood, contentDescription = "Emojis", tint = keyTextColor, modifier = Modifier.size(18.dp))
                }

                // Themes
                IconButton(
                    onClick = { onTabChange(KeyboardTab.THEMES) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Palette, contentDescription = "Themes", tint = keyTextColor, modifier = Modifier.size(18.dp))
                }

                // Settings
                IconButton(
                    onClick = { onTabChange(KeyboardTab.SETTINGS) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = keyTextColor, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Row 2: Live AI Suggestions & Predictions Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 6.dp)
        ) {
            items(state.suggestions) { suggestion ->
                SuggestionChip(
                    onClick = { onSuggestionClick(suggestion) },
                    label = {
                        Text(
                            text = suggestion,
                            color = keyTextColor,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = keyBgColor
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = keyTextColor.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

@Composable
fun LanguageModePill(
    label: String,
    isSelected: Boolean,
    primaryColor: Color,
    keyBgColor: Color,
    keyTextColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) primaryColor else keyBgColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else keyTextColor
        )
    }
}

@Composable
fun MainKeypad(
    state: KeyboardState,
    keyBgColor: Color,
    keyTextColor: Color,
    primaryColor: Color,
    onKeyClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onSpaceClick: () -> Unit,
    onEnterClick: () -> Unit,
    onShiftClick: () -> Unit,
    onSymbolToggle: () -> Unit,
    onTabChange: (KeyboardTab) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Optional Top Number Row
        if (state.isNumbersRowVisible) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                KeyboardLayouts.numberRow.forEach { num ->
                    KeyButton(
                        label = num,
                        modifier = Modifier.weight(1f),
                        keyBgColor = keyBgColor.copy(alpha = 0.8f),
                        keyTextColor = keyTextColor,
                        onClick = { onKeyClick(num) }
                    )
                }
            }
        }

        if (state.isSymbols) {
            // Symbols Keypad
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                KeyboardLayouts.symbolsRow1.forEach { sym ->
                    KeyButton(label = sym, modifier = Modifier.weight(1f), keyBgColor = keyBgColor, keyTextColor = keyTextColor, onClick = { onKeyClick(sym) })
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                KeyboardLayouts.symbolsRow2.forEach { sym ->
                    KeyButton(label = sym, modifier = Modifier.weight(1f), keyBgColor = keyBgColor, keyTextColor = keyTextColor, onClick = { onKeyClick(sym) })
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                KeyboardLayouts.symbolsRow3.forEach { sym ->
                    KeyButton(label = sym, modifier = Modifier.weight(1f), keyBgColor = keyBgColor, keyTextColor = keyTextColor, onClick = { onKeyClick(sym) })
                }
            }
        } else if (state.currentMode == LanguageMode.BANGLA && !state.isShifted) {
            // Bangla Specific Keypad
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                KeyboardLayouts.banglaRow1.forEach { char ->
                    KeyButton(label = char, modifier = Modifier.weight(1f), keyBgColor = keyBgColor, keyTextColor = keyTextColor, onClick = { onKeyClick(char) })
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                KeyboardLayouts.banglaRow2.take(10).forEach { char ->
                    KeyButton(label = char, modifier = Modifier.weight(1f), keyBgColor = keyBgColor, keyTextColor = keyTextColor, onClick = { onKeyClick(char) })
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                KeyboardLayouts.banglaRow3.take(10).forEach { char ->
                    KeyButton(label = char, modifier = Modifier.weight(1f), keyBgColor = keyBgColor, keyTextColor = keyTextColor, onClick = { onKeyClick(char) })
                }
            }
        } else {
            // Standard QWERTY Layout
            val row1 = if (state.isShifted) KeyboardLayouts.qwertyRow1Upper else KeyboardLayouts.qwertyRow1Lower
            val row2 = if (state.isShifted) KeyboardLayouts.qwertyRow2Upper else KeyboardLayouts.qwertyRow2Lower
            val row3 = if (state.isShifted) KeyboardLayouts.qwertyRow3Upper else KeyboardLayouts.qwertyRow3Lower

            // Row 1
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                row1.forEach { letter ->
                    KeyButton(label = letter, modifier = Modifier.weight(1f), keyBgColor = keyBgColor, keyTextColor = keyTextColor, onClick = { onKeyClick(letter) })
                }
            }

            // Row 2
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Spacer(modifier = Modifier.weight(0.5f))
                row2.forEach { letter ->
                    KeyButton(label = letter, modifier = Modifier.weight(1f), keyBgColor = keyBgColor, keyTextColor = keyTextColor, onClick = { onKeyClick(letter) })
                }
                Spacer(modifier = Modifier.weight(0.5f))
            }

            // Row 3 (Shift + Letters + Backspace)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                // Shift Key
                KeyButton(
                    icon = Icons.Default.ArrowUpward,
                    modifier = Modifier.weight(1.5f),
                    keyBgColor = if (state.isShifted) primaryColor else keyBgColor.copy(alpha = 0.7f),
                    keyTextColor = if (state.isShifted) Color.White else keyTextColor,
                    onClick = onShiftClick
                )

                row3.forEach { letter ->
                    KeyButton(label = letter, modifier = Modifier.weight(1f), keyBgColor = keyBgColor, keyTextColor = keyTextColor, onClick = { onKeyClick(letter) })
                }

                // Backspace Key
                KeyButton(
                    icon = Icons.Default.Backspace,
                    modifier = Modifier.weight(1.5f),
                    keyBgColor = keyBgColor.copy(alpha = 0.7f),
                    keyTextColor = keyTextColor,
                    onClick = onBackspaceClick
                )
            }
        }

        // Row 4: Bottom Special Keys (Symbols toggle, Comma, Spacebar, Period, Enter)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Symbol Toggle Key (?123 / ABC)
            KeyButton(
                label = if (state.isSymbols) "ABC" else "?123",
                modifier = Modifier.weight(1.3f),
                keyBgColor = keyBgColor.copy(alpha = 0.8f),
                keyTextColor = keyTextColor,
                onClick = onSymbolToggle
            )

            // Emoji / Sticker Shortcut
            KeyButton(
                icon = Icons.Default.Mood,
                modifier = Modifier.weight(1f),
                keyBgColor = keyBgColor.copy(alpha = 0.8f),
                keyTextColor = keyTextColor,
                onClick = { onTabChange(KeyboardTab.EMOJI) }
            )

            // Comma
            KeyButton(
                label = ",",
                modifier = Modifier.weight(1f),
                keyBgColor = keyBgColor,
                keyTextColor = keyTextColor,
                onClick = { onKeyClick(",") }
            )

            // Spacebar
            KeyButton(
                label = when (state.currentMode) {
                    LanguageMode.ENGLISH -> "English (US)"
                    LanguageMode.BANGLA -> "বাংলা (Bangla)"
                    LanguageMode.DUAL -> "English + বাংলা (Dual)"
                },
                modifier = Modifier.weight(4.5f),
                keyBgColor = keyBgColor,
                keyTextColor = keyTextColor.copy(alpha = 0.8f),
                onClick = onSpaceClick
            )

            // Period
            KeyButton(
                label = ".",
                modifier = Modifier.weight(1f),
                keyBgColor = keyBgColor,
                keyTextColor = keyTextColor,
                onClick = { onKeyClick(".") }
            )

            // Enter / Send Key
            KeyButton(
                icon = Icons.AutoMirrored.Filled.Send,
                modifier = Modifier.weight(1.5f),
                keyBgColor = primaryColor,
                keyTextColor = Color.White,
                onClick = onEnterClick
            )
        }
    }
}

@Composable
fun KeyButton(
    modifier: Modifier = Modifier,
    label: String? = null,
    icon: ImageVector? = null,
    keyBgColor: Color,
    keyTextColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .shadow(1.dp, RoundedCornerShape(8.dp)),
        color = keyBgColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (label != null) {
                Text(
                    text = label,
                    color = keyTextColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = keyTextColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmojiPanel(
    keyBgColor: Color,
    keyTextColor: Color,
    onEmojiSelect: (String) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .background(keyBgColor.copy(alpha = 0.3f))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Emojis", style = MaterialTheme.typography.titleMedium, color = keyTextColor, fontWeight = FontWeight.Bold)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = keyTextColor)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(KeyboardLayouts.emojisList) { emoji ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onEmojiSelect(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 22.sp)
                }
            }
        }
    }
}

@Composable
fun StickersPanel(
    keyBgColor: Color,
    keyTextColor: Color,
    primaryColor: Color,
    onStickerSelect: (String) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .background(keyBgColor.copy(alpha = 0.3f))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Stickers & Quick Phrases", style = MaterialTheme.typography.titleMedium, color = keyTextColor, fontWeight = FontWeight.Bold)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = keyTextColor)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(KeyboardLayouts.stickersList) { sticker ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onStickerSelect(sticker) },
                    color = primaryColor.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Text(
                        text = sticker,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = keyTextColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ClipboardPanel(
    clipboardItems: List<ClipboardEntity>,
    keyBgColor: Color,
    keyTextColor: Color,
    primaryColor: Color,
    onItemClick: (String) -> Unit,
    onPinClick: (Long, Boolean) -> Unit,
    onClearAll: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .background(keyBgColor.copy(alpha = 0.3f))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = primaryColor)
                Spacer(Modifier.width(6.dp))
                Text("Clipboard History", style = MaterialTheme.typography.titleMedium, color = keyTextColor, fontWeight = FontWeight.Bold)
            }
            Row {
                TextButton(onClick = onClearAll) {
                    Text("Clear", color = primaryColor)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = keyTextColor)
                }
            }
        }

        if (clipboardItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Clipboard is empty. Copied text will appear here.", style = MaterialTheme.typography.bodyMedium, color = keyTextColor.copy(alpha = 0.6f))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(clipboardItems) { item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onItemClick(item.text) },
                        color = keyBgColor
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.text,
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = keyTextColor,
                                style = MaterialTheme.typography.bodySmall
                            )
                            IconButton(
                                onClick = { onPinClick(item.id, item.isPinned) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = "Pin",
                                    tint = if (item.isPinned) primaryColor else keyTextColor.copy(alpha = 0.4f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemePanel(
    currentTheme: ThemeMode,
    onThemeSelect: (ThemeMode) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .background(Color(currentTheme.bgHex))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Select Keyboard Theme", style = MaterialTheme.typography.titleMedium, color = Color(currentTheme.keyTextHex), fontWeight = FontWeight.Bold)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(currentTheme.keyTextHex))
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(ThemeMode.entries.toTypedArray()) { theme ->
                val isSelected = theme == currentTheme
                Surface(
                    modifier = Modifier
                        .size(width = 110.dp, height = 130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onThemeSelect(theme) },
                    color = Color(theme.bgHex),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, Color(theme.primaryHex)) else CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(theme.title, style = MaterialTheme.typography.labelMedium, color = Color(theme.keyTextHex), fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(theme.keyBgHex)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("A B C", color = Color(theme.primaryHex), fontWeight = FontWeight.Bold)
                        }
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(theme.primaryHex), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickSettingsPanel(
    state: KeyboardState,
    keyBgColor: Color,
    keyTextColor: Color,
    primaryColor: Color,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .background(keyBgColor.copy(alpha = 0.3f))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Keyboard Quick Settings", style = MaterialTheme.typography.titleMedium, color = keyTextColor, fontWeight = FontWeight.Bold)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = keyTextColor)
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Open Full App & Settings Dashboard", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SettingQuickItem(icon = Icons.Default.VolumeUp, label = "Sound", isEnabled = state.soundEnabled, keyTextColor = keyTextColor)
            SettingQuickItem(icon = Icons.Default.Vibration, label = "Vibration", isEnabled = state.vibrationEnabled, keyTextColor = keyTextColor)
            SettingQuickItem(icon = Icons.Default.AutoAwesome, label = "AI Translation", isEnabled = state.isAITranslationEnabled, keyTextColor = keyTextColor)
        }
    }
}

@Composable
fun SettingQuickItem(icon: ImageVector, label: String, isEnabled: Boolean, keyTextColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = if (isEnabled) MaterialTheme.colorScheme.primary else keyTextColor.copy(alpha = 0.4f))
        Text(label, style = MaterialTheme.typography.labelSmall, color = keyTextColor)
    }
}
