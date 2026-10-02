package com.sangeetmind.features.astrology.referrals

/** Input rules for referral codes; the backend matches short codes case-insensitively. */
object ReferralCodes {
    /** Short codes are at most 9 chars (NICKY + 3-4); legacy uid codes are ~28. */
    private const val SHORT_CODE_MAX_LEN = 12

    /**
     * Trims and drops inner whitespace. Short codes are upper-cased; anything longer is a
     * legacy raw-uid code, which is case-sensitive, so it is left as typed.
     */
    fun normalizeInput(raw: String): String {
        val compact = raw.filterNot { it.isWhitespace() }
        return if (compact.length <= SHORT_CODE_MAX_LEN) compact.uppercase() else compact
    }
}
