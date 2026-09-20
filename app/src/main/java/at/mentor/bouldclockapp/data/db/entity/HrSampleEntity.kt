package at.mentor.bouldclockapp.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey

/**
 * Ein Pulswert, 1 Hz.
 *
 * Bleibt bewusst in Room und wandert nicht in eine Datei: die Werte werden
 * abgefragt (HRR60, Kurve, Mittelwerte pro Versuch). Bei 1 Hz sind das ~7.000
 * Zeilen pro Session - fuer SQLite belanglos. Die Rohsensorik (Accel/Gyro/Baro,
 * 50+ Hz) geht dagegen in Dateien, siehe [SensorChunkEntity].
 *
 * Zusammengesetzter Primaerschluessel statt eigener ID: verhindert Duplikate beim
 * Wiederaufsetzen nach einem Absturz und spart den Index.
 *
 * Kein [RecordMeta] - Massendaten werden nicht einzeln synchronisiert, sondern
 * am Stueck mit der Session.
 */
@Entity(
    tableName = "hr_sample",
    primaryKeys = ["sessionId", "timestampMs"],
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    // Kein zusaetzlicher Index: der Primaerschluessel (sessionId, timestampMs) deckt
    // jede Abfrage ab. Bei 1 Hz waere jeder weitere Index 7.000 Writes pro Session umsonst.
)
data class HrSampleEntity(
    val sessionId: String,
    val timestampMs: Long,
    val bpm: Int,

    /**
     * Genauigkeit laut Sensor. Beim Bouldern wichtiger als sonst: waehrend eines
     * Zuges ist das optische Signal am Handgelenk oft unbrauchbar (gebeugtes
     * Gelenk, kontrahierter Unterarm, Griffdruck). Schlechte Samples muessen aus
     * der Auswertung fliegen koennen statt stillschweigend den Mittelwert zu ziehen.
     */
    val accuracy: Int,
)
