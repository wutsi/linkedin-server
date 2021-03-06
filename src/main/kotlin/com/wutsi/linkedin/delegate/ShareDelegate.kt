package com.wutsi.linkedin.`delegate`

import com.echobox.api.linkedin.exception.LinkedInAPIException
import com.echobox.api.linkedin.types.Share
import com.wutsi.linkedin.dao.SecretRepository
import com.wutsi.linkedin.dao.ShareRepository
import com.wutsi.linkedin.entity.SecretEntity
import com.wutsi.linkedin.entity.ShareEntity
import com.wutsi.linkedin.event.LinkedinEventType
import com.wutsi.linkedin.event.LinkedinSharedEventPayload
import com.wutsi.linkedin.service.bitly.BitlyUrlShortenerFactory
import com.wutsi.linkedin.service.linkedin.Linkedin
import com.wutsi.site.SiteApi
import com.wutsi.site.SiteAttribute
import com.wutsi.site.dto.Site
import com.wutsi.story.StoryApi
import com.wutsi.story.dto.Story
import com.wutsi.stream.EventStream
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
public class ShareDelegate(
    @Autowired private val siteApi: SiteApi,
    @Autowired private val storyApi: StoryApi,
    @Autowired private val shareDao: ShareRepository,
    @Autowired private val secretDao: SecretRepository,
    @Autowired private val bitlyFactory: BitlyUrlShortenerFactory,
    @Autowired private val eventStream: EventStream,
    @Autowired private val linkedin: Linkedin
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ShareDelegate::class.java)
    }

    @Transactional
    public fun invoke(
        storyId: Long,
        message: String? = null,
        pictureUrl: String? = null,
        includeLink: Boolean = true,
        postId: Long? = null
    ) {
        val story = storyApi.get(storyId).story
        val site = siteApi.get(story.siteId).site
        if (!enabled(site)) {
            LOGGER.warn("Site#${story.siteId} doesn't have LinkedIn enabled. Ignoring the request")
            return
        }

        val secret = findSecret(story, site) ?: return
        val share = share(story, secret, site, message, pictureUrl, includeLink, postId)
        if (share != null) {
            eventStream.publish(
                type = LinkedinEventType.SHARED.urn,
                payload = LinkedinSharedEventPayload(
                    linkedinShareId = share.id,
                    postId = postId
                )
            )
        }
    }

    private fun share(
        story: Story,
        secret: SecretEntity,
        site: Site,
        message: String?,
        pictureUrl: String?,
        includeLink: Boolean,
        postId: Long?
    ): Share? {
        var result: Share? = null
        try {
            result = postShare(
                title = story.title,
                text = text(story, site, message, includeLink),
                pictureUrl = pictureUrl,
                includeLink = includeLink,
                url = url(story, site),
                secret = secret
            )
            LOGGER.info("Story#${story.id} shared to Linkedin. shareid=${result.id}")
        } catch (ex: Exception) {
            LOGGER.error("Unable to share the Story#${story.id} to Linkedin", ex)
            save(story, site, secret, ex, postId)
        } finally {
            if (result != null) {
                save(story, site, secret, result, postId)
            }
        }

        return result
    }

    private fun postShare(
        title: String,
        text: String,
        url: String,
        pictureUrl: String?,
        includeLink: Boolean,
        secret: SecretEntity
    ): Share =
        linkedin.postShare(
            title = title,
            text = text,
            pictureUrl = pictureUrl,
            includeLink = includeLink,
            url = url,
            secret = secret
        )

    private fun save(
        story: Story,
        site: Site,
        secret: SecretEntity,
        ex: Exception,
        postId: Long?
    ) {
        try {
            shareDao.save(
                ShareEntity(
                    storyId = story.id,
                    siteId = site.id,
                    secret = secret,
                    postId = postId,
                    linkedinShareId = null,
                    success = false,
                    errorCode = if (ex is LinkedInAPIException) ex.httpStatusCode else -1,
                    errorMessage = ex.message
                )
            )
        } catch (ex: Exception) {
            LOGGER.warn("Unable to store the share information", ex)
        }
    }

    private fun save(
        story: Story,
        site: Site,
        secret: SecretEntity,
        share: Share,
        postId: Long?
    ) {
        try {
            shareDao.save(
                ShareEntity(
                    storyId = story.id,
                    siteId = site.id,
                    secret = secret,
                    linkedinShareId = share.id,
                    postId = postId,
                    success = true
                )
            )
        } catch (ex: Exception) {
            LOGGER.warn("Unable to store the share information", ex)
        }
    }

    private fun text(
        story: Story,
        site: Site,
        message: String?,
        includeLink: Boolean
    ): String {
        val text = if (!message.isNullOrEmpty())
            message
        else if (!story.socialMediaMessage.isNullOrEmpty())
            story.socialMediaMessage
        else
            story.summary

        val url = if (includeLink)
            url(story, site)
        else
            ""

        return "$text $url".trim()
    }

    private fun url(story: Story, site: Site): String =
        bitlyFactory.get(site).shorten("${site.websiteUrl}${story.slug}?utm_source=linkedin")

    private fun findSecret(story: Story, site: Site): SecretEntity? {
        val opt = secretDao.findByUserIdAndSiteId(story.userId, site.id)
        if (opt.isPresent)
            return opt.get()

        LOGGER.warn("User#${story.userId} doesn't have a Linkedin secrets.")
        return null
    }

    private fun enabled(site: Site): Boolean =
        site.attributes.find { SiteAttribute.LINKEDIN_ENABLED.urn == it.urn }?.value == "true"
}
