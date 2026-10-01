package com.sangeetmind.libs.models

import com.squareup.moshi.JsonClass

// ---- Non-marital compatibility: POST /v1/match/relation (relation_match_service.py) ----
// relation: parent_child | siblings | business | friends. Person A is the elder where the
// relation is directional. Six factors out of 36; every factor has an en + hi reason.

@JsonClass(generateAdapter = true)
data class RelationMatchRequest(
    val personA: MatchBirthDetails,
    val personB: MatchBirthDetails,
    val relation: String
)

@JsonClass(generateAdapter = true)
data class RelationMatchEnvelope(
    val success: Boolean,
    val match: RelationMatchResult
)

@JsonClass(generateAdapter = true)
data class RelationFactor(
    val key: String = "",
    val label: LocalizedText = LocalizedText(),
    val points: Double = 0.0,
    val max: Double = 0.0,
    // good | neutral | caution
    val status: String = "",
    val reason: LocalizedText = LocalizedText()
)

@JsonClass(generateAdapter = true)
data class RelationRoles(
    val personA: LocalizedText = LocalizedText(),
    val personB: LocalizedText = LocalizedText()
)

@JsonClass(generateAdapter = true)
data class RelationPerson(
    val moonSign: String = "",
    val lagna: String = ""
)

@JsonClass(generateAdapter = true)
data class RelationMatchResult(
    val relation: String = "",
    val roles: RelationRoles = RelationRoles(),
    val personA: RelationPerson = RelationPerson(),
    val personB: RelationPerson = RelationPerson(),
    val factors: List<RelationFactor> = emptyList(),
    val total: Double = 0.0,
    val max: Double = 36.0,
    // strong | good | mixed | careful
    val level: String = "",
    val summary: LocalizedText = LocalizedText(),
    val tips: List<LocalizedText> = emptyList()
)
