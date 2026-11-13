package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "freegram_prefs")

class UserPreferences(private val context: Context) {
    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val HAS_CUSTOM_THEME = booleanPreferencesKey("has_custom_theme")
        val LOGGED_IN_USER_ID = stringPreferencesKey("logged_in_user_id")
    }

    val isDarkMode: Flow<Boolean?> = context.userPrefsDataStore.data.map { prefs ->
        if (prefs[Keys.HAS_CUSTOM_THEME] == true) {
            prefs[Keys.DARK_MODE] ?: true
        } else {
            null
        }
    }

    val loggedInUserId: Flow<String?> = context.userPrefsDataStore.data.map { prefs ->
        prefs[Keys.LOGGED_IN_USER_ID]
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[Keys.HAS_CUSTOM_THEME] = true
            prefs[Keys.DARK_MODE] = enabled
        }
    }

    suspend fun setLoggedInUserId(userId: String?) {
        context.userPrefsDataStore.edit { prefs ->
            if (userId == null) {
                prefs.remove(Keys.LOGGED_IN_USER_ID)
            } else {
                prefs[Keys.LOGGED_IN_USER_ID] = userId
            }
        }
    }
}
