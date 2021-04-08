package com.wutsi.linkedin.config

import com.wutsi.stream.EventStream
import org.springframework.beans.factory.`annotation`.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.`annotation`.Bean
import org.springframework.context.`annotation`.Configuration

@Configuration
@ConditionalOnProperty(
    value = ["rabbitmq.enabled"],
    havingValue = "false"
)
public class MQueueLocalConfiguration(
    @Autowired
    private val eventPublisher: ApplicationEventPublisher
) {
    @Bean
    public fun eventStream(): EventStream = com.wutsi.stream.file.FileEventStream(
        name = "linkedin",
        root = java.io.File(
            System.getProperty("user.home") + java.io.File.separator + "tmp",
            "mqueue"
        ),
        handler = object : com.wutsi.stream.EventHandler {
            override fun onEvent(event: com.wutsi.stream.Event) {
                eventPublisher.publishEvent(event)
            }
        }
    )
}
