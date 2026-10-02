package com.sangeetmind.features.astrology.sangeet

import androidx.annotation.StringRes
import com.sangeetmind.features.astrology.R

/**
 * A mantra the japa counter can log against. [id] is what the backend stores
 * (`mantra_sadhana_sessions.mantra_id`, free-form) — never change an existing id, or old
 * history stops matching. [graha] is the English planet name for the navagraha beej
 * mantras, used to pre-select the one for the user's current mahadasha lord.
 */
data class JapaMantra(
    val id: String,
    @StringRes val nameRes: Int,
    val graha: String? = null
)

object JapaMantras {
    /** Kept for compatibility: every session logged before the picker existed used this id. */
    const val GENERIC_ID = "generic"

    val generic = JapaMantra(GENERIC_ID, R.string.sangeet_mantra_generic)

    /** Picker order: the classics, then one beej mantra per navagraha, then "Any mantra". */
    val all: List<JapaMantra> = listOf(
        JapaMantra("gayatri", R.string.sangeet_mantra_gayatri),
        JapaMantra("mahamrityunjaya", R.string.sangeet_mantra_mahamrityunjaya),
        JapaMantra("om_namah_shivaya", R.string.sangeet_mantra_om_namah_shivaya),
        JapaMantra("hanuman_chalisa", R.string.sangeet_mantra_hanuman_chalisa),
        JapaMantra("surya_beej", R.string.sangeet_mantra_surya, graha = "Sun"),
        JapaMantra("chandra_beej", R.string.sangeet_mantra_chandra, graha = "Moon"),
        JapaMantra("mangal_beej", R.string.sangeet_mantra_mangal, graha = "Mars"),
        JapaMantra("budh_beej", R.string.sangeet_mantra_budh, graha = "Mercury"),
        JapaMantra("guru_beej", R.string.sangeet_mantra_guru, graha = "Jupiter"),
        JapaMantra("shukra_beej", R.string.sangeet_mantra_shukra, graha = "Venus"),
        JapaMantra("shani_beej", R.string.sangeet_mantra_shani, graha = "Saturn"),
        JapaMantra("rahu_beej", R.string.sangeet_mantra_rahu, graha = "Rahu"),
        JapaMantra("ketu_beej", R.string.sangeet_mantra_ketu, graha = "Ketu"),
        generic
    )

    private val byId: Map<String, JapaMantra> = all.associateBy { it.id }

    /** Known mantra for a server id (case/whitespace-insensitive); blank means [generic]. */
    fun byId(id: String?): JapaMantra? {
        val key = id?.trim()?.lowercase().orEmpty()
        return if (key.isEmpty()) generic else byId[key]
    }

    /** Beej mantra for a dasha lord in English ("Saturn") or Hindi ("Shani") form. */
    fun forDashaLord(lord: String?): JapaMantra? {
        val graha = GRAHA_ALIASES[lord?.trim()?.lowercase().orEmpty()] ?: return null
        return all.firstOrNull { it.graha == graha }
    }

    /** Fallback label for an id we don't know: "my_custom-mantra" -> "My Custom Mantra". */
    fun titleCase(id: String): String =
        id.replace('_', ' ').replace('-', ' ').trim()
            .split(' ').filter { it.isNotEmpty() }
            .joinToString(" ") { w -> w.lowercase().replaceFirstChar { it.uppercase() } }

    private val GRAHA_ALIASES: Map<String, String> = mapOf(
        "sun" to "Sun", "surya" to "Sun",
        "moon" to "Moon", "chandra" to "Moon",
        "mars" to "Mars", "mangal" to "Mars", "mangala" to "Mars",
        "mercury" to "Mercury", "budh" to "Mercury", "budha" to "Mercury",
        "jupiter" to "Jupiter", "guru" to "Jupiter", "brihaspati" to "Jupiter",
        "venus" to "Venus", "shukra" to "Venus",
        "saturn" to "Saturn", "shani" to "Saturn",
        "rahu" to "Rahu",
        "ketu" to "Ketu"
    )

    /** The graha beej mantra for a dasha lord ("Saturn", "Shani", "शनि"...), or null. */
    fun beejForLord(lord: String?): JapaMantra? {
        if (lord.isNullOrBlank()) return null
        all.firstOrNull { it.graha.equals(lord.trim(), ignoreCase = true) }?.let { return it }
        val id = MantraAudio.idForMantraText(lord) ?: return null
        return byId(id)?.takeIf { it.graha != null }
    }
}
