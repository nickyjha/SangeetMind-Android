package com.sangeetmind.features.astrology.readings

/**
 * Matches the price lead-in of a reading description, in English and Hindi:
 * "₹99, or free with Premium — ", "₹99, or free with Premium. ",
 * "₹99 per question, or free with Premium. ", "₹99, या प्रीमियम में मुफ़्त। ",
 * "₹99, या प्रीमियम में फ़्री — ", "हर सवाल ₹99, या प्रीमियम में मुफ़्त। ".
 */
private val PRICE_PREFIX = Regex(
    """^[^₹]{0,12}₹\d+[^,]{0,20}, [^.।—]{0,40}(?:Premium|प्रीमियम)[^.।—]{0,12}(?:\s*—\s*|[.।]\s*)"""
)

/**
 * Removes the leading "₹NN, or free with Premium — / . " (or its Hindi form) from a reading
 * description, so the text reads honestly while readings are free during the beta.
 * Text without that prefix is returned unchanged.
 */
fun stripPricePrefix(text: String): String {
    val match = PRICE_PREFIX.find(text) ?: return text
    val rest = text.substring(match.range.last + 1).trimStart()
    if (rest.isEmpty()) return text
    return rest.replaceFirstChar { it.uppercaseChar() }
}
