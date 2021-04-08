package com.wutsi.linkedin.event

import com.wutsi.channel.event.ChannelEventType
import com.wutsi.channel.event.ChannelSecretRevokedEventPayload
import com.wutsi.channel.event.ChannelSecretSubmittedEventPayload
import com.wutsi.linkedin.delegate.RevokeSecretDelegate
import com.wutsi.linkedin.delegate.ShareDelegate
import com.wutsi.linkedin.delegate.StoreSecretDelegate
import com.wutsi.linkedin.dto.StoreSecretRequest
import com.wutsi.post.PostApi
import com.wutsi.post.event.PostEventPayload
import com.wutsi.post.event.PostEventType
import com.wutsi.story.event.StoryEventPayload
import com.wutsi.story.event.StoryEventType
import com.wutsi.stream.Event
import com.wutsi.stream.ObjectMapperBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class EventHandler(
    @Autowired private val shareDelegate: ShareDelegate,
    @Autowired private val storeSecretDelegate: StoreSecretDelegate,
    @Autowired private val revokeSecretDelegate: RevokeSecretDelegate,
    @Autowired private val postApi: PostApi
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(EventHandler::class.java)
        private val CHANNEL_TYPE = "linkedin"
    }

    @EventListener
    fun onEvent(event: Event) {
        LOGGER.info("onEvent($event)")

        if (event.type == StoryEventType.PUBLISHED.urn) {
            onStoryPublished(event)
        } else if (event.type == ChannelEventType.SECRET_SUBMITTED.urn) {
            onSecretSubmitted(event)
        } else if (event.type == ChannelEventType.SECRET_REVOKED.urn) {
            onSecretRevoked(event)
        } else if (event.type == PostEventType.SUBMITTED.urn) {
            onPostSubmitted(event)
        }
    }

    private fun onStoryPublished(event: Event) {
        val payload = ObjectMapperBuilder().build().readValue(event.payload, StoryEventPayload::class.java)
        shareDelegate.invoke(payload.storyId)
    }

    private fun onSecretSubmitted(event: Event) {
        val payload = ObjectMapperBuilder().build().readValue(event.payload, ChannelSecretSubmittedEventPayload::class.java)
        if (payload.type == "twitter") {
            storeSecretDelegate.invoke(
                request = StoreSecretRequest(
                    userId = payload.userId,
                    siteId = payload.siteId,
                    linkedinId = payload.channelUserId,
                    accessToken = payload.accessToken,
                    accessTokenSecret = payload.accessTokenSecret
                )
            )
        }
    }

    private fun onSecretRevoked(event: Event) {
        val payload = ObjectMapperBuilder().build().readValue(event.payload, ChannelSecretRevokedEventPayload::class.java)
        if (payload.type == CHANNEL_TYPE) {
            revokeSecretDelegate.invoke(
                userId = payload.userId,
                siteId = payload.siteId
            )
        }
    }

    private fun onPostSubmitted(event: Event) {
        val payload = ObjectMapperBuilder().build().readValue(event.payload, PostEventPayload::class.java)
        val response = postApi.get(payload.postId)
        val post = response.post
        if (post.channelType == CHANNEL_TYPE) {
            shareDelegate.invoke(
                storyId = post.storyId,
                message = post.message,
                pictureUrl = post.pictureUrl,
                includeLink = post.includeLink,
                postId = post.id
            )
        }
    }
}
