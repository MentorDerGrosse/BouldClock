package at.mentor.bouldclockapp.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import at.mentor.bouldclockapp.core.model.SensorKind
import at.mentor.bouldclockapp.core.model.SyncState

/**
 * Verweis auf eine Rohsensordatei.
 *
 * Accel/Gyro/Baro bei 50 Hz sind ~5-10 MB pro Session. Eine Zeile pro Sample waere
 * das Ende der Datenbank, und abgefragt wird das Zeug ohnehin nicht - es wird am
 * Stueck gelesen, um spaeter die automatische Versuchserkennung zu trainieren.
 * Also: Daten in eine komprimierte Datei, hier nur die Metadaten.
 *
 * Deshalb wird schon in Stufe 1 aufgezeichnet, obwohl noch nichts damit passiert:
 * ohne gelabelten Datensatz aus echten Sessions gibt es die Automatik nie.
 */
@Entity(
    tableName = "sensor_chunk",
    indices = [Index("sessionId")],
)
data class SensorChunkEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val sensor: SensorKind,

    /** Relativ zum App-internen Speicher, nicht absolut. */
    val relativePath: String,
    val startedAt: Long,
    val endedAt: Long,
    val sampleRateHz: Int,
    val sampleCount: Int,
    val sizeBytes: Long,

    /** Nach dem Sync aufs Handy wird die Datei auf der Uhr geloescht. */
    val syncState: SyncState = SyncState.PENDING,
)
