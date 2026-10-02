package com.sangeetmind.core.ui.text

/**
 * A deliberately tiny Markdown subset for AI-generated text: `**bold**`, `__bold__`,
 * `*italic*`, `#`/`##`/`###` headings, `-`/`*`/`•` bullets, `1.` numbered items and
 * blank-line separated paragraphs. Pure Kotlin so it can be unit-tested without Compose;
 * [MarkdownText] renders the result.
 */
data class MdSpan(val text: String, val bold: Boolean = false, val italic: Boolean = false)

sealed class MdBlock {
    data class Heading(val level: Int, val spans: List<MdSpan>) : MdBlock()
    data class Paragraph(val spans: List<MdSpan>) : MdBlock()

    /** [marker] is "•" for unordered items or e.g. "1." for numbered ones. */
    data class ListItem(val marker: String, val spans: List<MdSpan>) : MdBlock()
}

object MarkdownParser {
    private val headingRe = Regex("^(#{1,6})\\s+(.*)$")
    private val bulletRe = Regex("^[-*•·]\\s+(.*)$")
    private val numberedRe = Regex("^(\\d{1,3})[.)]\\s+(.*)$")
    private val ruleRe = Regex("^([-*_])\\1{2,}$")

    fun parse(markdown: String): List<MdBlock> {
        val blocks = mutableListOf<MdBlock>()
        val paragraph = StringBuilder()

        fun flushParagraph() {
            if (paragraph.isNotBlank()) {
                blocks += MdBlock.Paragraph(parseInline(paragraph.toString().trim()))
            }
            paragraph.clear()
        }

        for (rawLine in markdown.replace("\r\n", "\n").split('\n')) {
            val line = rawLine.trim()
            if (line.isEmpty()) {
                flushParagraph()
                continue
            }
            if (ruleRe.matches(line)) {
                flushParagraph()
                continue
            }
            headingRe.matchEntire(line)?.let { m ->
                flushParagraph()
                val level = m.groupValues[1].length.coerceAtMost(3)
                // Headings are already bold; strip redundant ** wrappers inside them.
                val text = m.groupValues[2].trim().trimEnd('#').trim()
                blocks += MdBlock.Heading(level, parseInline(text))
                return@let
            } ?: bulletRe.matchEntire(line)?.let { m ->
                flushParagraph()
                blocks += MdBlock.ListItem("•", parseInline(m.groupValues[1].trim()))
            } ?: numberedRe.matchEntire(line)?.let { m ->
                flushParagraph()
                blocks += MdBlock.ListItem("${m.groupValues[1]}.", parseInline(m.groupValues[2].trim()))
            } ?: run {
                if (paragraph.isNotEmpty()) paragraph.append(' ')
                paragraph.append(line)
            }
        }
        flushParagraph()
        return blocks
    }

    /** Splits a single line into bold/italic runs. Unmatched markers are kept literally. */
    fun parseInline(text: String): List<MdSpan> {
        val spans = mutableListOf<MdSpan>()
        val buf = StringBuilder()
        var bold = false
        var italic = false
        var i = 0

        fun flush() {
            if (buf.isNotEmpty()) {
                val s = MdSpan(buf.toString(), bold, italic)
                val last = spans.lastOrNull()
                if (last != null && last.bold == s.bold && last.italic == s.italic) {
                    spans[spans.size - 1] = last.copy(text = last.text + s.text)
                } else {
                    spans += s
                }
                buf.clear()
            }
        }

        while (i < text.length) {
            val c = text[i]
            val isDouble = i + 1 < text.length && text[i + 1] == c && (c == '*' || c == '_')
            when {
                isDouble && (bold || text.indexOf("$c$c", i + 2) > i + 2) -> {
                    flush(); bold = !bold; i += 2
                }
                c == '*' && !isDouble && (italic || hasClosingItalic(text, i)) -> {
                    flush(); italic = !italic; i += 1
                }
                else -> {
                    buf.append(c); i += 1
                }
            }
        }
        flush()
        return spans
    }

    /** `*` opens italic only if followed by a non-space and closed later by a single `*`. */
    private fun hasClosingItalic(text: String, start: Int): Boolean {
        if (start + 1 >= text.length || text[start + 1].isWhitespace()) return false
        var j = start + 1
        while (j < text.length) {
            if (text[j] == '*') {
                val doubled = j + 1 < text.length && text[j + 1] == '*'
                if (!doubled && !text[j - 1].isWhitespace()) return true
                if (doubled) j++
            }
            j++
        }
        return false
    }

    /** Plain text with all markers removed — handy for share/copy actions. */
    fun toPlainText(markdown: String): String =
        parse(markdown).joinToString("\n\n") { block ->
            when (block) {
                is MdBlock.Heading -> block.spans.joinToString("") { it.text }
                is MdBlock.Paragraph -> block.spans.joinToString("") { it.text }
                is MdBlock.ListItem -> "${block.marker} " + block.spans.joinToString("") { it.text }
            }
        }
}
