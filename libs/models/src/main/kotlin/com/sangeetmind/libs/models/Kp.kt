package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** GET /v1/kp/ruling-planets (app/services/kp_horary.py). */
@JsonClass(generateAdapter = true)
data class RulingPlanetsResponse(
    val moment: String = "",
    val slots: List<RulingSlot> = emptyList(),
    @Json(name = "ruling_planets") val rulingPlanets: List<String> = emptyList()
)

/** role: lagna_star | lagna_sign | lagna_sub | moon_star | moon_sign | moon_sub | day */
@JsonClass(generateAdapter = true)
data class RulingSlot(
    val role: String,
    val planet: String
)

@JsonClass(generateAdapter = true)
data class PrashnaRequest(
    val number: Int,
    val question: String, // marriage | job | property | abroad | children | exam
    val lat: Double,
    val lon: Double,
    val tz: String = "Asia/Kolkata"
)

@JsonClass(generateAdapter = true)
data class PrashnaSignifications(
    @Json(name = "star_lord") val starLord: String = "",
    @Json(name = "via_star") val viaStar: List<Int> = emptyList(),
    val self: List<Int> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PrashnaAscendant(
    val sign: String = "",
    val degree: Double = 0.0,
    @Json(name = "star_lord") val starLord: String = "",
    @Json(name = "sub_lord") val subLord: String = ""
)

/** POST /v1/kp/prashna: verdict is "yes" | "delayed" | "no". */
@JsonClass(generateAdapter = true)
data class PrashnaResponse(
    val number: Int,
    val question: String,
    @Json(name = "question_text") val questionText: LocalizedText = LocalizedText(),
    val ascendant: PrashnaAscendant = PrashnaAscendant(),
    @Json(name = "main_house") val mainHouse: Int = 0,
    @Json(name = "cusp_sub_lord") val cuspSubLord: String = "",
    val significations: PrashnaSignifications = PrashnaSignifications(),
    @Json(name = "favourable_houses") val favourableHouses: List<Int> = emptyList(),
    @Json(name = "unfavourable_houses") val unfavourableHouses: List<Int> = emptyList(),
    val verdict: String = "",
    val reason: LocalizedText = LocalizedText(),
    @Json(name = "ruling_planets") val rulingPlanets: List<String> = emptyList(),
    @Json(name = "fruitful_planets") val fruitfulPlanets: List<String> = emptyList()
)
