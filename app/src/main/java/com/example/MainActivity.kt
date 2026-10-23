package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.ai.GeminiTranslationEngine
import com.example.ai.OfflineTranslator
import com.example.data.ClipboardEntity
import com.example.data.DictionaryEntity
import com.example.data.KeyboardRepository
import com.example.keyboard.LanguageMode
import com.example.keyboard.ThemeMode
import com.example.ui.theme.Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var repository: KeyboardRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repository = KeyboardRepository(applicationContext)

        setContent {
            Theme {
                MainAppScreen(
                    repository = repository,
                    onEnableKeyboardClick = {
                        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                    },
                    onSelectKeyboardClick = {
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showInputMethodPicker()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    repository: KeyboardRepository,
    onEnableKeyboardClick: () -> Unit,
    onSelectKeyboardClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val clipboardList by repository.clipboardItems.collectAsState(initial = emptyList())
    val dictionaryList by repository.dictionaryItems.collectAsState(initial = emptyList())

    val isKeyboardEnabled = remember { checkIsKeyboardEnabled(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("AI Smart Keyboard", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("EN + Bangla + Dual Mode AI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Build, contentDescription = null) },
                    label = { Text("Setup & Test") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Palette, contentDescription = null) },
                    label = { Text("Themes") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    label = { Text("Dictionary") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> SetupAndTestTab(
                    isKeyboardEnabled = isKeyboardEnabled,
                    onEnableClick = onEnableKeyboardClick,
                    onSelectClick = onSelectKeyboardClick
                )
                1 -> SettingsTab()
                2 -> ThemesTab()
                3 -> DictionaryAndClipboardTab(
                    repository = repository,
                    clipboardList = clipboardList,
                    dictionaryList = dictionaryList
                )
            }
        }
    }
}

@Composable
fun SetupAndTestTab(
    isKeyboardEnabled: Boolean,
    onEnableClick: () -> Unit,
    onSelectClick: () -> Unit
) {
    var testText by remember { mutableStateOf("") }
    var dualModePreviewEn by remember { mutableStateOf("I love you ❤️") }
    var dualModePreviewBn by remember { mutableStateOf("আমি তোমাকে ভালোবাসি ❤️") }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isKeyboardEnabled)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isKeyboardEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isKeyboardEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isKeyboardEnabled) "AI Smart Keyboard Active" else "Setup Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isKeyboardEnabled) "Keyboard is enabled and ready to use in any app!" else "Follow the steps below to enable AI Smart Keyboard.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Step 1 & Step 2 Buttons
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Keyboard Setup Steps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Button(
                        onClick = onEnableClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ToggleOn, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("1. Enable Keyboard in Android Settings")
                    }

                    Button(
                        onClick = onSelectClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.TouchApp, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("2. Switch Default Keyboard to AI Smart Keyboard")
                    }
                }
            }
        }

        // Live Interactive Test Canvas
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Live Keyboard Test Area", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Text("Tap the box below to bring up the keyboard and test English, Bangla, and Dual Mode output!", style = MaterialTheme.typography.bodySmall)

                    OutlinedTextField(
                        value = testText,
                        onValueChange = { input ->
                            testText = input
                            dualModePreviewEn = input.ifBlank { "I love you ❤️" }
                            scope.launch {
                                dualModePreviewBn = GeminiTranslationEngine.translateText(dualModePreviewEn, "en_to_bn")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Type here to test AI Smart Keyboard...") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Dual Mode Live Output Box Example
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Dual Mode Live Translation Preview", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("English Line:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(dualModePreviewEn, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text("Bangla Line:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(dualModePreviewBn, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsTab() {
    var activeMode by remember { mutableStateOf(LanguageMode.DUAL) }
    var isAIEnabled by remember { mutableStateOf(true) }
    var isVoiceEnabled by remember { mutableStateOf(true) }
    var autoCorrect by remember { mutableStateOf(true) }
    var numberRow by remember { mutableStateOf(true) }
    var vibration by remember { mutableStateOf(true) }
    var sound by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Keyboard & AI Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        // Language Modes
        item {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Default Language Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = activeMode == LanguageMode.ENGLISH,
                            onClick = { activeMode = LanguageMode.ENGLISH },
                            label = { Text("English Only") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = activeMode == LanguageMode.BANGLA,
                            onClick = { activeMode = LanguageMode.BANGLA },
                            label = { Text("Bangla Only") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = activeMode == LanguageMode.DUAL,
                            onClick = { activeMode = LanguageMode.DUAL },
                            label = { Text("Dual Mode") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Toggles Card
        item {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingToggleRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Online AI Translation (Gemini 3.5 Flash)",
                        subtitle = "High accuracy English-Bangla translation",
                        isChecked = isAIEnabled,
                        onCheckedChange = { isAIEnabled = it }
                    )
                    Divider(Modifier.padding(vertical = 8.dp))
                    SettingToggleRow(
                        icon = Icons.Default.Mic,
                        title = "Voice Input Recognition",
                        subtitle = "Speak in English or Bangla",
                        isChecked = isVoiceEnabled,
                        onCheckedChange = { isVoiceEnabled = it }
                    )
                    Divider(Modifier.padding(vertical = 8.dp))
                    SettingToggleRow(
                        icon = Icons.Default.Spellcheck,
                        title = "Auto-Correction & Predictions",
                        subtitle = "Smart next-word predictions",
                        isChecked = autoCorrect,
                        onCheckedChange = { autoCorrect = it }
                    )
                    Divider(Modifier.padding(vertical = 8.dp))
                    SettingToggleRow(
                        icon = Icons.Default.Numbers,
                        title = "Top Number Row",
                        subtitle = "Show dedicated numbers 1-0 row",
                        isChecked = numberRow,
                        onCheckedChange = { numberRow = it }
                    )
                    Divider(Modifier.padding(vertical = 8.dp))
                    SettingToggleRow(
                        icon = Icons.Default.Vibration,
                        title = "Haptic Vibration Feedback",
                        subtitle = "Soft keypress vibrations",
                        isChecked = vibration,
                        onCheckedChange = { vibration = it }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun ThemesTab() {
    var currentSelectedTheme by remember { mutableStateOf(ThemeMode.MATERIAL_YOU) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Keyboard Themes Gallery", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        items(ThemeMode.entries.toTypedArray()) { theme ->
            val isSelected = theme == currentSelectedTheme
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { currentSelectedTheme = theme },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(theme.bgHex)),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, Color(theme.primaryHex)) else CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(theme.title, style = MaterialTheme.typography.titleMedium, color = Color(theme.keyTextHex), fontWeight = FontWeight.Bold)
                        Text("Style Preview", style = MaterialTheme.typography.bodySmall, color = Color(theme.keyTextHex).copy(alpha = 0.7f))
                    }
                    Box(
                        modifier = Modifier
                            .size(height = 36.dp, width = 80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(theme.keyBgHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Key", color = Color(theme.primaryHex), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DictionaryAndClipboardTab(
    repository: KeyboardRepository,
    clipboardList: List<ClipboardEntity>,
    dictionaryList: List<DictionaryEntity>
) {
    var newWord by remember { mutableStateOf("") }
    var newReplacement by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Personal Dictionary & Clipboard", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        // Add Custom Word / Shortcut
        item {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Add Personal Word or Shortcut", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = newWord,
                        onValueChange = { newWord = it },
                        label = { Text("Shortcut / Word (e.g. omw)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newReplacement,
                        onValueChange = { newReplacement = it },
                        label = { Text("Replacement Text (e.g. On my way!)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (newWord.isNotBlank() && newReplacement.isNotBlank()) {
                                scope.launch {
                                    repository.addDictionaryWord(newWord, newReplacement)
                                    newWord = ""
                                    newReplacement = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add to Personal Dictionary")
                    }
                }
            }
        }

        // Clipboard Snippets List
        item {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recent Clipboard Items (${clipboardList.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { scope.launch { repository.clearUnpinnedClipboard() } }) {
                            Text("Clear Unpinned")
                        }
                    }

                    if (clipboardList.isEmpty()) {
                        Text("No items copied yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        clipboardList.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.text, modifier = Modifier.weight(1f), maxLines = 1)
                                IconButton(onClick = {
                                    scope.launch { repository.togglePinClipboard(item.id, item.isPinned) }
                                }) {
                                    Icon(
                                        Icons.Default.PushPin,
                                        contentDescription = "Pin",
                                        tint = if (item.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun checkIsKeyboardEnabled(context: Context): Boolean {
    return try {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledMethods = imm.enabledInputMethodList
        enabledMethods.any { it.packageName == context.packageName }
    } catch (e: Exception) {
        false
    }
}
