package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** GET /v1/festivals (app/services/festival_service.py): a year's major Hindu festivals. */
@JsonClass(generateAdapter = true)
data class FestivalCalendarResponse(
    val year: Int,
    val festivals: List<Festival> = emptyList()
)

/** name is English (kept for the website), name_hi is Hindi; date is yyyy-MM-dd. */
@JsonClass(generateAdapter = true)
data class Festival(
    val id: String = "",
    val name: String,
    @Json(name = "name_hi") val nameHi: String = "",
    val date: String
) {
    fun nameFor(languageCode: String): String = if (languageCode == "hi") nameHi.ifBlank { name } else name
}
