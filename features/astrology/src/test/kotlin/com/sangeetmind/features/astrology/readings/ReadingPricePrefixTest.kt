package com.sangeetmind.features.astrology.readings

import org.junit.Assert.assertEquals
import org.junit.Test

class ReadingPricePrefixTest {

    @Test
    fun `strips the English dash prefix and capitalises the rest`() {
        assertEquals(
            "A personal career outlook from your kundli's D1/D10 charts. You're only charged if the reading is ready.",
            stripPricePrefix("₹99, or free with Premium — a personal career outlook from your kundli's D1/D10 charts. You're only charged if the reading is ready.")
        )
    }

    @Test
    fun `strips the English full-stop prefix, per-question and small-reading variants`() {
        assertEquals(
            "Spouse nature, relationship strengths and timing.",
            stripPricePrefix("₹99, or free with Premium. Spouse nature, relationship strengths and timing.")
        )
        assertEquals(
            "A direct answer from your 10th house.",
            stripPricePrefix("₹99 per question, or free with Premium. A direct answer from your 10th house.")
        )
        assertEquals(
            "From your Venus, Moon, 5th house and Mars.",
            stripPricePrefix("₹49, or free with Premium. From your Venus, Moon, 5th house and Mars.")
        )
    }

    @Test
    fun `strips the Hindi prefixes`() {
        assertEquals(
            "आपकी कुंडली के D1/D10 चार्ट से बनी करियर रीडिंग।",
            stripPricePrefix("₹99, या प्रीमियम में फ़्री — आपकी कुंडली के D1/D10 चार्ट से बनी करियर रीडिंग।")
        )
        assertEquals(
            "आपके सातवें भाव से जीवनसाथी का स्वभाव।",
            stripPricePrefix("₹99, या प्रीमियम में मुफ़्त। आपके सातवें भाव से जीवनसाथी का स्वभाव।")
        )
        assertEquals(
            "आपके दसवें भाव से सीधा जवाब।",
            stripPricePrefix("हर सवाल ₹99, या प्रीमियम में मुफ़्त। आपके दसवें भाव से सीधा जवाब।")
        )
        assertEquals(
            "आपके शुक्र, चंद्र से।",
            stripPricePrefix("₹49, या प्रीमियम में मुफ़्त। आपके शुक्र, चंद्र से।")
        )
    }

    @Test
    fun `leaves text without a price prefix untouched`() {
        assertEquals("Generate career reading", stripPricePrefix("Generate career reading"))
        assertEquals("", stripPricePrefix(""))
        assertEquals(
            "Your bond with children. Top up ₹99 on Payments.",
            stripPricePrefix("Your bond with children. Top up ₹99 on Payments.")
        )
        assertEquals("₹99, or free with Premium — ", stripPricePrefix("₹99, or free with Premium — "))
    }
}
