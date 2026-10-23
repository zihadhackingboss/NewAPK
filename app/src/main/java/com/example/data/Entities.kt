package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clipboard_history")
data class ClipboardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

@Entity(tableName = "personal_dictionary")
data class DictionaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word: String,
    val replacement: String,
    val language: String = "en", // "en" or "bn"
    val isUserAdded: Boolean = true
)

@Entity(tableName = "keyboard_settings")
data class KeyboardSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
