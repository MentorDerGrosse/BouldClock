package at.mentor.bouldclockapp.data.db.entity

import at.mentor.bouldclockapp.core.model.SyncState

/**
 * Sync-Spalten, die jede fachliche Tabelle mitfuehrt.
 *
 * Wird per `@Embedded` flach in die jeweilige Tabelle geschrieben, es entstehen
 * also echte Spalten `createdAt`, `updatedAt`, `deletedAt`, `syncState`.
 *
 * Warum jetzt schon, obwohl Stufe 1 rein lokal ist: Handy-Sync und spaeter Cloud
 * brauchen (a) global eindeutige IDs und (b) die Information, was sich seit wann
 * geaendert hat. Beides nachtraeglich einzufuehren heisst, jede Tabelle zu
 * migrieren. Jetzt kostet es vier Spalten.
 *
 * [deletedAt] ist ein Soft Delete: hart geloeschte Zeilen kann ein Sync nicht
 * uebertragen, der Loeschvorgang wuerde beim anderen Geraet nie ankommen.
 */
data class RecordMeta(
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val syncState: SyncState = SyncState.PENDING,
) {
    fun touched(now: Long): RecordMeta =
        copy(updatedAt = now, syncState = SyncState.PENDING)

    fun deleted(now: Long): RecordMeta =
        copy(deletedAt = now, updatedAt = now, syncState = SyncState.PENDING)

    companion object {
        fun now(clock: Long): RecordMeta = RecordMeta(createdAt = clock, updatedAt = clock)
    }
}
