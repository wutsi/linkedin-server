package com.wutsi.linkedin.service.bitly

import com.wutsi.bitly.BitlyUrlShortener
import com.wutsi.bitly.DefaultBitlyUrlShortener
import com.wutsi.bitly.NullBitlyUrlShortener
import com.wutsi.linkedin.AttributeUrn
import com.wutsi.site.dto.Site
import org.springframework.stereotype.Service

@Service
class BitlyUrlShortenerFactory {
    fun get(site: Site): BitlyUrlShortener {
        val accessToken = accessToken(site)
        return if (accessToken == null)
            NullBitlyUrlShortener()
        else
            DefaultBitlyUrlShortener(accessToken)
    }

    private fun accessToken(site: Site): String? =
        site.attributes.find { it.urn == AttributeUrn.BITLY_ACCESS_TOKEN.urn }?.value
}
