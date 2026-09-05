package com.sangeetmind.libs.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PanchangIndexName(
    val index: Int,
    val name: String
)

@JsonClass(generateAdapter = true)
data class PanchangIndex(
    val index: Int
)

/** GET /v1/panchang response (app/services/panchang_service.py::compute_panchang). */
@JsonClass(generateAdapter = true)
data class PanchangResponse(
    val date: String,
    val latitude: Double,
    val longitude: Double,
    val tithi: PanchangIndexName,
    val nakshatra: PanchangIndexName,
    val yoga: PanchangIndexName,
    val karana: PanchangIndex,
    val vara: PanchangIndexName,
    val moonSign: String,
    val sunSign: String
)
