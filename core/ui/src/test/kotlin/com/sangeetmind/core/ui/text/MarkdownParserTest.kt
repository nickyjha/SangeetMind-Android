package com.sangeetmind.core.ui.text

import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownParserTest {

    @Test
    fun boldAndItalicInline() {
        val spans = MarkdownParser.parseInline("Your **Moon** is *strong* today")
        assertEquals(
            listOf(
                MdSpan("Your "),
                MdSpan("Moon", bold = true),
                MdSpan(" is "),
                MdSpan("strong", italic = true),
                MdSpan(" today")
            ),
            spans
        )
    }

    @Test
    fun unmatchedMarkersStayLiteral() {
        assertEquals(listOf(MdSpan("5 * 3 = 15 and **open")), MarkdownParser.parseInline("5 * 3 = 15 and **open"))
    }

    @Test
    fun headingsBulletsAndParagraphs() {
        val md = """
            ## Career
            Work goes **well**.
            This line joins.

            - First point
            • Second point
            1. Numbered
            ### Small
        """.trimIndent()
        val blocks = MarkdownParser.parse(md)
        assertEquals(
            listOf(
                MdBlock.Heading(2, listOf(MdSpan("Career"))),
                MdBlock.Paragraph(listOf(MdSpan("Work goes "), MdSpan("well", bold = true), MdSpan(". This line joins."))),
                MdBlock.ListItem("•", listOf(MdSpan("First point"))),
                MdBlock.ListItem("•", listOf(MdSpan("Second point"))),
                MdBlock.ListItem("1.", listOf(MdSpan("Numbered"))),
                MdBlock.Heading(3, listOf(MdSpan("Small")))
            ),
            blocks
        )
    }

    @Test
    fun boldWrappedBulletAndHorizontalRule() {
        val blocks = MarkdownParser.parse("* **Remedy:** chant daily\n---\nDone")
        assertEquals(
            listOf(
                MdBlock.ListItem("•", listOf(MdSpan("Remedy:", bold = true), MdSpan(" chant daily"))),
                MdBlock.Paragraph(listOf(MdSpan("Done")))
            ),
            blocks
        )
    }

    @Test
    fun plainTextStripsMarkers() {
        assertEquals("Title\n\nHello world\n\n• a", MarkdownParser.toPlainText("# Title\nHello **world**\n- a"))
    }

    @Test
    fun hindiTextSurvives() {
        assertEquals(
            listOf(MdBlock.Paragraph(listOf(MdSpan("आज "), MdSpan("शुभ", bold = true), MdSpan(" दिन है"))))
            , MarkdownParser.parse("आज **शुभ** दिन है")
        )
    }
}
