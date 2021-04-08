package com.wutsi.linkedin.service.bitly

import com.wutsi.linkedin.AttributeUrn
import com.wutsi.site.dto.Attribute
import com.wutsi.site.dto.Site
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class BitlyUrlShortenerTest {
    private val shortener = BitlyUrlShortener()
    private val url = "https://www.google.ca"

    @Test
    fun `shorten url`() {
        val site = Site(
            attributes = listOf(
                Attribute(AttributeUrn.BITLY_ACCESS_TOKEN.urn, "7c6a88dd1ca7633b0d5e15336184848e0ec5d22c")
            )
        )

        val result = shortener.shorten(url, site)
        assertEquals("https://bit.ly/2KPbcAE", result)
    }

    @Test
    fun `do not shorten URL with invalid token`() {
        val site = Site(
            attributes = listOf(
                Attribute(AttributeUrn.BITLY_ACCESS_TOKEN.urn, "xxx")
            )
        )

        val result = shortener.shorten(url, site)
        assertEquals(url, result)
    }
}
