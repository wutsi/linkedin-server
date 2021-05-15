package com.wutsi.linkedin.service.bitly

import com.wutsi.bitly.DefaultBitlyUrlShortener
import com.wutsi.bitly.NullBitlyUrlShortener
import com.wutsi.site.SiteAttribute
import com.wutsi.site.dto.Attribute
import com.wutsi.site.dto.Site
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class BitlyUrlShortenerFactoryTest {
    @Test
    fun `return DefaultBitlyUrlShortener when access-token configure`() {
        val site = Site(
            attributes = listOf(Attribute(SiteAttribute.BITLY_ACCESS_TOKEN.urn, "xxx"))
        )
        val shortener = BitlyUrlShortenerFactory().get(site)

        assertTrue(shortener is DefaultBitlyUrlShortener)
    }

    @Test
    fun `return NullBitlyUrlShortener when no access-token`() {
        val site = Site()
        val shortener = BitlyUrlShortenerFactory().get(site)

        assertTrue(shortener is NullBitlyUrlShortener)
    }
}
