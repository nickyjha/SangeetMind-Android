package com.sangeetmind.features.auth

/**
 * Pure (no Android dependency) helpers for the phone-OTP sign-in path so they can be
 * unit-tested on the JVM. Everything that touches Firebase lives in [AuthRepository].
 */
object PhoneAuthHelpers {

    const val DEFAULT_COUNTRY_CODE = "91"
    const val OTP_LENGTH = 6
    const val RESEND_WINDOW_SECONDS = 30

    /** E.164 allows at most 15 digits in total (country code + subscriber number). */
    private const val E164_MAX_DIGITS = 15
    private const val MIN_SUBSCRIBER_DIGITS = 4

    /** Keeps digits only, max 3 (country codes are 1–3 digits). Leading "+" / "00" are dropped. */
    fun sanitizeCountryCodeInput(input: String): String {
        val digits = input.trimStart('+').removePrefix("00").filter { it.isDigit() }
        return digits.take(3)
    }

    /**
     * Keeps digits only and drops a leading trunk "0" (so "098765…" becomes "98765…").
     * India numbers are capped at 10 digits; others at 14.
     */
    fun sanitizePhoneInput(input: String, countryCode: String): String {
        val digits = input.filter { it.isDigit() }
            .let { if (it.length > 1 && it.startsWith("0")) it.substring(1) else it }
        val max = if (sanitizeCountryCodeInput(countryCode) == DEFAULT_COUNTRY_CODE) 10 else 14
        return digits.take(max)
    }

    /** Keeps digits only, capped at [OTP_LENGTH]. */
    fun sanitizeOtpInput(input: String): String =
        input.filter { it.isDigit() }.take(OTP_LENGTH)

    /** A valid OTP is exactly six digits. */
    fun isValidOtp(code: String): Boolean =
        code.length == OTP_LENGTH && code.all { it.isDigit() }

    /**
     * Normalises a user-typed country code + number into E.164 ("+919876543210").
     *
     * - Spaces, dashes, dots and brackets in the number are ignored.
     * - If the number itself starts with "+", it is treated as already international and
     *   the country code field is ignored.
     * - A single leading trunk "0" (as Indians commonly dial, "09876543210") is stripped.
     * - For India (+91) the subscriber number must be exactly 10 digits starting with 6–9.
     * - For other countries the total length must fit E.164 (max 15 digits).
     *
     * Returns null when the input cannot be a valid phone number.
     */
    fun normalizeToE164(countryCode: String, number: String): String? {
        val rawNumber = number.trim()
        val cc: String
        val subscriber: String
        if (rawNumber.startsWith("+")) {
            val digits = rawNumber.filter { it.isDigit() }
            if (digits.length !in (MIN_SUBSCRIBER_DIGITS + 1)..E164_MAX_DIGITS) return null
            // Cannot reliably split cc / number for arbitrary countries; special-case India.
            return if (digits.startsWith(DEFAULT_COUNTRY_CODE) && digits.length == 12) {
                val sub = digits.substring(2)
                if (isValidIndianSubscriber(sub)) "+$digits" else null
            } else {
                "+$digits"
            }
        } else {
            cc = sanitizeCountryCodeInput(countryCode)
            var digits = rawNumber.filter { it.isDigit() }
            if (digits.startsWith("0") && digits.length > 1) digits = digits.substring(1)
            subscriber = digits
        }

        if (cc.isEmpty() || cc.startsWith("0")) return null
        if (subscriber.length < MIN_SUBSCRIBER_DIGITS) return null
        if (cc.length + subscriber.length > E164_MAX_DIGITS) return null

        if (cc == DEFAULT_COUNTRY_CODE && !isValidIndianSubscriber(subscriber)) return null

        return "+$cc$subscriber"
    }

    /** Indian mobile numbers: 10 digits, first digit 6–9. */
    fun isValidIndianSubscriber(subscriber: String): Boolean =
        subscriber.length == 10 && subscriber.all { it.isDigit() } && subscriber[0] in '6'..'9'

    /**
     * Pretty form for "OTP sent to …" messages, e.g. "+91 98765 43210" for India, otherwise
     * the E.164 string as-is.
     */
    fun formatForDisplay(e164: String): String {
        val digits = e164.removePrefix("+")
        return if (digits.startsWith(DEFAULT_COUNTRY_CODE) && digits.length == 12) {
            "+91 ${digits.substring(2, 7)} ${digits.substring(7)}"
        } else {
            e164
        }
    }

    /**
     * Seconds left (rounded up) before "Resend OTP" may be tapped again. Never negative.
     * Timestamps are epoch millis so the countdown survives a recomposition or a paused app.
     */
    fun resendSecondsRemaining(
        sentAtMillis: Long,
        nowMillis: Long,
        windowSeconds: Int = RESEND_WINDOW_SECONDS
    ): Int {
        if (windowSeconds <= 0) return 0
        val elapsedMillis = nowMillis - sentAtMillis
        if (elapsedMillis < 0) return windowSeconds
        val remainingMillis = windowSeconds * 1000L - elapsedMillis
        if (remainingMillis <= 0) return 0
        return ((remainingMillis + 999) / 1000).toInt()
    }

    fun canResend(sentAtMillis: Long, nowMillis: Long, windowSeconds: Int = RESEND_WINDOW_SECONDS): Boolean =
        resendSecondsRemaining(sentAtMillis, nowMillis, windowSeconds) == 0
}
