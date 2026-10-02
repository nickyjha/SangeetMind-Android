package com.sangeetmind.features.astrology.sangeet

import androidx.annotation.RawRes
import com.sangeetmind.features.astrology.R

/**
 * Recorded chant for a [JapaMantras] id. A track holds [chantsPerPlay] repetitions; the
 * player loops it and credits that many chants to the mala on every full play.
 * (Preview: bundled in res/raw. The full set will stream from cloud storage.)
 */
data class MantraTrack(@RawRes val rawRes: Int, val chantsPerPlay: Int)

object MantraAudio {
    /** A full mala. */
    const val MALA = 108

    private val tracks: Map<String, MantraTrack> = mapOf(
        "surya_beej" to MantraTrack(R.raw.surya_beej, chantsPerPlay = 1),
        "chandra_beej" to MantraTrack(R.raw.chandra_beej, chantsPerPlay = 1),
        "mangal_beej" to MantraTrack(R.raw.mangal_beej, chantsPerPlay = 1),
        "budh_beej" to MantraTrack(R.raw.budh_beej, chantsPerPlay = 1),
        "guru_beej" to MantraTrack(R.raw.guru_beej, chantsPerPlay = 1),
        "shukra_beej" to MantraTrack(R.raw.shukra_beej, chantsPerPlay = 1),
        "shani_beej" to MantraTrack(R.raw.shani_beej, chantsPerPlay = 1),
        "ketu_beej" to MantraTrack(R.raw.ketu_beej, chantsPerPlay = 1),
        "gayatri" to MantraTrack(R.raw.gayatri, chantsPerPlay = 1),
        "mahamrityunjaya" to MantraTrack(R.raw.mahamrityunjaya, chantsPerPlay = 1)
    )

    fun forMantra(mantraId: String): MantraTrack? = tracks[mantraId]

    private val grahaKeys = listOf(
        "surya" to listOf("surya", "sun", "aditya", "सूर्य"),
        "chandra" to listOf("chandra", "moon", "soma", "चंद्र", "चन्द्र"),
        "mangal" to listOf("mangal", "mars", "bhaum", "angarak", "मंगल", "भौम"),
        "budh" to listOf("budh", "mercury", "बुध"),
        "guru" to listOf("guru", "jupiter", "brihaspat", "गुरु", "बृहस्पत"),
        "shukra" to listOf("shukra", "venus", "शुक्र"),
        "shani" to listOf("shani", "saturn", "shanaishchar", "शनि", "शनैश्चर"),
        "rahu" to listOf("rahu", "राहु"),
        "ketu" to listOf("ketu", "केतु")
    )

    /**
     * Best japa mantra id for a free-text mantra name such as the horoscope's
     * "Om Chandraya Namaha" or "Mahamrityunjaya Mantra"; null when nothing fits.
     */
    fun idForMantraText(text: String): String? {
        val t = text.lowercase()
        return when {
            "mrityunjaya" in t || "tryambak" in t || "त्र्यम्बक" in t || "महामृत्युंजय" in t -> "mahamrityunjaya"
            "shivaya" in t || "शिवाय" in t -> "om_namah_shivaya"
            "hanuman" in t || "हनुमान" in t -> "hanuman_chalisa"
            else -> grahaKeys.firstOrNull { (_, keys) -> keys.any { it in t } }?.let { "${it.first}_beej" }
                ?: if ("gayatri" in t || "गायत्री" in t || "bhur bhuva" in t) "gayatri" else null
        }
    }

    /** Chant count after one more full play, capped at [target]. */
    fun afterPlay(count: Int, track: MantraTrack, target: Int = MALA): Int =
        (count + track.chantsPerPlay).coerceAtMost(target)
}
