package at.mentor.bouldclockapp.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.SessionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Nutzereinstellungen.
 *
 * Bewusst DataStore und nicht Room: das sind einzelne Werte ohne Historie und
 * ohne Sync. Alles, was spaeter ausgewertet werden soll, gehoert dagegen in die
 * Datenbank - deshalb liegt die Soll-Pause zusaetzlich an der Session selbst.
 */
class AppSettings(private val context: Context) {

    val gradeSystem: Flow<GradeSystem> = context.settingsStore.data.map { prefs ->
        prefs[KEY_GRADE_SYSTEM]
            ?.let { stored -> GradeSystem.entries.firstOrNull { it.name == stored } }
            ?: GradeSystem.FONT
    }

    suspend fun setGradeSystem(system: GradeSystem) {
        context.settingsStore.edit { it[KEY_GRADE_SYSTEM] = system.name }
    }

    /** Faellt auf [SessionType.defaultRestMs] zurueck, solange nichts eingestellt wurde. */
    fun restTargetMs(type: SessionType): Flow<Long> = context.settingsStore.data.map { prefs ->
        prefs[restKey(type)] ?: type.defaultRestMs
    }

    suspend fun setRestTargetMs(type: SessionType, ms: Long) {
        context.settingsStore.edit { it[restKey(type)] = ms }
    }

    private companion object {
        val KEY_GRADE_SYSTEM = stringPreferencesKey("grade_system")

        /** Je Sessiontyp eine eigene Pause - Limit und Volumen sind verschiedene Sportarten. */
        fun restKey(type: SessionType) = longPreferencesKey("rest_target_${type.name}")
    }
}
