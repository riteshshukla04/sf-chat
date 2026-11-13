package dev.softwarefactory.chatter.data

import android.content.Context
import dev.softwarefactory.chatter.state.KeyValueStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** On-device storage in SharedPreferences (the AsyncStorage equivalent). */
class PrefsStorage(context: Context) : KeyValueStorage {
    private val prefs = context.applicationContext.getSharedPreferences("chatter", Context.MODE_PRIVATE)
    override suspend fun getItem(key: String): String? = withContext(Dispatchers.IO) { prefs.getString(key, null) }
    override suspend fun setItem(key: String, value: String) = withContext(Dispatchers.IO) { prefs.edit().putString(key, value).commit(); Unit }
    override suspend fun removeItem(key: String) = withContext(Dispatchers.IO) { prefs.edit().remove(key).commit(); Unit }
}
