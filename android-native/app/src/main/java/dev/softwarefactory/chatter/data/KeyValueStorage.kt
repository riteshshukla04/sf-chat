package dev.softwarefactory.chatter.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

/** The small slice of AsyncStorage the app uses. Injectable so tests can use [InMemoryStorage]. */
interface KeyValueStorage {
    suspend fun getItem(key: String): String?
    suspend fun setItem(key: String, value: String)
    suspend fun removeItem(key: String)
}

class InMemoryStorage(initial: Map<String, String> = emptyMap()) : KeyValueStorage {
    val values: MutableMap<String, String> = initial.toMutableMap()
    override suspend fun getItem(key: String): String? = values[key]
    override suspend fun setItem(key: String, value: String) { values[key] = value }
    override suspend fun removeItem(key: String) { values.remove(key) }
}

private val Context.chatterDataStore by preferencesDataStore(name = "chatter")

/** Jetpack DataStore (Preferences) backed storage; the whole app state is one JSON string under one key. */
class DataStoreStorage(private val context: Context) : KeyValueStorage {
    override suspend fun getItem(key: String): String? =
        context.chatterDataStore.data.first()[stringPreferencesKey(key)]

    override suspend fun setItem(key: String, value: String) {
        context.chatterDataStore.edit { it[stringPreferencesKey(key)] = value }
    }

    override suspend fun removeItem(key: String) {
        context.chatterDataStore.edit { it.remove(stringPreferencesKey(key)) }
    }
}
