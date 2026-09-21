package com.sangeetmind.core.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// The Navagraha System — SangeetMind's palette, drawn from the nine classical
// grahas rather than an arbitrary brand color. Dark theme is the primary,
// intended look (a night sky at brahma muhurat); light theme is a parchment
// variant of the same hues, not a separate design.
// ---------------------------------------------------------------------------

// Dark theme — night sky
val Void = Color(0xFF0B0A1A)
val Dusk = Color(0xFF15132C)
val DuskElevated = Color(0xFF1F1C3D)
val Hairline = Color(0xFF332F5C)
val Moonlight = Color(0xFFE7E5F7)

// Lightened from an earlier A19DC4 — that read as "soft" rather than intentionally
// muted (7:1 contrast on paper, but low hue-contrast against Dusk made it feel murky
// at a glance). BDBAE0 keeps the lavender-grey "secondary" identity at ~9.7-10.5:1.
val Starlight = Color(0xFFBDBAE0)

// Light theme — parchment (same hues, inverted ground)
val Parchment = Color(0xFFFBF7F0)
val ParchmentSurface = Color(0xFFF3EDE2)
val ParchmentElevated = Color(0xFFEAE1D2)
val InkHairline = Color(0xFFDCD2BE)
val Ink = Color(0xFF211C3D)

// Darkened from an earlier 5B5580 for the same reason as Starlight below — this is
// the label color for nearly every row in the app (Lagna, Nakshatra, dasha lords),
// so it needs to read instantly, not just pass a contrast checker on paper.
val InkMuted = Color(0xFF433D66)

// ---------------------------------------------------------------------------
// Graha accents. Each graha has TWO tones, not one, because "gold" and "indigo"
// text/icons that look sharp on a near-black surface go nearly invisible on a
// parchment one (and vice versa for filled tiles with white text on top):
//  - the plain name (e.g. GrahaSurya) is the DARK-theme tone: bright, used as
//    text/icon color on dark surfaces, and as the vivid end of a tile gradient.
//  - the "...Deep" tone is the LIGHT-theme tone: darkened for AA contrast as
//    text/icon color on parchment surfaces, and doubles as the tile-gradient's
//    anchor stop so white overlay text stays readable in both themes.
// Resolve the theme-correct one via LocalGrahaColors (see Theme.kt) rather than
// reaching for these directly from a screen.
// ---------------------------------------------------------------------------
val GrahaSurya = Color(0xFFF2A93B)      // Sun — gold. Horoscope, primary/CTA.
val GrahaSuryaDeep = Color(0xFF7A4A0E)
val GrahaChandra = Color(0xFF9FC0E8)    // Moon — pale blue. Panchang.
val GrahaChandraDeep = Color(0xFF2E5480)
val GrahaMangala = Color(0xFFF17F5E)    // Mars — vermilion. Match / manglik dosha.
val GrahaMangalaDeep = Color(0xFF9E3419)
val GrahaBudha = Color(0xFF3FBF8F)      // Mercury — jade. Numerology.
val GrahaBudhaDeep = Color(0xFF19563E)
val GrahaGuru = Color(0xFFF2C94C)       // Jupiter — amber. Muhurat.
val GrahaGuruDeep = Color(0xFF6B5209)
val GrahaShukra = Color(0xFFE893B8)     // Venus — rose. Full Reading / premium.
val GrahaShukraDeep = Color(0xFF8A3D60)
val GrahaShani = Color(0xFF8B96F5)      // Saturn — indigo. Birth Chart (foundation).
val GrahaShaniDeep = Color(0xFF333E9E)
val GrahaRahu = Color(0xFFB0A3D6)       // Rahu — violet haze. ChatMind (the novel, the technological).
val GrahaRahuDeep = Color(0xFF4F4278)

// Semantic
val Sindoor = Color(0xFFE8613C)         // alerts, doshas, destructive actions (= Mangala)
val SindoorContainerDark = Color(0xFF3A2119)
val SindoorContainerLight = Color(0xFFFBE2DA)

// ---------------------------------------------------------------------------
// Material3 color-scheme mappings
// ---------------------------------------------------------------------------

// Light
val Primary = GrahaSuryaDeep
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFFCE3BC)
val OnPrimaryContainer = Color(0xFF3D2600)

val Secondary = GrahaShaniDeep
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFDBE0FB)
val OnSecondaryContainer = Color(0xFF1A1F4D)

val Tertiary = GrahaShukraDeep
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFFBDCE9)
val OnTertiaryContainer = Color(0xFF3D1225)

val Error = GrahaMangalaDeep
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = SindoorContainerLight
val OnErrorContainer = Color(0xFF410E00)

val Background = Parchment
val OnBackground = Ink
val Surface = ParchmentSurface
val OnSurface = Ink

val SurfaceVariant = ParchmentElevated
val OnSurfaceVariant = InkMuted
val Outline = InkHairline

// Dark
val DarkPrimary = GrahaSurya
val DarkOnPrimary = Color(0xFF3D2600)
val DarkPrimaryContainer = Color(0xFF5C3D00)
val DarkOnPrimaryContainer = Color(0xFFFCE3BC)

val DarkSecondary = GrahaShani
val DarkOnSecondary = Color(0xFF10163F)
val DarkSecondaryContainer = Color(0xFF2B3270)
val DarkOnSecondaryContainer = Color(0xFFDBE0FB)

val DarkTertiary = GrahaShukra
val DarkOnTertiary = Color(0xFF3D1225)
val DarkTertiaryContainer = Color(0xFF5C2B41)
val DarkOnTertiaryContainer = Color(0xFFFBDCE9)

val DarkError = GrahaMangala
val DarkOnError = Color(0xFF3A2119)
val DarkErrorContainer = SindoorContainerDark
val DarkOnErrorContainer = Color(0xFFFBDCE9)

val DarkBackground = Void
val DarkOnBackground = Moonlight
val DarkSurface = Dusk
val DarkOnSurface = Moonlight

val DarkSurfaceVariant = DuskElevated
val DarkOnSurfaceVariant = Starlight
val DarkOutline = Hairline
