package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ---- POST /v1/chart (MVP + Vimshottari timeline + divisional/varga charts d2..d60;
// doshas, friendship, KP, jaimini — deferred to later chart sprints.) ----

@JsonClass(generateAdapter = true)
data class ChartRequest(
    val date: String, // YYYY-MM-DD
    val time: String, // HH:MM
    val timezone: String? = null,
    val place: String = "",
    val lat: Double,
    val lon: Double
)

@JsonClass(generateAdapter = true)
data class LagnaInfo(
    val sign: String,
    val degree: Double = 0.0,
    val absolute: Double = 0.0,
    @Json(name = "absolute_dms") val absoluteDms: String? = null
)

@JsonClass(generateAdapter = true)
data class PlanetInfo(
    val sign: String,
    val degree: Double = 0.0,
    val absolute: String = "",
    @Json(name = "absolute_dms") val absoluteDms: String? = null,
    val house: Int? = null,
    val retrograde: Boolean = false,
    val combust: Boolean = false,
    val exalted: Boolean = false,
    val debilitated: Boolean = false,
    val vargottama: Boolean = false,
    /** Whole-sign houses (1-12) this planet's drishti falls on (app/services/chart_aspects.py). */
    @Json(name = "aspects_houses") val aspectsHouses: List<Int> = emptyList(),
    /** Same aspects with which-aspect (`offset`, e.g. 7) and drishti-bala `strength` %. */
    val aspects: List<PlanetAspect> = emptyList()
)

/** One drishti cast by a planet: the house it lands on, which aspect it is (offset from
 * the planet's own house, e.g. 7 = the universal 7th-house aspect) and its strength
 * (7th = 100%, Mars/Saturn specials = 75%, Jupiter/nodes specials = 50%). */
@JsonClass(generateAdapter = true)
data class PlanetAspect(
    val house: Int,
    val offset: Int = 0,
    val strength: Int = 100
)

/** Two grahas that aspect each other's house ("mutual aspect"). */
@JsonClass(generateAdapter = true)
data class MutualAspect(
    @Json(name = "planet_a") val planetA: String,
    @Json(name = "house_a") val houseA: Int,
    @Json(name = "planet_b") val planetB: String,
    @Json(name = "house_b") val houseB: Int
)

@JsonClass(generateAdapter = true)
data class MoonNakshatraInfo(
    val index: Int = 0,
    val pada: Int = 0,
    val lord: String = "",
    val name: String? = null
)

@JsonClass(generateAdapter = true)
data class DashaPeriod(
    val lord: String,
    val start: String,
    val end: String,
    val partial: Boolean = false
)

@JsonClass(generateAdapter = true)
data class VimshottariCurrent(
    val mahadasha: DashaPeriod? = null,
    val antardasha: DashaPeriod? = null,
    val pratyantardasha: DashaPeriod? = null,
    @Json(name = "pratyantar_dasha") val pratyantarDashaAlt: DashaPeriod? = null,
    val pratyantar: DashaPeriod? = null,
    val now: String? = null
) {
    val resolvedPratyantar: DashaPeriod?
        get() = pratyantardasha ?: pratyantarDashaAlt ?: pratyantar
}

@JsonClass(generateAdapter = true)
data class BhuktiPeriod(
    val lord: String,
    val start: String,
    val end: String,
    val partial: Boolean = false,
    val pratyantars: List<DashaPeriod> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MahadashaPeriod(
    val lord: String,
    val start: String,
    val end: String,
    val partial: Boolean = false,
    val bhuktis: List<BhuktiPeriod> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VimshottariInfo(
    val current: VimshottariCurrent? = null,
    val mahadashas: List<MahadashaPeriod> = emptyList()
)

/** A divisional/varga chart (D2, D9, ...) — same shape as the D1 data on
 * [ChartSummaryResponse] itself, just nested under its own key. */
@JsonClass(generateAdapter = true)
data class DivisionalChart(
    val lagna: LagnaInfo,
    val planets: Map<String, PlanetInfo> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class ManglikDosha(
    val present: Boolean = false,
    @Json(name = "effective_present") val effectivePresent: Boolean = false,
    val cancelled: Boolean = false,
    val summary: String = ""
)

@JsonClass(generateAdapter = true)
data class KalsarpaDosha(
    val present: Boolean = false,
    val yoga: String? = null,
    @Json(name = "yoga_full_name") val yogaFullName: String? = null
)

@JsonClass(generateAdapter = true)
data class SadesatiPeriod(
    val kind: String = "",
    val phase: String? = null,
    @Json(name = "saturn_sign") val saturnSign: String = "",
    @Json(name = "start_date") val startDate: String = "",
    @Json(name = "end_date") val endDate: String = ""
)

@JsonClass(generateAdapter = true)
data class SadesatiDosha(
    @Json(name = "active_on_today") val activeOnToday: SadesatiPeriod? = null,
    val periods: List<SadesatiPeriod> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ChartDoshas(
    val manglik: ManglikDosha = ManglikDosha(),
    val kalsarpa: KalsarpaDosha = KalsarpaDosha(),
    val sadesati: SadesatiDosha = SadesatiDosha()
)

/** Sarva Ashtakvarga — bindu (strength) score per sign, summing to 337 across the chart. */
@JsonClass(generateAdapter = true)
data class AshtakvargaSav(
    @Json(name = "bindus_by_sign") val bindusBySign: Map<String, Int> = emptyMap(),
    @Json(name = "total_bindus") val totalBindus: Int = 0
)

@JsonClass(generateAdapter = true)
data class ChartAshtakvarga(
    val sav: AshtakvargaSav = AshtakvargaSav()
)

/** Panchadha (five-fold) planetary friendship — the combined permanent+temporal result
 * the backend already returns as its top-level `relations`/`planets`. */
@JsonClass(generateAdapter = true)
data class ChartFriendship(
    val planets: List<String> = emptyList(),
    val relations: Map<String, Map<String, String>> = emptyMap()
)

/** Six-fold planetary strength (Shadbala) — virupas per component, comparable within a
 * chart; not full BPHS arc-minute tables (app/services/shadbala.py). */
@JsonClass(generateAdapter = true)
data class ShadbalaComponents(
    val sthana: Double = 0.0,
    val dig: Double = 0.0,
    val kala: Double = 0.0,
    val cheshta: Double = 0.0,
    val naisargika: Double = 0.0,
    val drik: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class ShadbalaPlanet(
    val sign: String = "",
    val house: Int? = null,
    val components: ShadbalaComponents = ShadbalaComponents(),
    @Json(name = "total_virupas") val totalVirupas: Double = 0.0,
    @Json(name = "total_rupas") val totalRupas: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class ShadbalaRanking(
    val planet: String,
    @Json(name = "total_virupas") val totalVirupas: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class ChartShadbala(
    val planets: Map<String, ShadbalaPlanet> = emptyMap(),
    val ranking: List<ShadbalaRanking> = emptyList(),
    val method: String = "",
    val note: String = ""
)

/** House strength (Bhavabala), derived from Ashtakvarga SAV + the house lord's Shadbala
 * (app/services/bhavabala.py). */
@JsonClass(generateAdapter = true)
data class BhavabalaHouse(
    val house: Int,
    val sign: String = "",
    val lord: String = "",
    @Json(name = "lord_house") val lordHouse: Int? = null,
    @Json(name = "sav_bindus") val savBindus: Double = 0.0,
    @Json(name = "lord_shadbala_virupas") val lordShadbalaVirupas: Double = 0.0,
    @Json(name = "total_virupas") val totalVirupas: Double = 0.0,
    val rank: Int = 0
)

@JsonClass(generateAdapter = true)
data class ChartBhavabala(
    val houses: List<BhavabalaHouse> = emptyList(),
    @Json(name = "strongest_house") val strongestHouse: Int? = null,
    val method: String = ""
)

/** KP (Krishnamurti Paddhati) house cusp — sign, sub-lord, sub-sub-lord at the cusp
 * longitude, Krishnamurti ayanamsa (app/services/kp_system.py). */
@JsonClass(generateAdapter = true)
data class KpCusp(
    val house: Int,
    val longitude: Double = 0.0,
    val sign: String = "",
    val degree: Double = 0.0,
    @Json(name = "sub_lord") val subLord: String = "",
    @Json(name = "sub_sub_lord") val subSubLord: String = ""
)

@JsonClass(generateAdapter = true)
data class KpPlanet(
    val longitude: Double = 0.0,
    val sign: String = "",
    val degree: Double = 0.0,
    @Json(name = "absolute_dms") val absoluteDms: String? = null,
    val house: Int? = null,
    val nakshatra: MoonNakshatraInfo = MoonNakshatraInfo(),
    @Json(name = "sub_lord") val subLord: String = "",
    @Json(name = "sub_sub_lord") val subSubLord: String = ""
)

@JsonClass(generateAdapter = true)
data class ChartKp(
    @Json(name = "ayanamsa_name") val ayanamsaName: String = "",
    @Json(name = "ayanamsa_deg") val ayanamsaDeg: Double = 0.0,
    val cusps: List<KpCusp> = emptyList(),
    val planets: Map<String, KpPlanet> = emptyMap(),
    /** House numbers each planet signifies, via cusp ownership + occupation. */
    val significators: Map<String, List<Int>> = emptyMap()
)

/** Jaimini chara karaka — the 7 classical planets ranked by degree-in-sign, Atmakaraka
 * (highest degree) first (app/services/jaimini_charts.py). */
@JsonClass(generateAdapter = true)
data class CharaKaraka(
    val karaka: String,
    val abbrev: String,
    val planet: String,
    @Json(name = "degree_in_sign") val degreeInSign: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class JaiminiLagna(
    val sign: String = "",
    val house: Int = 1,
    val degree: Double? = null
)

@JsonClass(generateAdapter = true)
data class JaiminiChartPlanet(
    val sign: String = "",
    val house: Int? = null,
    @Json(name = "d1_sign") val d1Sign: String? = null
)

/** Karakamsa (D9 sign of Atmakaraka) or Swamsa (D9 sign of Amatyakaraka) lagna, treated
 * as its own chart. */
@JsonClass(generateAdapter = true)
data class JaiminiSubChart(
    val label: String = "",
    @Json(name = "lagna_sign") val lagnaSign: String = "",
    val lagna: JaiminiLagna = JaiminiLagna(),
    val planets: Map<String, JaiminiChartPlanet> = emptyMap(),
    val source: String = ""
)

@JsonClass(generateAdapter = true)
data class ChartJaimini(
    @Json(name = "chara_karakas") val charaKarakas: List<CharaKaraka> = emptyList(),
    val atmakaraka: String = "",
    val amatyakaraka: String = "",
    val karakamsa: JaiminiSubChart = JaiminiSubChart(),
    val swamsa: JaiminiSubChart = JaiminiSubChart()
)

/** Lal Kitab — the backend's own scope note: whole-sign D1 houses from birth lagna, not
 * full classical Lal Kitab (blind houses, divisional-chart rules). Placements + karmic
 * debt (rin) yoga flags + short remedies (app/services/lal_kitab.py). Note the backend
 * mixes snake_case and camelCase across this one response — modeled as-is. */
@JsonClass(generateAdapter = true)
data class LalKitabPlacement(
    val planet: String,
    val sign: String = "",
    val house: Int,
    @Json(name = "house_theme") val houseTheme: String = "",
    val retrograde: Boolean = false
)

@JsonClass(generateAdapter = true)
data class LalKitabRinYoga(
    val planet: String,
    val house: String = "",
    val rinType: String = "",
    val note: String = ""
)

@JsonClass(generateAdapter = true)
data class LalKitabRemedy(
    val planet: String,
    val remedy: String = ""
)

@JsonClass(generateAdapter = true)
data class LalKitabPrediction(
    val planet: String,
    val house: String = "",
    val text: String = ""
)

@JsonClass(generateAdapter = true)
data class ChartLalKitab(
    val scope: String = "",
    val rinYogas: List<LalKitabRinYoga> = emptyList(),
    val remedies: List<LalKitabRemedy> = emptyList(),
    @Json(name = "system_note") val systemNote: String = "",
    @Json(name = "lagna_sign") val lagnaSign: String = "",
    val placements: List<LalKitabPlacement> = emptyList(),
    val predictions: List<LalKitabPrediction> = emptyList(),
    @Json(name = "epic_status") val epicStatus: String = ""
)

/** Static, non-LLM narrative text — nakshatra/lagna/dasha phal summaries and a per-planet
 * consideration list. Lighter than the LLM-driven interpretation/ChatMind readings (the
 * backend's own `disclaimer` says as much) (app/services/kundli_narratives.py). */
@JsonClass(generateAdapter = true)
data class NakshatraPhal(
    val nakshatra: String = "",
    val pada: Int? = null,
    val lord: String = "",
    val text: String = ""
)

@JsonClass(generateAdapter = true)
data class AscendantSummary(
    val sign: String = "",
    val text: String = ""
)

@JsonClass(generateAdapter = true)
data class VimshottariMahadashaPhal(
    @Json(name = "current_mahadasha") val currentMahadasha: String? = null,
    @Json(name = "current_antardasha") val currentAntardasha: String? = null,
    @Json(name = "current_pratyantardasha") val currentPratyantardasha: String? = null,
    @Json(name = "mahadasha_text") val mahadashaText: String? = null,
    @Json(name = "antardasha_note") val antardashaNote: String? = null
)

@JsonClass(generateAdapter = true)
data class ChartNarratives(
    @Json(name = "nakshatra_phal") val nakshatraPhal: NakshatraPhal = NakshatraPhal(),
    @Json(name = "ascendant_summary") val ascendantSummary: AscendantSummary = AscendantSummary(),
    @Json(name = "life_predictions_intro") val lifePredictionsIntro: String = "",
    @Json(name = "vimshottari_mahadasha_phal")
    val vimshottariMahadashaPhal: VimshottariMahadashaPhal = VimshottariMahadashaPhal(),
    @Json(name = "planet_considerations") val planetConsiderations: List<String> = emptyList(),
    val disclaimer: String = ""
)

/** Placidus house cusps for the active `system` (whole_sign or placidus)
 * (app/services/houses.py: build_houses_block). */
@JsonClass(generateAdapter = true)
data class HouseCusp(
    val house: Int,
    val longitude: Double = 0.0,
    val sign: String = "",
    val degree: Double = 0.0,
    @Json(name = "absolute_dms") val absoluteDms: String? = null
)

@JsonClass(generateAdapter = true)
data class ChartHouses(
    val system: String = "",
    val cusps: List<HouseCusp> = emptyList(),
    @Json(name = "cusps_longitude") val cuspsLongitude: List<Double> = emptyList(),
    @Json(name = "placidus_note") val placidusNote: String = "",
    @Json(name = "planet_houses") val planetHouses: Map<String, Int> = emptyMap()
)

/** Chalit (Bhava) chart — the same birth moment redrawn with Placidus house cusps instead
 * of whole-sign, so its lagna and per-planet houses can differ from the D1 Rasi chart.
 * Note `planets[].absolute` is a Double here (unlike the top-level `planets[].absolute`,
 * which is a formatted String) — modeled as its own type rather than reusing PlanetInfo
 * (app/services/houses.py: build_chalit_block). */
@JsonClass(generateAdapter = true)
data class ChalitPlanet(
    val sign: String = "",
    val house: Int? = null,
    val degree: Double = 0.0,
    val absolute: Double = 0.0,
    @Json(name = "absolute_dms") val absoluteDms: String? = null
)

@JsonClass(generateAdapter = true)
data class ChartChalit(
    val lagna: LagnaInfo = LagnaInfo(sign = ""),
    @Json(name = "cusps_longitude") val cuspsLongitude: List<Double> = emptyList(),
    val planets: Map<String, ChalitPlanet> = emptyMap()
)

/** Chandra Kundli (Moon chart) — whole-sign houses from the Moon sign as lagna, used for
 * classical Moon-rashi analysis. The response's `manglik` field duplicates the richer
 * [ManglikDosha] already shown on Overview via [ChartDoshas], so it's intentionally left
 * unparsed here — Moshi skips unknown JSON keys by default
 * (app/services/moon_chart.py: build_moon_chart). */
@JsonClass(generateAdapter = true)
data class MoonChartPlanet(
    val sign: String = "",
    val house: Int? = null,
    val degree: Double = 0.0,
    val absolute: Double = 0.0,
    @Json(name = "absolute_dms") val absoluteDms: String? = null,
    val retrograde: Boolean = false,
    val combust: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ChartMoonChart(
    val lagna: LagnaInfo = LagnaInfo(sign = ""),
    val planets: Map<String, MoonChartPlanet> = emptyMap()
)

/** Yogini Dasha — 8-yogini, 36-year cycle from Moon nakshatra (app/services/dasha_yogini.py). */
@JsonClass(generateAdapter = true)
data class YoginiAntardasha(
    val yogini: String,
    val lord: String,
    val start: String,
    val end: String,
    val years: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class YoginiMahadasha(
    val yogini: String,
    val lord: String,
    val start: String,
    val end: String,
    val partial: Boolean = false,
    val years: Double = 0.0,
    val cycle: Int = 1,
    val antardashas: List<YoginiAntardasha> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YoginiInfo(
    val mahadashas: List<YoginiMahadasha> = emptyList(),
    @Json(name = "starting_yogini") val startingYogini: String = ""
)

/** Jaimini Chara Dasha — sign-based periods from the lagna sign (app/services/dasha_char.py). */
@JsonClass(generateAdapter = true)
data class CharaAntardasha(
    val sign: String,
    val start: String,
    val end: String,
    val years: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class CharaMahadasha(
    val sign: String,
    val start: String,
    val end: String,
    val years: Double = 0.0,
    val partial: Boolean = false,
    val antardashas: List<CharaAntardasha> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CharaDashaInfo(
    val mahadashas: List<CharaMahadasha> = emptyList(),
    val direction: String = "",
    @Json(name = "lagna_sign") val lagnaSign: String = ""
)

@JsonClass(generateAdapter = true)
data class ChartSummaryResponse(
    val lagna: LagnaInfo,
    val planets: Map<String, PlanetInfo> = emptyMap(),
    @Json(name = "moon_nakshatra") val moonNakshatra: MoonNakshatraInfo = MoonNakshatraInfo(),
    val vimshottari: VimshottariInfo = VimshottariInfo(),
    val doshas: ChartDoshas = ChartDoshas(),
    val ashtakvarga: ChartAshtakvarga = ChartAshtakvarga(),
    val friendship: ChartFriendship = ChartFriendship(),
    val shadbala: ChartShadbala = ChartShadbala(),
    val bhavabala: ChartBhavabala = ChartBhavabala(),
    @Json(name = "mutual_aspects") val mutualAspects: List<MutualAspect> = emptyList(),
    val kp: ChartKp = ChartKp(),
    val jaimini: ChartJaimini = ChartJaimini(),
    @Json(name = "lal_kitab") val lalKitab: ChartLalKitab = ChartLalKitab(),
    val narratives: ChartNarratives = ChartNarratives(),
    val houses: ChartHouses = ChartHouses(),
    val chalit: ChartChalit = ChartChalit(),
    @Json(name = "moon_chart") val moonChart: ChartMoonChart = ChartMoonChart(),
    val yogini: YoginiInfo = YoginiInfo(),
    @Json(name = "chara_dasha") val charaDasha: CharaDashaInfo = CharaDashaInfo(),
    val d2: DivisionalChart? = null,
    val d3: DivisionalChart? = null,
    val d4: DivisionalChart? = null,
    val d7: DivisionalChart? = null,
    val d9: DivisionalChart? = null,
    val d10: DivisionalChart? = null,
    val d12: DivisionalChart? = null,
    val d16: DivisionalChart? = null,
    val d20: DivisionalChart? = null,
    val d24: DivisionalChart? = null,
    val d27: DivisionalChart? = null,
    val d30: DivisionalChart? = null,
    val d40: DivisionalChart? = null,
    val d45: DivisionalChart? = null,
    val d60: DivisionalChart? = null
) {
    /** D1 plus every divisional chart the backend actually returned, in varga order —
     * drives the chart-switcher tabs so we never show a tab with no data behind it. */
    val availableCharts: List<Pair<String, DivisionalChart>>
        get() = listOfNotNull(
            "D1" to DivisionalChart(lagna, planets),
            d2?.let { "D2" to it },
            d3?.let { "D3" to it },
            d4?.let { "D4" to it },
            d7?.let { "D7" to it },
            d9?.let { "D9" to it },
            d10?.let { "D10" to it },
            d12?.let { "D12" to it },
            d16?.let { "D16" to it },
            d20?.let { "D20" to it },
            d24?.let { "D24" to it },
            d27?.let { "D27" to it },
            d30?.let { "D30" to it },
            d40?.let { "D40" to it },
            d45?.let { "D45" to it },
            d60?.let { "D60" to it }
        )
}

/** Label + what-it's-for, matching the website's CHART_LABELS so the two stay in sync. */
val DIVISIONAL_CHART_META: Map<String, Pair<String, String>> = mapOf(
    "D1" to ("D1 Rasi Chart" to "Birth chart"),
    "D2" to ("D2 Hora" to "Wealth, income"),
    "D3" to ("D3 Drekkana" to "Siblings, courage"),
    "D4" to ("D4 Chaturthamsa" to "Property, fortune"),
    "D7" to ("D7 Saptamsa" to "Children"),
    "D9" to ("D9 Navamsa" to "Marriage, dharma"),
    "D10" to ("D10 Dashamsa" to "Career, profession"),
    "D12" to ("D12 Dwadasamsa" to "Parents, family"),
    "D16" to ("D16 Shodashamsa" to "Vehicles, comforts"),
    "D20" to ("D20 Vimsamsa" to "Spiritual progress"),
    "D24" to ("D24 Chaturvimsamsa" to "Education"),
    "D27" to ("D27 Saptavimsamsa" to "Strengths, weaknesses"),
    "D30" to ("D30 Trimsamsa" to "Health, resilience"),
    "D40" to ("D40 Khavedamsa" to "Auspicious effects"),
    "D45" to ("D45 Akshavedamsa" to "Character, conduct"),
    "D60" to ("D60 Sashtiamsa" to "Past karma")
)

// ---- POST /v1/varshaphal (Tajika annual/solar-return chart) ----

@JsonClass(generateAdapter = true)
data class VarshaphalRequest(
    val date: String,
    val time: String,
    val timezone: String? = null,
    val place: String = "",
    val lat: Double,
    val lon: Double,
    val year: Int? = null
)

/** Muntha: whole-sign house of (natal lagna + completed years) from the Varshaphal
 * lagna — the year's "seat," a core Tajika concept with no D1 equivalent
 * (app/services/tajika_varshaphal.py). */
@JsonClass(generateAdapter = true)
data class VarshaphalMuntha(
    val sign: String = "",
    @Json(name = "house_from_varshaphal_lagna") val houseFromVarshaphalLagna: Int? = null,
    val label: String = ""
)

@JsonClass(generateAdapter = true)
data class VarshaphalLagnaMeta(
    val sign: String = "",
    val lord: String? = null
)

@JsonClass(generateAdapter = true)
data class VarshaphalTajika(
    @Json(name = "age_completed") val ageCompleted: Int = 0,
    val muntha: VarshaphalMuntha = VarshaphalMuntha(),
    @Json(name = "varshaphal_lagna") val varshaphalLagna: VarshaphalLagnaMeta = VarshaphalLagnaMeta(),
    @Json(name = "varshaphal_moon") val varshaphalMoon: VarshaphalLagnaMeta = VarshaphalLagnaMeta(),
    @Json(name = "year_lord") val yearLord: String? = null
)

/** `chart` is the same shape POST /v1/chart returns (just computed for the solar-return
 * instant instead of birth), so it reuses [ChartSummaryResponse] rather than a parallel
 * model (app/services/varshaphal.py: compute_varshaphal calls the same compute_chart). */
@JsonClass(generateAdapter = true)
data class VarshaphalResponse(
    val year: Int,
    @Json(name = "solar_return_utc") val solarReturnUtc: String = "",
    @Json(name = "solar_return_local") val solarReturnLocal: String = "",
    @Json(name = "solar_return_sun_error_deg") val solarReturnSunErrorDeg: Double = 0.0,
    val tajika: VarshaphalTajika = VarshaphalTajika(),
    val chart: ChartSummaryResponse
)

// ---- POST /v1/transit (Gochar: planets on a date over the natal chart) ----

@JsonClass(generateAdapter = true)
data class TransitRequest(
    val date: String,
    val time: String,
    val timezone: String? = null,
    val place: String = "",
    val lat: Double,
    val lon: Double,
    @Json(name = "transit_date") val transitDate: String, // YYYY-MM-DD
    @Json(name = "transit_time") val transitTime: String? = null // HH:MM, in `timezone`
)

/** When a transiting planet next enters a new sign; `date` is local "YYYY-MM-DDTHH:MM". */
@JsonClass(generateAdapter = true)
data class TransitSignChange(
    val date: String = "",
    val sign: String = ""
)

/** When a planet next stations; `type` is "retrograde" or "direct". */
@JsonClass(generateAdapter = true)
data class TransitStation(
    val date: String = "",
    val type: String = ""
)

/** One graha's transit position (app/services/transit_service.py). Houses are whole-sign,
 * counted from the natal lagna and from the natal Moon; `gochar_effect` is the classical
 * from-Moon verdict ("favourable" / "challenging"). */
@JsonClass(generateAdapter = true)
data class TransitPlanet(
    val sign: String = "",
    val degree: Double = 0.0,
    val absolute: String = "",
    val retrograde: Boolean = false,
    val combust: Boolean = false,
    val exalted: Boolean = false,
    val debilitated: Boolean = false,
    @Json(name = "house_from_natal_lagna") val houseFromNatalLagna: Int? = null,
    @Json(name = "house_from_natal_moon") val houseFromNatalMoon: Int? = null,
    @Json(name = "gochar_effect") val gocharEffect: String? = null,
    @Json(name = "aspects_natal_houses") val aspectsNatalHouses: List<PlanetAspect> = emptyList(),
    @Json(name = "next_sign_change") val nextSignChange: TransitSignChange? = null,
    @Json(name = "next_station") val nextStation: TransitStation? = null
)

@JsonClass(generateAdapter = true)
data class TransitNatal(
    val lagna: LagnaInfo,
    @Json(name = "moon_sign") val moonSign: String? = null
)

@JsonClass(generateAdapter = true)
data class TransitSnapshot(
    val date: String = "",
    val time: String = "",
    val planets: Map<String, TransitPlanet> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class TransitResponse(
    val natal: TransitNatal,
    val transit: TransitSnapshot = TransitSnapshot()
)

// ---- POST /rules-engine/analyze-chart ----

@JsonClass(generateAdapter = true)
data class RulesEngineBirthDetails(
    @Json(name = "birth_date") val birthDate: String,
    @Json(name = "birth_time") val birthTime: String,
    @Json(name = "birth_place") val birthPlace: String,
    val coordinates: Map<String, Double>? = null,
    val timezone: String? = null
)

@JsonClass(generateAdapter = true)
data class ChartAnalysisRequest(
    @Json(name = "birth_details") val birthDetails: RulesEngineBirthDetails,
    @Json(name = "narration_style") val narrationStyle: String = "product"
)

@JsonClass(generateAdapter = true)
data class RuleEffect(
    @Json(name = "rule_id") val ruleId: String,
    @Json(name = "rule_name") val ruleName: String,
    val weight: Int,
    val tags: List<String> = emptyList(),
    val content: String
)

@JsonClass(generateAdapter = true)
data class ChartAnalysis(
    val narrative: String? = null,
    @Json(name = "positive_effects") val positiveEffects: List<RuleEffect> = emptyList(),
    val challenges: List<RuleEffect> = emptyList(),
    val remedies: List<RuleEffect> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ChartAnalysisResponse(
    val success: Boolean,
    val analysis: ChartAnalysis?
)

/** Classical 27 nakshatras; API `moon_nakshatra.index` is 0-based when present. */
val NAKSHATRA_NAMES: List<String> = listOf(
    "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra", "Punarvasu",
    "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni", "Hasta",
    "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha", "Mula", "Purva Ashadha",
    "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha", "Purva Bhadrapada",
    "Uttara Bhadrapada", "Revati"
)

fun MoonNakshatraInfo.displayName(): String {
    name?.takeIf { it.isNotBlank() }?.let { return it }
    return NAKSHATRA_NAMES.getOrNull(index) ?: "Nakshatra $index"
}
