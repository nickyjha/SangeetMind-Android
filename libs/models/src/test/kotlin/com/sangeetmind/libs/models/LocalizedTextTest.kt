package com.sangeetmind.libs.models

import org.junit.Assert.assertEquals
import org.junit.Test

class LocalizedTextTest {
    private val text = LocalizedText(en = "A self-made career.", hi = "अपने दम पर करियर।")

    @Test
    fun picksHindiForHindi() = assertEquals("अपने दम पर करियर।", text.forLanguage("hi"))

    @Test
    fun fallsBackToEnglishForOtherLanguages() = assertEquals("A self-made career.", text.forLanguage("ta"))

    @Test
    fun fallsBackToEnglishWhenHindiMissing() =
        assertEquals("A self-made career.", LocalizedText(en = "A self-made career.").forLanguage("hi"))
}
