package at.mentor.bouldclockapp.core.model

/**
 * Skala, in der ein Grad angezeigt wird.
 *
 * Wichtig fuers Verstaendnis: [FONT] *ist* die franzoesische Skala. Fontainebleau
 * ist der Wald bei Paris, in dem sie entstanden ist, und sie ist die uebliche
 * Bewertung fuer Boulder in Europa - 6A, 6B+, 7A.
 *
 * Nicht zu verwechseln mit der franzoesischen *Routen*-Skala (Kleinbuchstaben:
 * 6a, 7b, 9b+). Die gilt fuers Seilklettern und ist eine andere Disziplin mit
 * anderer Leiter. Sie waere kein weiterer Eintrag in [Grades], sondern eine
 * eigene Leiter - sonst wuerde die App Werte vergleichen, die nichts miteinander
 * zu tun haben. Beim Eingeben ist die Schreibweise egal: [Grades.parse] liest
 * "6a" genauso wie "6A".
 */
enum class GradeSystem(val displayName: String) {
    /** Fontainebleau, Grossbuchstaben: 3 bis 9A. In Europa der Standard. */
    FONT("Font"),

    /** Hueco / V-Scale: VB bis V17. Im englischsprachigen Raum ueblich. */
    V_SCALE("V-Scale"),

    /** Hallenfarben. Braucht eine Zuordnung je Halle, noch nicht verdrahtet. */
    COLOR("Farben"),
    ;

    /** Skalen mit fester Leiter koennen direkt in einen Picker. */
    val hasFixedLadder: Boolean get() = this != COLOR
}

/**
 * Die kanonische Schwierigkeitsleiter der App.
 *
 * Gespeichert wird ueberall nur ein `gradeValue: Int` - die Stufe auf dieser
 * Leiter. Jede Skala ist nur eine andere Beschriftung derselben Stufe. Damit
 * bleiben Grade sortierbar, mittelbar und ueber Skalen hinweg vergleichbar,
 * und der Int passt direkt auf den Drehkranz: ein Bezel-Schritt = eine Stufe.
 *
 * ERWEITERN:
 * - Neue Stufe (z. B. falls sich 9A+ etabliert): in *jeder* Liste in [LABELS]
 *   an derselben Position ergaenzen.
 * - Neue Skala: Wert in [GradeSystem] anlegen und eine gleich lange Liste in
 *   [LABELS] eintragen. Sonst ist nichts anzupassen - [pickerValues], [parse]
 *   und [label] arbeiten generisch ueber die Map.
 *
 * `GradesTest` sichert ab, dass alle Leitern gleich lang und in sich
 * widerspruchsfrei bleiben.
 */
object Grades {

    private val LABELS: Map<GradeSystem, List<String>> = mapOf(
        GradeSystem.FONT to listOf(
            "3", "4", "4+", "5", "5+",
            "6A", "6A+", "6B", "6B+", "6C", "6C+",
            "7A", "7A+", "7B", "7B+", "7C", "7C+",
            "8A", "8A+", "8B", "8B+", "8C", "8C+", "9A",
        ),
        // Bewusst nicht bijektiv: V ist groeber als Font, mehrere Stufen teilen
        // sich eine Beschriftung. pickerValues() faltet die Dopplungen zusammen.
        GradeSystem.V_SCALE to listOf(
            "VB", "V0", "V0", "V1", "V2",
            "V3", "V3", "V4", "V4", "V5", "V5",
            "V6", "V7", "V8", "V8", "V9", "V10",
            "V11", "V12", "V13", "V14", "V15", "V16", "V17",
        ),
    )

    /** Fallback fuer Skalen ohne eigene Leiter (Hallenfarben). */
    private val DEFAULT_SYSTEM = GradeSystem.FONT

    const val MIN_VALUE: Int = 0
    val MAX_VALUE: Int = LABELS.getValue(DEFAULT_SYSTEM).lastIndex

    /** Beschriftung -> Stufe, ueber alle Skalen und schreibweisenunabhaengig. */
    private val BY_LABEL: Map<String, Int> = buildMap {
        LABELS.values.forEach { labels ->
            labels.forEachIndexed { index, label ->
                // Erste Stufe gewinnt: "V3" steht fuer 6A und 6A+, gemeint ist 6A.
                putIfAbsent(label.lowercase(), index)
            }
        }
    }

    fun labelsOf(system: GradeSystem): List<String> =
        LABELS[system] ?: LABELS.getValue(DEFAULT_SYSTEM)

    fun label(value: Int, system: GradeSystem): String =
        labelsOf(system).getOrElse(value) { UNKNOWN }

    /** Liest "7A", "7a" und "V6" gleichermassen. */
    fun parse(label: String): Int? = BY_LABEL[label.trim().lowercase()]

    /**
     * Auswahlwerte fuer den Drehkranz. Stufen, die sich in dieser Skala eine
     * Beschriftung teilen, erscheinen nur einmal - man soll nicht zweimal an
     * "V3" vorbeiscrollen.
     */
    fun pickerValues(system: GradeSystem): List<Int> {
        val labels = labelsOf(system)
        return labels.indices.distinctBy { labels[it] }
    }

    fun clamp(value: Int): Int = value.coerceIn(MIN_VALUE, MAX_VALUE)

    private const val UNKNOWN = "?"
}
