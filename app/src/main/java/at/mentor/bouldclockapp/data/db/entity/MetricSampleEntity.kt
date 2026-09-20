package at.mentor.bouldclockapp.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import at.mentor.bouldclockapp.core.model.SessionMetric

/**
 * Fortlaufender Stand einer Messgroesse, so wie Health Services ihn meldet.
 *
 * Als Verlauf und nicht als Endsumme: daraus laesst sich beides ableiten - der
 * Wert der ganzen Session (letzter Punkt) und der Anteil eines einzelnen
 * Versuchs (Differenz zwischen Beginn und Ende). Nur die Summe zu speichern
 * wuerde den Wandanteil unwiederbringlich verschenken.
 *
 * Eine Tabelle fuer alle Groessen, weil sie sich technisch nicht unterscheiden.
 * Der Puls bleibt aussen vor - der hat eine Genauigkeit und ist nicht kumulativ.
 *
 * Kein [RecordMeta] - Massendaten werden am Stueck mit der Session synchronisiert.
 */
@Entity(
    tableName = "metric_sample",
    primaryKeys = ["sessionId", "metric", "timestampMs"],
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class MetricSampleEntity(
    val sessionId: String,
    val metric: SessionMetric,
    val timestampMs: Long,

    /** Stand seit Beginn der Aufzeichnung, monoton steigend. */
    val value: Double,
)
