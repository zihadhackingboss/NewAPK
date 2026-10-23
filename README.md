# ⌨️ AI Smart Keyboard (English + Bangla + Dual Mode)

A modern, full-featured Android Input Method Editor (IME) built with **Kotlin** and **Jetpack Compose**. Powered by **Gemini 3.5 Flash**, it provides real-time English to Bengali (Bangla) translation, smart conversational predictions, an exclusive **Dual Mode** (bilingual text output), speech recognition, clipboard manager, and themes.

---

## ✨ Features

- **🌐 3 Language Modes**:
  - **English Mode**: Fluid QWERTY typing with real-time next-word predictions.
  - **Bangla Mode**: Live conversion to natural Bengali script.
  - **Dual Mode (Unique Bilingual Output)**: Automatically outputs both English and natural Bangla translation on separate lines:
    ```
    I love you
    আমি তোমাকে ভালোবাসি
    ```
- **⚡ Dual-Engine Translation**:
  - **Online AI (Gemini 3.5 Flash)**: Contextual, highly accurate translation using the Google Generative Language API.
  - **Offline Fallback Engine**: Instant offline dictionary & phonetic translation when internet is unavailable or without an API key.
- **🎙️ Voice Typing**: Speak in English or Bangla for instant hands-free dictation with automatic translation.
- **📋 Smart Clipboard Manager**: Automatically stores copied and committed phrases. Pin favorite snippets, search, or clear unpinned history.
- **🎨 Theme Engine**: Switch between 6 handcrafted styles:
  - Material You (Dynamic)
  - Midnight OLED Dark
  - Cyber Neon
  - Rose Gold
  - Emerald Slate
  - Soft Lavender
- **📐 Ergonomic Layouts**: Toggle **One-Handed Mode** (left/right compact) and **Floating Mode** anytime.
- **📖 Personal Dictionary & Shortcuts**: Define custom shortcuts (e.g., `omw` → `On my way!`) that expand automatically.
- **😄 Emojis, Symbols & Kaomoji**: Built-in tabs for emojis, currency/math symbols, and ASCII expressions (`¯\_(ツ)_/¯`, `(⁠•⁠_⁠•⁠)`, `(⁠⌐⁠■⁠-⁠■⁠)`).

---

## 🚀 Quick Setup & Activation

1. **Launch the AI Smart Keyboard App** on your device or emulator.
2. Under the **Setup & Test** tab:
   - Tap **1. Enable Keyboard in Android Settings** → Toggle on **AI Smart Keyboard**.
   - Tap **2. Switch Default Keyboard** → Select **AI Smart Keyboard** as your active input method.
3. Use the **Live Keyboard Test Area** inside the app to try typing in English, Bangla, and Dual Mode!

---

## 🔑 API Key & Environment Configuration

The app integrates with Google Gemini API for high-accuracy translation and smart suggestions.

### Configuration via `.env`:
1. The project includes `.env` and `.env.example`:
   ```properties
   # Gemini API Key for online English <-> Bangla translation and smart predictions
   GEMINI_API_KEY=YOUR_GEMINI_API_KEY_HERE
   ```
2. In Google AI Studio:
   - Navigate to the **Secrets panel**.
   - Set `GEMINI_API_KEY` to your valid Gemini API key.
   - The platform automatically injects this secret into `BuildConfig.GEMINI_API_KEY` during compilation.
3. If no key is set or the device is offline, the keyboard seamlessly falls back to the built-in **Offline Translation Engine**.

> [!CAUTION]
> **Security Warning**: Please be aware that Android APKs can be easily decompiled, and secrets embedded directly in APK builds can be extracted. **Do not share release APK files containing private API keys publicly or with unauthorized individuals**. For production deployments, route AI requests through a secure backend service (e.g., Firebase Cloud Functions).

---

## 🛠️ Architecture & Tech Stack

- **Android IME Framework**: `InputMethodService` hosting a lifecycle-aware `ComposeView`.
- **UI & Design**: Modern **Jetpack Compose** with **Material Design 3 (M3)** components, responsive insets, and dynamic themes.
- **Persistence**: **Room Database** (`ClipboardDao`, `DictionaryDao`) with Kotlin Coroutines & `Flow` for real-time history.
- **Networking**: `OkHttpClient` with 60-second timeouts and automatic retry logic for robust Gemini API communication.
- **Lifecycle & State**: `StateFlow` and unidirectional data flow (`KeyboardState`) ensuring 60fps responsive keypresses and haptic feedback.
