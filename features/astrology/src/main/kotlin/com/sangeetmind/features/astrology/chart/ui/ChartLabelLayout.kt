package com.sangeetmind.features.astrology.chart.ui

/**
 * Pure layout for the planet labels inside one chart house/cell, in the chart's 400×400
 * viewBox units (the caller multiplies by its pixel scale).
 *
 * A house has a rectangular "safe area" that lies fully inside its polygon. The block we
 * place there is an optional header line (the rashi number on the North chart) followed by
 * the planet labels in rows. If the labels don't fit in one column at the base font, we try
 * smaller fonts and 2–3 columns, never going below [minFont], and pick the first layout that
 * fits; if none fits we use the most compact one (smallest font, most useful columns).
 */
internal data class SafeRect(val cx: Float, val cy: Float, val width: Float, val height: Float) {
    val left: Float get() = cx - width / 2f
    val top: Float get() = cy - height / 2f
}

/** A text anchor: [x] is the horizontal centre of the text, [y] its baseline. */
internal data class LabelPos(val x: Float, val y: Float)

internal data class HouseLabelLayout(
    val fontSize: Float,
    val columns: Int,
    /** Where the header (rashi number) goes, or null when there is no header. */
    val header: LabelPos?,
    val labels: List<LabelPos>,
    /** False only when even the most compact option overflows the safe area. */
    val fits: Boolean
)

internal const val LABEL_LINE_HEIGHT = 1.3f // × font size
internal const val LABEL_COLUMN_GAP = 4f // viewBox units between columns
private const val BASELINE_FRACTION = 0.8f // baseline sits at ~80% of the line box

/** North Indian safe areas (viewBox units). Diamonds 1/4/7/10 are larger than the corner triangles. */
internal val NORTH_SAFE_RECTS: Map<Int, SafeRect> = mapOf(
    // Diamonds: half-diagonals of 100, so |dx|/100 + |dy|/100 ≤ 1 → 110×84 fits.
    1 to SafeRect(200f, 100f, 110f, 84f),
    4 to SafeRect(100f, 200f, 110f, 84f),
    7 to SafeRect(200f, 300f, 110f, 84f),
    10 to SafeRect(300f, 200f, 110f, 84f),
    // Top/bottom triangles (base on the outer edge, width 200, depth 100): 96 wide × 50 deep.
    2 to SafeRect(100f, 27f, 96f, 50f),
    12 to SafeRect(300f, 27f, 96f, 50f),
    6 to SafeRect(100f, 373f, 96f, 50f),
    8 to SafeRect(300f, 373f, 96f, 50f),
    // Side triangles: the same shape rotated, so 50 wide × 96 tall.
    3 to SafeRect(27f, 100f, 50f, 96f),
    5 to SafeRect(27f, 300f, 50f, 96f),
    9 to SafeRect(373f, 300f, 50f, 96f),
    11 to SafeRect(373f, 100f, 50f, 96f)
)

/**
 * @param labelWidthsPerUnitFont each label's width when drawn at font size 1.
 * @param baseFont preferred font size; [minFont] is the floor (≥ 8sp in the caller).
 * @param headerFont font of the header line, or null for no header.
 */
internal fun layoutHouseLabels(
    rect: SafeRect,
    labelWidthsPerUnitFont: List<Float>,
    baseFont: Float,
    minFont: Float,
    headerFont: Float? = null
): HouseLabelLayout {
    val n = labelWidthsPerUnitFont.size
    val headerH = headerFont?.let { it * LABEL_LINE_HEIGHT } ?: 0f
    val floor = minOf(minFont, baseFont)
    val maxW = labelWidthsPerUnitFont.maxOrNull() ?: 0f

    // Candidates in order of preference: keep one column and the full size as long as
    // possible, then trade a little size, then add columns.
    val candidates = buildList {
        add(1 to 1f); add(1 to 0.92f)
        add(2 to 1f); add(2 to 0.92f); add(1 to 0.85f); add(2 to 0.85f)
        add(2 to 0.78f); add(3 to 0.85f); add(3 to 0.78f); add(2 to 0.7f); add(3 to 0.7f)
    }.filter { (cols, _) -> cols == 1 || cols <= n }
        .map { (cols, s) -> cols to maxOf(baseFont * s, floor) }
        .distinct()

    fun size(cols: Int, font: Float): Pair<Float, Float> {
        val rows = if (n == 0) 0 else (n + cols - 1) / cols
        val w = cols * maxW * font + (cols - 1) * LABEL_COLUMN_GAP
        val h = headerH + rows * font * LABEL_LINE_HEIGHT
        return w to h
    }

    var chosen = candidates.firstOrNull { (cols, font) ->
        val (w, h) = size(cols, font)
        w <= rect.width && h <= rect.height
    }
    val fits = chosen != null
    if (chosen == null) {
        // Nothing fits: take the option that overflows least (by area ratio).
        chosen = candidates.minByOrNull { (cols, font) ->
            val (w, h) = size(cols, font)
            maxOf(w / rect.width, 1f) * maxOf(h / rect.height, 1f)
        } ?: (1 to baseFont)
    }
    val (cols, font) = chosen
    val (_, blockH) = size(cols, font)
    val lineH = font * LABEL_LINE_HEIGHT

    // Centre the block vertically inside the safe area.
    var y = rect.cy - blockH / 2f
    val header = headerFont?.let {
        val pos = LabelPos(rect.cx, y + it * LABEL_LINE_HEIGHT * BASELINE_FRACTION)
        y += headerH
        pos
    }
    val colW = maxW * font
    val totalW = cols * colW + (cols - 1) * LABEL_COLUMN_GAP
    val firstColCx = rect.cx - totalW / 2f + colW / 2f
    val labels = (0 until n).map { i ->
        val row = i / cols
        val col = i % cols
        // A last row that is not full is centred rather than left-aligned.
        val itemsInRow = minOf(cols, n - row * cols)
        val rowOffset = (cols - itemsInRow) * (colW + LABEL_COLUMN_GAP) / 2f
        LabelPos(
            x = firstColCx + rowOffset + col * (colW + LABEL_COLUMN_GAP),
            y = y + row * lineH + lineH * BASELINE_FRACTION
        )
    }
    return HouseLabelLayout(font, cols, header, labels, fits)
}
