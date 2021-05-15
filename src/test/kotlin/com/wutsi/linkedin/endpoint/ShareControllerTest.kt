package com.wutsi.linkedin.endpoint

import com.echobox.api.linkedin.exception.LinkedInAPIException
import com.echobox.api.linkedin.types.Share
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.bitly.BitlyUrlShortener
import com.wutsi.linkedin.SiteAttribute
import com.wutsi.linkedin.dao.ShareRepository
import com.wutsi.linkedin.event.LinkedinEventType.SHARED
import com.wutsi.linkedin.event.LinkedinSharedEventPayload
import com.wutsi.linkedin.service.bitly.BitlyUrlShortenerFactory
import com.wutsi.linkedin.service.linkedin.Linkedin
import com.wutsi.site.SiteApi
import com.wutsi.site.dto.Attribute
import com.wutsi.site.dto.GetSiteResponse
import com.wutsi.site.dto.Site
import com.wutsi.story.StoryApi
import com.wutsi.story.dto.GetStoryResponse
import com.wutsi.story.dto.Story
import com.wutsi.stream.EventStream
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.jdbc.Sql
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/ShareController.sql"])
internal class ShareControllerTest : ControllerTestBase() {
    @LocalServerPort
    private val port = 0

    @Autowired
    private lateinit var dao: ShareRepository

    @MockBean
    private lateinit var siteApi: SiteApi

    @MockBean
    private lateinit var storyApi: StoryApi

    @MockBean
    private lateinit var linkedin: Linkedin

    @MockBean
    private lateinit var bitlyFactory: BitlyUrlShortenerFactory

    @MockBean
    private lateinit var eventStream: EventStream

    private val shortenUrl = "https://bit.ly/123"

    @BeforeEach
    override fun setUp() {
        super.setUp()

        login("linkedin")

        val bitly = mock<BitlyUrlShortener>()
        doReturn(shortenUrl).whenever(bitly).shorten(any())
        doReturn(bitly).whenever(bitlyFactory).get(any())
    }

    @Test
    @Sql(value = ["/db/clean.sql", "/db/ShareController.sql"])
    fun `save message to DB when sharing story`() {
        val site = createSite()
        doReturn(GetSiteResponse(site)).whenever(siteApi).get(1L)

        val story = createStory()
        doReturn(GetStoryResponse(story)).whenever(storyApi).get(123L)

        val lkshare = createShare(11L)
        doReturn(lkshare).whenever(linkedin).postShare(
            eq(story.title),
            eq("${story.socialMediaMessage!!} $shortenUrl"),
            eq(shortenUrl),
            eq(null),
            eq(true),
            any()
        )

        val url = "http://127.0.0.1:$port/v1/linkedin/share?story-id=123"
        get(url, Any::class.java)

        val shares = dao.findAll().toList()[0]
        assertEquals(lkshare.id, shares.linkedinShareId)
        assertEquals(story.id, shares.storyId)
        assertEquals(site.id, shares.siteId)
        assertTrue(shares.success)
        assertNull(shares.errorCode)
        assertNull(shares.errorMessage)
    }

    @Test
    @Sql(value = ["/db/clean.sql", "/db/ShareController.sql"])
    fun `save error to DB when sharing story fails`() {
        val site = createSite()
        doReturn(GetSiteResponse(site)).whenever(siteApi).get(1L)

        val story = createStory()
        doReturn(GetStoryResponse(story)).whenever(storyApi).get(123L)

        val ex = mock<LinkedInAPIException>()
        doReturn("Failed !!!").whenever(ex).message
        doReturn(666).whenever(ex).httpStatusCode
        doThrow(ex).whenever(linkedin).postShare(
            eq(story.title),
            eq("${story.socialMediaMessage!!} $shortenUrl"),
            eq(shortenUrl),
            eq(null),
            eq(true),
            any()
        )

        val url = "http://127.0.0.1:$port/v1/linkedin/share?story-id=123"
        get(url, Any::class.java)

        val shares = dao.findAll().toList()[0]
        assertNull(shares.linkedinShareId)
        assertEquals(story.id, shares.storyId)
        assertEquals(site.id, shares.siteId)
        assertFalse(shares.success)
        assertEquals(666, shares.errorCode)
        assertEquals("Failed !!!", shares.errorMessage)
    }

    @Test
    fun `share when story sharing story with summary`() {
        val site = createSite()
        doReturn(GetSiteResponse(site)).whenever(siteApi).get(1L)

        val story = createStory(socialMediaMessage = null)
        doReturn(GetStoryResponse(story)).whenever(storyApi).get(123L)

        val url = "http://127.0.0.1:$port/v1/linkedin/share?story-id=123"
        get(url, Any::class.java)

        verify(linkedin).postShare(
            eq(story.title),
            eq("${story.summary} $shortenUrl"),
            eq(shortenUrl),
            eq(null),
            eq(true),
            any()
        )
    }

    @Test
    fun `do not share when flag not enabled`() {
        val site = createSite(
            attributes = listOf()
        )
        doReturn(GetSiteResponse(site)).whenever(siteApi).get(1L)

        val story = createStory()
        doReturn(GetStoryResponse(story)).whenever(storyApi).get(123L)

        val url = "http://127.0.0.1:$port/v1/linkedin/share?story-id=123"
        get(url, Any::class.java)

        verify(linkedin, never()).postShare(
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
        )
    }

    @Test
    fun `event send when sharing a story`() {
        val site = createSite()
        doReturn(GetSiteResponse(site)).whenever(siteApi).get(1L)

        val story = createStory()
        doReturn(GetStoryResponse(story)).whenever(storyApi).get(123L)

        val lkshare = createShare(11L)
        doReturn(lkshare).whenever(linkedin).postShare(
            eq(story.title),
            eq("${story.socialMediaMessage!!} $shortenUrl"),
            eq(shortenUrl),
            eq(null),
            eq(true),
            any()
        )

        val url = "http://127.0.0.1:$port/v1/linkedin/share?story-id=123"
        get(url, Any::class.java)

        val payload = argumentCaptor<LinkedinSharedEventPayload>()
        verify(eventStream).publish(eq(SHARED.urn), payload.capture())
        assertEquals(11L, payload.firstValue.linkedinShareId)
        assertNull(payload.firstValue.postId)
    }

    private fun createStory(
        userId: Long = 1L,
        socialMediaMessage: String? = "This is nice"
    ) = Story(
        id = 123L,
        title = "This is a story title",
        slug = "/read/123/this-is-a-story-title",
        summary = "Yo man",
        socialMediaMessage = socialMediaMessage,
        userId = userId
    )

    private fun createSite(
        attributes: List<Attribute> = listOf(
            Attribute(SiteAttribute.LINKEDIN_ENABLED.urn, "true")
        )
    ) = Site(
        id = 1L,
        domainName = "www.wutsi.com",
        websiteUrl = "https://www.wutsi.com",
        attributes = attributes
    )

    private fun createShare(id: Long): Share {
        val share = mock<Share>()
        doReturn(id).whenever(share).id
        return share
    }
}
