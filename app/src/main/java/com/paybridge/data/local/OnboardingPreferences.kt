package com.paybridge.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding")

/** Tracks whether the one-time first-launch trust screen has been shown. */
class OnboardingPreferences(private val context: Context) {
    private val hasSeenTrustScreenKey = booleanPreferencesKey("has_seen_trust_screen")

    val hasSeenTrustScreen: Flow<Boolean> =
        context.dataStore.data.map { it[hasSeenTrustScreenKey] ?: false }

    suspend fun markTrustScreenSeen() {
        context.dataStore.edit { it[hasSeenTrustScreenKey] = true }
    }
}
