package com.sangeetmind.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sangeetmind.libs.models.Intensity
import com.sangeetmind.libs.models.Mood
import com.sangeetmind.libs.models.Raag
import com.sangeetmind.libs.models.TimeOfDay

@Entity(tableName = "raags")
data class RaagEntity(
    @PrimaryKey val id: String,
    val name: String,
    val nameHindi: String?,
    val description: String,
    val artworkUrl: String?,
    val audioUrl: String,
    val durationSeconds: Int,
    val tags: List<String>,
    val timeOfDay: TimeOfDay?,
    val mood: Mood?,
    val intensity: Intensity?,
    val isFavorite: Boolean,
    val isDownloaded: Boolean,
    val localAudioPath: String?
)

fun RaagEntity.toRaag(): Raag = Raag(
    id = id,
    name = name,
    nameHindi = nameHindi,
    description = description,
    artworkUrl = artworkUrl,
    audioUrl = audioUrl,
    durationSeconds = durationSeconds,
    tags = tags,
    timeOfDay = timeOfDay,
    mood = mood,
    intensity = intensity,
    isFavorite = isFavorite,
    isDownloaded = isDownloaded
)

fun Raag.toEntity(localAudioPath: String? = null): RaagEntity = RaagEntity(
    id = id,
    name = name,
    nameHindi = nameHindi,
    description = description,
    artworkUrl = artworkUrl,
    audioUrl = audioUrl,
    durationSeconds = durationSeconds,
    tags = tags,
    timeOfDay = timeOfDay,
    mood = mood,
    intensity = intensity,
    isFavorite = isFavorite,
    isDownloaded = isDownloaded,
    localAudioPath = localAudioPath
)

