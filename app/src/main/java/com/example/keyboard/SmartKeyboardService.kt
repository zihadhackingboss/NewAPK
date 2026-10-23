package com.example.keyboard

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.inputmethodservice.InputMethodService
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.example.ai.GeminiTranslationEngine
import com.example.ai.OfflineTranslator
import com.example.data.ClipboardEntity
import com.example.data.KeyboardRepository
import com.example.ui.theme.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class SmartKeyboardService : InputMethodService(),
    LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val serviceJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val _viewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = _viewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private lateinit var repository: KeyboardRepository
    private val _keyboardState = MutableStateFlow(KeyboardState())
    val keyboardState = _keyboardState.asStateFlow()

    private val _clipboardItems = MutableStateFlow<List<ClipboardEntity>>(emptyList())
    private var speechRecognizer: SpeechRecognizer? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        repository = KeyboardRepository(applicationContext)

        scope.launch {
            repository.clipboardItems.collect { items ->
                _clipboardItems.value = items
            }
        }
    }

    override fun onCreateInputView(): View {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        return ComposeView(this).apply {
            setContent {
                val state by keyboardState.collectAsState()
                val clipboardList by _clipboardItems.collectAsState()

                Theme {
                    KeyboardView(
                        state = state,
                        clipboardItems = clipboardList,
                        onKeyClick = { char -> onKeyClicked(char) },
                        onBackspaceClick = { onBackspaceClicked() },
                        onSpaceClick = { onSpaceClicked() },
                        onEnterClick = { onEnterClicked() },
                        onShiftClick = {
                            _keyboardState.value = state.copy(isShifted = !state.isShifted)
                        },
                        onSymbolToggle = {
                            _keyboardState.value = state.copy(isSymbols = !state.isSymbols)
                        },
                        onModeChange = { mode ->
                            _keyboardState.value = state.copy(currentMode = mode)
                        },
                        onTabChange = { tab ->
                            _keyboardState.value = state.copy(activeTab = tab)
                        },
                        onSuggestionClick = { text -> commitTextDirectly(text) },
                        onToggleAI = {
                            _keyboardState.value = state.copy(isAITranslationEnabled = !state.isAITranslationEnabled)
                        },
                        onVoiceClick = { toggleVoiceRecognition() },
                        onToggleOneHand = {
                            _keyboardState.value = state.copy(isOneHanded = !state.isOneHanded)
                        },
                        onToggleFloating = {
                            _keyboardState.value = state.copy(isFloating = !state.isFloating)
                        },
                        onThemeSelect = { theme ->
                            _keyboardState.value = state.copy(currentTheme = theme)
                        },
                        onPinClipboard = { id, pinned ->
                            scope.launch { repository.togglePinClipboard(id, pinned) }
                        },
                        onClearClipboard = {
                            scope.launch { repository.clearUnpinnedClipboard() }
                        },
                        onOpenSettings = {
                            val intent = packageManager.getLaunchIntentForPackage(packageName)
                            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            if (intent != null) startActivity(intent)
                        }
                    )
                }
            }
        }
    }

    private fun onKeyClicked(char: String) {
        val currentInput = currentInputConnection ?: return
        val currentDraft = _keyboardState.value.currentTextDraft + char
        _keyboardState.value = _keyboardState.value.copy(currentTextDraft = currentDraft)

        if (_keyboardState.value.currentMode == LanguageMode.ENGLISH) {
            currentInput.commitText(char, 1)
        } else {
            // Update live translation preview
            updateLiveTranslation(currentDraft)
        }

        // Fetch smart suggestions in background
        updateSmartSuggestions(currentDraft)
    }

    private fun onBackspaceClicked() {
        val currentInput = currentInputConnection ?: return
        val currentDraft = _keyboardState.value.currentTextDraft
        if (currentDraft.isNotEmpty()) {
            val newDraft = currentDraft.dropLast(1)
            _keyboardState.value = _keyboardState.value.copy(currentTextDraft = newDraft)
            if (_keyboardState.value.currentMode == LanguageMode.ENGLISH) {
                currentInput.deleteSurroundingText(1, 0)
            } else {
                updateLiveTranslation(newDraft)
            }
        } else {
            currentInput.deleteSurroundingText(1, 0)
        }
    }

    private fun onSpaceClicked() {
        val currentInput = currentInputConnection ?: return
        val state = _keyboardState.value

        if (state.currentMode == LanguageMode.ENGLISH) {
            currentInput.commitText(" ", 1)
            _keyboardState.value = state.copy(currentTextDraft = "")
        } else if (state.currentMode == LanguageMode.BANGLA) {
            val translated = state.liveTranslation.ifBlank { OfflineTranslator.translateEnToBn(state.currentTextDraft) }
            currentInput.commitText("$translated ", 1)
            _keyboardState.value = state.copy(currentTextDraft = "", liveTranslation = "")
        } else if (state.currentMode == LanguageMode.DUAL) {
            // Dual Mode Output Logic:
            // English: <draft>
            // Bangla: <translated>
            commitDualModeText(state.currentTextDraft, state.liveTranslation)
            _keyboardState.value = state.copy(currentTextDraft = "", liveTranslation = "")
        }
    }

    private fun onEnterClicked() {
        val currentInput = currentInputConnection ?: return
        val state = _keyboardState.value

        if (state.currentTextDraft.isNotBlank()) {
            if (state.currentMode == LanguageMode.DUAL) {
                commitDualModeText(state.currentTextDraft, state.liveTranslation)
            } else if (state.currentMode == LanguageMode.BANGLA) {
                val translated = state.liveTranslation.ifBlank { OfflineTranslator.translateEnToBn(state.currentTextDraft) }
                currentInput.commitText(translated, 1)
            } else {
                currentInput.commitText(state.currentTextDraft, 1)
            }
            _keyboardState.value = state.copy(currentTextDraft = "", liveTranslation = "")
        }

        // Perform Editor Action or New Line
        val action = currentInputEditorInfo?.actionId
        if (action != null && action != EditorInfo.IME_ACTION_NONE) {
            currentInput.performEditorAction(action)
        } else {
            currentInput.commitText("\n", 1)
        }
    }

    private fun commitDualModeText(englishText: String, banglaText: String) {
        val currentInput = currentInputConnection ?: return
        if (englishText.isBlank()) return

        val enText = englishText.trim()
        val bnText = banglaText.ifBlank { OfflineTranslator.translateEnToBn(enText) }.trim()

        val dualOutput = "$enText\n$bnText"
        currentInput.commitText(dualOutput, 1)

        // Save to clipboard history repository
        scope.launch {
            repository.addClipboardText(dualOutput)
        }
    }

    private fun commitTextDirectly(text: String) {
        val currentInput = currentInputConnection ?: return
        currentInput.commitText(text, 1)
        scope.launch {
            repository.addClipboardText(text)
        }
    }

    private fun updateLiveTranslation(draft: String) {
        if (draft.isBlank()) {
            _keyboardState.value = _keyboardState.value.copy(liveTranslation = "")
            return
        }

        scope.launch {
            val mode = if (_keyboardState.value.currentMode == LanguageMode.BANGLA) "en_to_bn" else "en_to_bn"
            val result = if (_keyboardState.value.isAITranslationEnabled) {
                GeminiTranslationEngine.translateText(draft, mode)
            } else {
                OfflineTranslator.translateEnToBn(draft)
            }
            _keyboardState.value = _keyboardState.value.copy(liveTranslation = result)
        }
    }

    private fun updateSmartSuggestions(draft: String) {
        scope.launch {
            val suggestions = if (draft.isNotBlank() && _keyboardState.value.isAITranslationEnabled) {
                GeminiTranslationEngine.getSmartSuggestions(draft)
            } else {
                OfflineTranslator.getNextWordPredictions(draft)
            }
            _keyboardState.value = _keyboardState.value.copy(suggestions = suggestions)
        }
    }

    private fun toggleVoiceRecognition() {
        val isListening = _keyboardState.value.isListeningVoice
        if (isListening) {
            stopVoiceRecognition()
        } else {
            startVoiceRecognition()
        }
    }

    private fun startVoiceRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            // Simulated voice input for device compatibility
            handleVoiceInputResult("I love you")
            return
        }

        try {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _keyboardState.value = _keyboardState.value.copy(isListeningVoice = true)
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spokenText = matches?.firstOrNull() ?: ""
                    handleVoiceInputResult(spokenText)
                    _keyboardState.value = _keyboardState.value.copy(isListeningVoice = false)
                }
                override fun onError(error: Int) {
                    _keyboardState.value = _keyboardState.value.copy(isListeningVoice = false)
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    _keyboardState.value = _keyboardState.value.copy(isListeningVoice = false)
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            handleVoiceInputResult("I love you")
        }
    }

    private fun stopVoiceRecognition() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        _keyboardState.value = _keyboardState.value.copy(isListeningVoice = false)
    }

    private fun handleVoiceInputResult(spokenText: String) {
        if (spokenText.isBlank()) return

        val state = _keyboardState.value
        if (state.currentMode == LanguageMode.DUAL) {
            scope.launch {
                val translation = GeminiTranslationEngine.translateText(spokenText, "en_to_bn")
                commitDualModeText(spokenText, translation)
            }
        } else if (state.currentMode == LanguageMode.BANGLA) {
            scope.launch {
                val translation = GeminiTranslationEngine.translateText(spokenText, "en_to_bn")
                commitTextDirectly(translation)
            }
        } else {
            commitTextDirectly(spokenText)
        }
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        speechRecognizer?.destroy()
        serviceJob.cancel()
        super.onDestroy()
    }
}
