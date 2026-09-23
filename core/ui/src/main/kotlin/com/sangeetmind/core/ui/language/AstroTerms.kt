package com.sangeetmind.core.ui.language

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.sangeetmind.core.ui.R

/**
 * Translates the fixed astrological vocabulary the backend sends in English — sign,
 * planet, nakshatra, weekday, tithi/yoga/karana, dignity and dasha names — into the
 * current display language. Unknown strings pass through unchanged, so wrapping any
 * backend-supplied name is always safe:
 *
 *     Text(astroTerm(planet.sign))          // "Aquarius" -> "कुंभ" in Hindi
 *
 * Interpretive prose (readings, narratives) is *not* covered — that comes from the
 * backend in whatever language it was asked for.
 */
@Composable
fun astroTerm(name: String?): String = LocalContext.current.astroTerm(name)

fun Context.astroTerm(name: String?): String {
    if (name.isNullOrBlank()) return name.orEmpty()
    val res = astroTermRes(name) ?: return name
    return getString(res)
}

/** Resource id for a known term, or null. Lookup ignores case, spaces and punctuation. */
@StringRes
fun astroTermRes(name: String): Int? = TERMS[normalize(name)]

private fun normalize(s: String): String = s.lowercase().filter { it.isLetterOrDigit() }

private fun terms(vararg pairs: Pair<String, Int>): Map<String, Int> =
    pairs.associate { (k, v) -> normalize(k) to v }

