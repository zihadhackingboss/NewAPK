package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {
    @Query("SELECT * FROM clipboard_history ORDER BY isPinned DESC, timestamp DESC")
    fun getAllClipboardItems(): Flow<List<ClipboardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClipboard(item: ClipboardEntity)

    @Query("DELETE FROM clipboard_history WHERE id = :id")
    suspend fun deleteClipboardById(id: Long)

    @Query("DELETE FROM clipboard_history WHERE isPinned = 0")
    suspend fun clearUnpinnedClipboard()

    @Query("UPDATE clipboard_history SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean)
}

@Dao
interface DictionaryDao {
    @Query("SELECT * FROM personal_dictionary WHERE language = :language ORDER BY word ASC")
    fun getDictionaryByLanguage(language: String): Flow<List<DictionaryEntity>>

    @Query("SELECT * FROM personal_dictionary ORDER BY word ASC")
    fun getAllDictionary(): Flow<List<DictionaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(item: DictionaryEntity)

    @Query("DELETE FROM personal_dictionary WHERE id = :id")
    suspend fun deleteWordById(id: Long)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM keyboard_settings")
    fun getAllSettings(): Flow<List<KeyboardSettingEntity>>

    @Query("SELECT value FROM keyboard_settings WHERE key = :key LIMIT 1")
    suspend fun getSettingValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: KeyboardSettingEntity)
}
