package at.mentor.bouldclockapp.data.sync

import at.mentor.bouldclockapp.core.model.BiologicalSex
import at.mentor.bouldclockapp.core.model.SyncState
import at.mentor.bouldclockapp.data.db.entity.RecordMeta
import at.mentor.bouldclockapp.data.db.entity.UserProfileEntity
import org.json.JSONObject

/**
 * Das Nutzerprofil als uebertragbares Paket.
 *
 * Eigener Weg neben den Sessions, weil es in beide Richtungen laeuft: angelegt
 * wird es meist auf der Uhr, bearbeitet nur am Handy. Welche Seite gewinnt,
 * entscheidet `updatedAt` - keine muss die andere fragen.
 */
data class ProfilePayload(val profile: UserProfileEntity) {

    fun toJson(): String = JSONObject().apply {
        put("weightKg", profile.weightKg)
        put("birthYear", profile.birthYear)
        put("sex", profile.sex.name)
        put("createdAt", profile.meta.createdAt)
        put("updatedAt", profile.meta.updatedAt)
    }.toString()

    companion object {
        fun fromJson(raw: String): ProfilePayload {
            val json = JSONObject(raw)
            return ProfilePayload(
                UserProfileEntity(
                    weightKg = json.getInt("weightKg"),
                    birthYear = json.getInt("birthYear"),
                    sex = BiologicalSex.entries
                        .firstOrNull { it.name == json.optString("sex") }
                        ?: BiologicalSex.UNSPECIFIED,
                    meta = RecordMeta(
                        createdAt = json.optLong("createdAt", 0L),
                        updatedAt = json.optLong("updatedAt", 0L),
                        syncState = SyncState.SYNCED,
                    ),
                ),
            )
        }
    }
}
