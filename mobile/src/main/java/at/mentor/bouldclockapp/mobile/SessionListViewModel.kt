package at.mentor.bouldclockapp.mobile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import at.mentor.bouldclockapp.data.db.BouldClockDatabase
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Was am Handy angekommen ist.
 *
 * Dieselbe Datenbank wie auf der Uhr - dieselben Entitaeten, dieselben
 * Migrationen, aus dem geteilten Modul. Nur befuellt wird sie hier nicht durch
 * Klettern, sondern durch die Synchronisierung.
 */
class SessionListViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BouldClockDatabase.get(application)

    val summaries: StateFlow<List<SessionSummaryEntity>> =
        db.sessionSummaryDao().observeRecent(RECENT_LIMIT)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private companion object {
        /** Am Handy darf es ruhig mehr sein als auf der Uhr. */
        const val RECENT_LIMIT = 200
    }
}
