package com.asmaster.adblocker

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

const val YT = "com.google.android.youtube"

val Context.store by preferencesDataStore("as_master")

object K {
    val AUTO = booleanPreferencesKey("auto")
    val LANG = stringPreferencesKey("lang")
    val THEME = stringPreferencesKey("theme")
    val ANIM = booleanPreferencesKey("anim")
    val STATS = stringPreferencesKey("stats")
    val CUSTOM_DNS = stringPreferencesKey("custom_dns")
    val DNS_SEL = stringPreferencesKey("dns_sel")
}

data class Prefs(
    val auto: Boolean = true,
    val lang: String = "bn",
    val theme: String = "dark",
    val anim: Boolean = true,
    val customDns: String = "",
    val dnsSel: String = ""
)

fun Preferences.toPrefs() = Prefs(
    auto = this[K.AUTO] ?: true,
    lang = this[K.LANG] ?: "bn",
    theme = this[K.THEME] ?: "dark",
    anim = this[K.ANIM] ?: true,
    customDns = this[K.CUSTOM_DNS] ?: "",
    dnsSel = this[K.DNS_SEL] ?: ""
)

fun dayKey(offset: Int = 0): String {
    val c = Calendar.getInstance()
    c.add(Calendar.DAY_OF_YEAR, offset)
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.time)
}

fun parseStats(s: String?): Map<String, Int> =
    s.orEmpty().split(",").mapNotNull {
        val p = it.split(":")
        if (p.size == 2) p[1].toIntOrNull()?.let { n -> p[0] to n } else null
    }.toMap()

fun Map<String, Int>.sumDays(days: Int): Int = (0 until days).sumOf { this[dayKey(-it)] ?: 0 }

object Repo {
    fun prefs(c: Context): Flow<Prefs> = c.store.data.map { it.toPrefs() }
    fun stats(c: Context): Flow<Map<String, Int>> = c.store.data.map { parseStats(it[K.STATS]) }

    /** Called only after a Skip button click action really succeeded. */
    suspend fun recordSkip(c: Context) {
        c.store.edit { p ->
            val m = parseStats(p[K.STATS]).toMutableMap()
            val d = dayKey()
            m[d] = (m[d] ?: 0) + 1
            p[K.STATS] = m.entries.joinToString(",") { "${it.key}:${it.value}" }
        }
    }

    suspend fun resetStats(c: Context) {
        c.store.edit { it.remove(K.STATS) }
    }

    suspend fun <T> put(c: Context, key: Preferences.Key<T>, v: T) {
        c.store.edit { it[key] = v }
    }
}
