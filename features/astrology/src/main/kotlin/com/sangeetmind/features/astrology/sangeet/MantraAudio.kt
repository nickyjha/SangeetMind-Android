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
        "ketu_beej" to MantraTrack(R.raw.ketu_beej, chantsPerPlay = 1)
    )

    fun forMantra(mantraId: String): MantraTrack? = tracks[mantraId]

    /** Chant count after one more full play, capped at [target]. */
    fun afterPlay(count: Int, track: MantraTrack, target: Int = MALA): Int =
        (count + track.chantsPerPlay).coerceAtMost(target)
}
