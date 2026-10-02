package com.sangeetmind.core.ui.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

/** Builds an [AnnotatedString] for one run of inline spans. */
fun List<MdSpan>.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
    for (span in this@toAnnotatedString) {
        val style = SpanStyle(
            fontWeight = if (span.bold) FontWeight.Bold else null,
            fontStyle = if (span.italic) FontStyle.Italic else null
        )
        withStyle(style) { append(span.text) }
    }
}

/** Inline-only rendering (bold/italic) for single-line places such as chips or titles. */
fun markdownInline(text: String): AnnotatedString =
    MarkdownParser.parseInline(text).toAnnotatedString()

/**
 * Renders AI-generated text that may contain light Markdown (see [MarkdownParser]) as
 * styled paragraphs, headings and bullet lists instead of raw `**` / `##` / `-` markers.
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified,
    blockSpacing: androidx.compose.ui.unit.Dp = 8.dp
) {
    val blocks = remember(markdown) { MarkdownParser.parse(markdown) }
    val resolvedColor = if (color != Color.Unspecified) color else LocalContentColor.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(blockSpacing)) {
        for (block in blocks) {
            when (block) {
                is MdBlock.Heading -> Text(
                    text = block.spans.toAnnotatedString(),
                    style = when (block.level) {
                        1 -> MaterialTheme.typography.titleMedium
                        2 -> MaterialTheme.typography.titleSmall
                        else -> style
                    },
                    fontWeight = FontWeight.Bold,
                    color = resolvedColor
                )
                is MdBlock.Paragraph -> Text(
                    text = block.spans.toAnnotatedString(),
                    style = style,
                    color = resolvedColor
                )
                is MdBlock.ListItem -> Row(modifier = Modifier.padding(start = 4.dp)) {
                    Text(
                        text = block.marker,
                        style = style,
                        color = resolvedColor,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = block.spans.toAnnotatedString(),
                        style = style,
                        color = resolvedColor
                    )
                }
            }
        }
    }
}
