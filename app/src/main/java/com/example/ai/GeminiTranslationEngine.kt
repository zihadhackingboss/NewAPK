package com.example.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiTranslationEngine {
    private const val TAG = "GeminiTranslation"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    suspend fun translateText(text: String, sourceMode: String): String = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext ""
        
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "API key missing or default, using offline translator")
            return@withContext performOfflineTranslation(text, sourceMode)
        }

        try {
            val prompt = when (sourceMode) {
                "en_to_bn" -> "Translate the following text from English to natural Bengali/Bangla. Return ONLY the Bengali translation text, nothing else:\n\"$text\""
                "bn_to_en" -> "Translate the following text from Bengali/Bangla to natural English. Return ONLY the English translation text, nothing else:\n\"$text\""
                else -> "Translate the following English or Bangla text to Bengali (Bangla). Return ONLY the translation text:\n\"$text\""
            }

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseStr = response.body?.string() ?: ""
                    val parsedText = extractTextFromGeminiResponse(responseStr)
                    if (parsedText.isNotBlank()) {
                        return@withContext parsedText.trim()
                    }
                } else {
                    Log.w(TAG, "Gemini API error ${response.code}: ${response.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to translate via Gemini API", e)
        }

        // Fallback
        return@withContext performOfflineTranslation(text, sourceMode)
    }

    suspend fun getSmartSuggestions(input: String): List<String> = withContext(Dispatchers.IO) {
        if (input.isBlank()) return@withContext listOf("How are you?", "I love you ❤️", "Thank you!", "Good morning")

        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = "Give 3 short, natural sentence continuations or smart replies for the typing input: \"$input\". Return ONLY a comma-separated list of short suggestions without quotes."
                val jsonBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                            })
                        })
                    })
                }

                val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL?key=$apiKey")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseStr = response.body?.string() ?: ""
                        val text = extractTextFromGeminiResponse(responseStr)
                        if (text.isNotBlank()) {
                            val items = text.split(",").map { it.trim().removeSurrounding("\"") }.filter { it.isNotBlank() }
                            if (items.isNotEmpty()) return@withContext items.take(4)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gemini smart suggestions error", e)
            }
        }

        return@withContext OfflineTranslator.getNextWordPredictions(input)
    }

    private fun performOfflineTranslation(text: String, sourceMode: String): String {
        return when (sourceMode) {
            "bn_to_en" -> OfflineTranslator.translateBnToEn(text)
            else -> OfflineTranslator.translateEnToBn(text)
        }
    }

    private fun extractTextFromGeminiResponse(jsonString: String): String {
        try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates") ?: return ""
            if (candidates.length() == 0) return ""
            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            if (parts.length() == 0) return ""
            return parts.getJSONObject(0).optString("text", "")
        } catch (e: Exception) {
            return ""
        }
    }
}