// Backend spelling variants (Moola/Mula, Jyeshta/Jyeshtha, …) are listed as aliases of
// the same resource, so whichever transliteration a given service emits still resolves.
private val TERMS: Map<String, Int> = terms(
    // Signs
    "Aries" to R.string.astro_sign_aries,
    "Taurus" to R.string.astro_sign_taurus,
    "Gemini" to R.string.astro_sign_gemini,
    "Cancer" to R.string.astro_sign_cancer,
    "Leo" to R.string.astro_sign_leo,
    "Virgo" to R.string.astro_sign_virgo,
    "Libra" to R.string.astro_sign_libra,
    "Scorpio" to R.string.astro_sign_scorpio,
    "Sagittarius" to R.string.astro_sign_sagittarius,
    "Capricorn" to R.string.astro_sign_capricorn,
    "Aquarius" to R.string.astro_sign_aquarius,
    "Pisces" to R.string.astro_sign_pisces,
    // Grahas
    "Sun" to R.string.astro_planet_sun,
    "Moon" to R.string.astro_planet_moon,
    "Mars" to R.string.astro_planet_mars,
    "Mercury" to R.string.astro_planet_mercury,
    "Jupiter" to R.string.astro_planet_jupiter,
    "Venus" to R.string.astro_planet_venus,
    "Saturn" to R.string.astro_planet_saturn,
    "Rahu" to R.string.astro_planet_rahu,
    "Ketu" to R.string.astro_planet_ketu,
    "Uranus" to R.string.astro_planet_uranus,
    "Neptune" to R.string.astro_planet_neptune,
    "Pluto" to R.string.astro_planet_pluto,
    "Lagna" to R.string.astro_lagna,
    "Ascendant" to R.string.astro_lagna,
    // Nakshatras
    "Ashwini" to R.string.astro_nak_ashwini,
    "Bharani" to R.string.astro_nak_bharani,
    "Krittika" to R.string.astro_nak_krittika,
    "Rohini" to R.string.astro_nak_rohini,
    "Mrigashira" to R.string.astro_nak_mrigashira,
    "Mrigashirsha" to R.string.astro_nak_mrigashira,
    "Mrigasira" to R.string.astro_nak_mrigashira,
    "Ardra" to R.string.astro_nak_ardra,
    "Punarvasu" to R.string.astro_nak_punarvasu,
    "Pushya" to R.string.astro_nak_pushya,
    "Ashlesha" to R.string.astro_nak_ashlesha,
    "Aslesha" to R.string.astro_nak_ashlesha,
    "Magha" to R.string.astro_nak_magha,
    "Purva Phalguni" to R.string.astro_nak_purva_phalguni,
    "Uttara Phalguni" to R.string.astro_nak_uttara_phalguni,
    "Hasta" to R.string.astro_nak_hasta,
    "Chitra" to R.string.astro_nak_chitra,
    "Swati" to R.string.astro_nak_swati,
    "Vishakha" to R.string.astro_nak_vishakha,
    "Anuradha" to R.string.astro_nak_anuradha,
    "Jyeshtha" to R.string.astro_nak_jyeshtha,
    "Jyeshta" to R.string.astro_nak_jyeshtha,
    "Mula" to R.string.astro_nak_mula,
    "Moola" to R.string.astro_nak_mula,
    "Purva Ashadha" to R.string.astro_nak_purva_ashadha,
    "Uttara Ashadha" to R.string.astro_nak_uttara_ashadha,
    "Shravana" to R.string.astro_nak_shravana,
    "Dhanishta" to R.string.astro_nak_dhanishta,
    "Dhanishtha" to R.string.astro_nak_dhanishta,
    "Shatabhisha" to R.string.astro_nak_shatabhisha,
    "Satabhisha" to R.string.astro_nak_shatabhisha,
    "Purva Bhadrapada" to R.string.astro_nak_purva_bhadrapada,
    "Uttara Bhadrapada" to R.string.astro_nak_uttara_bhadrapada,
    "Revati" to R.string.astro_nak_revati,
    // Weekdays
    "Sunday" to R.string.astro_day_sunday,
    "Monday" to R.string.astro_day_monday,
    "Tuesday" to R.string.astro_day_tuesday,
    "Wednesday" to R.string.astro_day_wednesday,
    "Thursday" to R.string.astro_day_thursday,
    "Friday" to R.string.astro_day_friday,
    "Saturday" to R.string.astro_day_saturday,
    // Dignities / states
    "Exalted" to R.string.astro_dig_exalted,
    "Debilitated" to R.string.astro_dig_debilitated,
    "Own Sign" to R.string.astro_dig_own_sign,
    "Own" to R.string.astro_dig_own_sign,
    "Moolatrikona" to R.string.astro_dig_moolatrikona,
    "Great Friend" to R.string.astro_dig_great_friend,
    "Friend" to R.string.astro_dig_friend,
    "Neutral" to R.string.astro_dig_neutral,
    "Enemy" to R.string.astro_dig_enemy,
    "Great Enemy" to R.string.astro_dig_great_enemy,
    "Combust" to R.string.astro_state_combust,
    "Retrograde" to R.string.astro_state_retrograde,
    "Direct" to R.string.astro_state_direct,
    // Tithis
    "Pratipada" to R.string.astro_tithi_pratipada,
    "Dwitiya" to R.string.astro_tithi_dwitiya,
    "Tritiya" to R.string.astro_tithi_tritiya,
    "Chaturthi" to R.string.astro_tithi_chaturthi,
    "Panchami" to R.string.astro_tithi_panchami,
    "Shashthi" to R.string.astro_tithi_shashthi,
    "Shashti" to R.string.astro_tithi_shashthi,
    "Saptami" to R.string.astro_tithi_saptami,
    "Ashtami" to R.string.astro_tithi_ashtami,
    "Navami" to R.string.astro_tithi_navami,
    "Dashami" to R.string.astro_tithi_dashami,
    "Ekadashi" to R.string.astro_tithi_ekadashi,
    "Dwadashi" to R.string.astro_tithi_dwadashi,
    "Trayodashi" to R.string.astro_tithi_trayodashi,
    "Chaturdashi" to R.string.astro_tithi_chaturdashi,
    "Purnima" to R.string.astro_tithi_purnima,
    "Amavasya" to R.string.astro_tithi_amavasya,
    "Shukla" to R.string.astro_paksha_shukla,
    "Krishna" to R.string.astro_paksha_krishna,
    "Shukla Paksha" to R.string.astro_paksha_shukla_full,
    "Krishna Paksha" to R.string.astro_paksha_krishna_full,
    // Yogas (panchang)
    "Vishkambha" to R.string.astro_yoga_vishkambha,
    "Vishkumbha" to R.string.astro_yoga_vishkambha,
    "Priti" to R.string.astro_yoga_priti,
    "Ayushman" to R.string.astro_yoga_ayushman,
    "Saubhagya" to R.string.astro_yoga_saubhagya,
    "Shobhana" to R.string.astro_yoga_shobhana,
    "Atiganda" to R.string.astro_yoga_atiganda,
    "Sukarma" to R.string.astro_yoga_sukarma,
    "Dhriti" to R.string.astro_yoga_dhriti,
    "Shula" to R.string.astro_yoga_shula,
    "Ganda" to R.string.astro_yoga_ganda,
    "Vriddhi" to R.string.astro_yoga_vriddhi,
    "Dhruva" to R.string.astro_yoga_dhruva,
    "Vyaghata" to R.string.astro_yoga_vyaghata,
    "Harshana" to R.string.astro_yoga_harshana,
    "Vajra" to R.string.astro_yoga_vajra,
    "Siddhi" to R.string.astro_yoga_siddhi,
    "Vyatipata" to R.string.astro_yoga_vyatipata,
    "Variyan" to R.string.astro_yoga_variyan,
    "Parigha" to R.string.astro_yoga_parigha,
    "Shiva" to R.string.astro_yoga_shiva,
    "Siddha" to R.string.astro_yoga_siddha,
    "Sadhya" to R.string.astro_yoga_sadhya,
    "Shubha" to R.string.astro_yoga_shubha,
    "Brahma" to R.string.astro_yoga_brahma,
    "Indra" to R.string.astro_yoga_indra,
    "Vaidhriti" to R.string.astro_yoga_vaidhriti,
    // Karanas
    "Bava" to R.string.astro_karana_bava,
    "Balava" to R.string.astro_karana_balava,
    "Kaulava" to R.string.astro_karana_kaulava,
    "Taitila" to R.string.astro_karana_taitila,
    "Garaja" to R.string.astro_karana_garaja,
    "Vanija" to R.string.astro_karana_vanija,
    "Vishti" to R.string.astro_karana_vishti,
    "Shakuni" to R.string.astro_karana_shakuni,
    "Chatushpada" to R.string.astro_karana_chatushpada,
    "Naga" to R.string.astro_karana_naga,
    "Kimstughna" to R.string.astro_karana_kimstughna,
    // Dasha vocabulary
    "Mahadasha" to R.string.astro_dasha_maha,
    "Antardasha" to R.string.astro_dasha_antar,
    "Pratyantardasha" to R.string.astro_dasha_pratyantar,
    "Vimshottari" to R.string.astro_dasha_vimshottari,
    "Yogini" to R.string.astro_dasha_yogini,
    "Chara" to R.string.astro_dasha_chara,
    "Mangala" to R.string.astro_yogini_mangala,
    "Pingala" to R.string.astro_yogini_pingala,
    "Dhanya" to R.string.astro_yogini_dhanya,
    "Bhramari" to R.string.astro_yogini_bhramari,
    "Bhadrika" to R.string.astro_yogini_bhadrika,
    "Ulka" to R.string.astro_yogini_ulka,
    "Sankata" to R.string.astro_yogini_sankata,
    // Elements & misc
    "Fire" to R.string.astro_elem_fire,
    "Earth" to R.string.astro_elem_earth,
    "Air" to R.string.astro_elem_air,
    "Water" to R.string.astro_elem_water,
    "Manglik" to R.string.astro_misc_manglik,
    "Kalsarpa" to R.string.astro_misc_kalsarpa,
    "Sade Sati" to R.string.astro_misc_sade_sati,
    "Present" to R.string.astro_misc_present,
    "Absent" to R.string.astro_misc_absent,
    "Male" to R.string.astro_misc_male,
    "Female" to R.string.astro_misc_female,
    // Daily mood words (astro_service.py: TARA_MOOD_MAP + the per-planet mood map)
    "Centered" to R.string.astro_mood_centered,
    "Optimistic" to R.string.astro_mood_optimistic,
    "Cautious" to R.string.astro_mood_cautious,
    "Content" to R.string.astro_mood_content,
    "Restless" to R.string.astro_mood_restless,
    "Driven" to R.string.astro_mood_driven,
    "On edge" to R.string.astro_mood_on_edge,
    "Sociable" to R.string.astro_mood_sociable,
    "Joyful" to R.string.astro_mood_joyful,
    "Emotional" to R.string.astro_mood_emotional,
    "Energetic" to R.string.astro_mood_energetic,
    "Reflective" to R.string.astro_mood_reflective,
    "Thoughtful" to R.string.astro_mood_thoughtful,
    "Empowered" to R.string.astro_mood_empowered,
    "Curious" to R.string.astro_mood_curious,
    "Detached" to R.string.astro_mood_detached
)
