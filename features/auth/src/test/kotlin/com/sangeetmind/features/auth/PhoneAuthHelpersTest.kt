package com.sangeetmind.features.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneAuthHelpersTest {

    // ---- normalizeToE164 ----------------------------------------------------------------

    @Test
    fun `indian number with default code normalises to e164`() {
        assertEquals("+919876543210", PhoneAuthHelpers.normalizeToE164("91", "9876543210"))
    }

    @Test
    fun `spaces dashes and brackets in number are ignored`() {
        assertEquals("+919876543210", PhoneAuthHelpers.normalizeToE164("91", "98765 43210"))
        assertEquals("+919876543210", PhoneAuthHelpers.normalizeToE164("91", "98765-43210"))
        assertEquals("+919876543210", PhoneAuthHelpers.normalizeToE164("91", "(98765) 43210"))
    }

    @Test
    fun `leading trunk zero is stripped`() {
        assertEquals("+919876543210", PhoneAuthHelpers.normalizeToE164("91", "09876543210"))
    }

    @Test
    fun `country code may be typed with plus or 00 prefix`() {
        assertEquals("+919876543210", PhoneAuthHelpers.normalizeToE164("+91", "9876543210"))
        assertEquals("+919876543210", PhoneAuthHelpers.normalizeToE164("0091", "9876543210"))
    }

    @Test
    fun `number already in international form ignores the country code field`() {
        assertEquals("+919876543210", PhoneAuthHelpers.normalizeToE164("1", "+91 98765 43210"))
        assertEquals("+14155552671", PhoneAuthHelpers.normalizeToE164("91", "+1 415 555 2671"))
    }

    @Test
    fun `indian number must be 10 digits starting 6 to 9`() {
        assertNull(PhoneAuthHelpers.normalizeToE164("91", "987654321"))    // 9 digits
        assertNull(PhoneAuthHelpers.normalizeToE164("91", "98765432100"))  // 11 digits
        assertNull(PhoneAuthHelpers.normalizeToE164("91", "5876543210"))   // starts with 5
        assertNull(PhoneAuthHelpers.normalizeToE164("91", "+915876543210"))
    }

    @Test
    fun `non indian numbers accept other lengths within e164 limits`() {
        assertEquals("+447911123456", PhoneAuthHelpers.normalizeToE164("44", "7911123456"))
        assertEquals("+14155552671", PhoneAuthHelpers.normalizeToE164("1", "4155552671"))
        assertEquals("+6598765432", PhoneAuthHelpers.normalizeToE164("65", "98765432"))
    }

    @Test
    fun `rejects empty invalid or oversized input`() {
        assertNull(PhoneAuthHelpers.normalizeToE164("", "9876543210"))
        assertNull(PhoneAuthHelpers.normalizeToE164("91", ""))
        assertNull(PhoneAuthHelpers.normalizeToE164("91", "abc"))
        assertNull(PhoneAuthHelpers.normalizeToE164("0", "9876543210"))
        assertNull(PhoneAuthHelpers.normalizeToE164("1", "123"))                // too short
        assertNull(PhoneAuthHelpers.normalizeToE164("44", "123456789012345"))   // > 15 digits total
        assertNull(PhoneAuthHelpers.normalizeToE164("44", "+1234567890123456")) // > 15 digits
    }

    // ---- input sanitisers -------------------------------------------------------------------

    @Test
    fun `country code input keeps at most three digits`() {
        assertEquals("91", PhoneAuthHelpers.sanitizeCountryCodeInput("+91"))
        assertEquals("1", PhoneAuthHelpers.sanitizeCountryCodeInput("001"))
        assertEquals("971", PhoneAuthHelpers.sanitizeCountryCodeInput("9715"))
        assertEquals("", PhoneAuthHelpers.sanitizeCountryCodeInput("+ab"))
    }

    @Test
    fun `phone input is digits only and capped per country`() {
        assertEquals("9876543210", PhoneAuthHelpers.sanitizePhoneInput("98765 43210 99", "91"))
        assertEquals("98765432109999", PhoneAuthHelpers.sanitizePhoneInput("9876543210999999", "44"))
        assertEquals("", PhoneAuthHelpers.sanitizePhoneInput("abc", "91"))
    }

    @Test
    fun `phone input drops a leading trunk zero so the ten digit cap is not wasted`() {
        assertEquals("9876543210", PhoneAuthHelpers.sanitizePhoneInput("09876543210", "91"))
        assertEquals("0", PhoneAuthHelpers.sanitizePhoneInput("0", "91"))
    }

    @Test
    fun `otp input is digits only and capped at six`() {
        assertEquals("123456", PhoneAuthHelpers.sanitizeOtpInput("1234567"))
        assertEquals("1234", PhoneAuthHelpers.sanitizeOtpInput("12 34"))
        assertEquals("", PhoneAuthHelpers.sanitizeOtpInput("abcdef"))
    }

    // ---- OTP validation -----------------------------------------------------------------------

    @Test
    fun `otp is valid only when exactly six digits`() {
        assertTrue(PhoneAuthHelpers.isValidOtp("123456"))
        assertTrue(PhoneAuthHelpers.isValidOtp("000000"))
        assertFalse(PhoneAuthHelpers.isValidOtp("12345"))
        assertFalse(PhoneAuthHelpers.isValidOtp("1234567"))
        assertFalse(PhoneAuthHelpers.isValidOtp("12345a"))
        assertFalse(PhoneAuthHelpers.isValidOtp(""))
    }

    // ---- display formatting ------------------------------------------------------------------

    @Test
    fun `indian e164 is grouped for display`() {
        assertEquals("+91 98765 43210", PhoneAuthHelpers.formatForDisplay("+919876543210"))
        assertEquals("+14155552671", PhoneAuthHelpers.formatForDisplay("+14155552671"))
    }

    // ---- resend countdown ----------------------------------------------------------------------

    @Test
    fun `countdown starts at full window and reaches zero`() {
        val sentAt = 1_000_000L
        assertEquals(30, PhoneAuthHelpers.resendSecondsRemaining(sentAt, sentAt))
        assertEquals(29, PhoneAuthHelpers.resendSecondsRemaining(sentAt, sentAt + 1_000))
        assertEquals(1, PhoneAuthHelpers.resendSecondsRemaining(sentAt, sentAt + 29_000))
        assertEquals(0, PhoneAuthHelpers.resendSecondsRemaining(sentAt, sentAt + 30_000))
        assertEquals(0, PhoneAuthHelpers.resendSecondsRemaining(sentAt, sentAt + 90_000))
    }

    @Test
    fun `countdown rounds partial seconds up so it never shows zero early`() {
        val sentAt = 0L
        assertEquals(30, PhoneAuthHelpers.resendSecondsRemaining(sentAt, 1))
        assertEquals(29, PhoneAuthHelpers.resendSecondsRemaining(sentAt, 1_500))
        assertEquals(1, PhoneAuthHelpers.resendSecondsRemaining(sentAt, 29_999))
    }

    @Test
    fun `countdown is defensive against clock skew and bad windows`() {
        assertEquals(30, PhoneAuthHelpers.resendSecondsRemaining(sentAtMillis = 5_000, nowMillis = 1_000))
        assertEquals(0, PhoneAuthHelpers.resendSecondsRemaining(0, 0, windowSeconds = 0))
        assertEquals(0, PhoneAuthHelpers.resendSecondsRemaining(0, 0, windowSeconds = -5))
    }

    @Test
    fun `canResend flips exactly at the end of the window`() {
        assertFalse(PhoneAuthHelpers.canResend(0, 29_999))
        assertTrue(PhoneAuthHelpers.canResend(0, 30_000))
    }
}
