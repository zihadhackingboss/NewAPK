package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class KeyboardRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val clipboardDao = db.clipboardDao()
    private val dictionaryDao = db.dictionaryDao()
    private val settingsDao = db.settingsDao()

    val clipboardItems: Flow<List<ClipboardEntity>> = clipboardDao.getAllClipboardItems()
    val dictionaryItems: Flow<List<DictionaryEntity>> = dictionaryDao.getAllDictionary()

    suspend fun addClipboardText(text: String) {
        if (text.isBlank()) return
        clipboardDao.insertClipboard(ClipboardEntity(text = text.trim()))
    }

    suspend fun togglePinClipboard(id: Long, currentPin: Boolean) {
        clipboardDao.setPinned(id, !currentPin)
    }

    suspend fun deleteClipboardItem(id: Long) {
        clipboardDao.deleteClipboardById(id)
    }

    suspend fun clearUnpinnedClipboard() {
        clipboardDao.clearUnpinnedClipboard()
    }

    suspend fun addDictionaryWord(word: String, replacement: String, lang: String = "en") {
        if (word.isBlank() || replacement.isBlank()) return
        dictionaryDao.insertWord(DictionaryEntity(word = word.trim(), replacement = replacement.trim(), language = lang))
    }

    suspend fun deleteDictionaryWord(id: Long) {
        dictionaryDao.deleteWordById(id)
    }

    suspend fun getSetting(key: String, defaultValue: String): String {
        return settingsDao.getSettingValue(key) ?: defaultValue
    }

    suspend fun saveSetting(key: String, value: String) {
        settingsDao.saveSetting(KeyboardSettingEntity(key, value))
    }
}
