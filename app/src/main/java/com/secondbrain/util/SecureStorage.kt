package com.secondbrain.util

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure storage utility for storing sensitive information like API keys
 * Uses Android's EncryptedSharedPreferences for encryption
 */
@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SecureStorage"
        private const val ENCRYPTED_PREFS_FILE_NAME = "secure_prefs"

        // Keys for API keys
        const val KEY_GEMINI_API_KEY = "gemini_api_key"
        const val KEY_OPENAI_API_KEY = "openai_api_key"
        const val KEY_CLAUDE_API_KEY = "claude_api_key"
        const val KEY_DEEPSEEK_API_KEY = "deepseek_api_key"
        const val KEY_OPENROUTER_API_KEY = "openrouter_api_key"
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedSharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFS_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating EncryptedSharedPreferences", e)
            // Fallback to regular SharedPreferences in case of error
            context.getSharedPreferences(ENCRYPTED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * Store a string value securely
     */
    fun storeString(key: String, value: String) {
        try {
            encryptedSharedPreferences.edit().putString(key, value).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error storing string for key: $key", e)
        }
    }

    /**
     * Alias for storeString for consistency
     */
    fun putString(key: String, value: String) {
        storeString(key, value)
    }

    /**
     * Retrieve a string value securely
     */
    fun getString(key: String, defaultValue: String = ""): String {
        return try {
            encryptedSharedPreferences.getString(key, defaultValue) ?: defaultValue
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving string for key: $key", e)
            defaultValue
        }
    }

    /**
     * Check if a key exists
     */
    fun containsKey(key: String): Boolean {
        return try {
            encryptedSharedPreferences.contains(key)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if key exists: $key", e)
            false
        }
    }

    /**
     * Remove a key-value pair
     */
    fun remove(key: String) {
        try {
            encryptedSharedPreferences.edit().remove(key).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error removing key: $key", e)
        }
    }

    /**
     * Clear all stored values
     */
    fun clearAll() {
        try {
            encryptedSharedPreferences.edit().clear().apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing all values", e)
        }
    }
}
