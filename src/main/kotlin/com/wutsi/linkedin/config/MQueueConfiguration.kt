package com.wutsi.linkedin.config

import com.wutsi.channel.event.ChannelEventStream
import com.wutsi.post.event.PostEventStream
import com.wutsi.story.event.StoryEventStream
import com.wutsi.stream.EventStream
import com.wutsi.stream.EventSubscription
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MQueueConfiguration(
    @Autowired private val stream: EventStream
) {
    @Bean
    fun storySubscription() = EventSubscription(StoryEventStream.NAME, stream)

    @Bean
    fun channelSubscription() = EventSubscription(ChannelEventStream.NAME, stream)

    @Bean
    fun postSubscription() = EventSubscription(PostEventStream.NAME, stream)
}
