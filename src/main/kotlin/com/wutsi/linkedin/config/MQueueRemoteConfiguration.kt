package com.wutsi.linkedin.config

import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.wutsi.stream.rabbitmq.RabbitMQEventStream
import org.springframework.beans.factory.`annotation`.Autowired
import org.springframework.beans.factory.`annotation`.Value
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.`annotation`.Bean
import org.springframework.context.`annotation`.Configuration
import java.util.concurrent.ExecutorService
import kotlin.Int
import kotlin.String

@Configuration
@ConditionalOnProperty(
    value = ["rabbitmq.enabled"],
    havingValue = "true"
)
public class MQueueRemoteConfiguration(
    @Autowired
    private val eventPublisher: ApplicationEventPublisher,
    @Value(value = "\${rabbitmq.url}")
    private val url: String,
    @Value(value = "\${rabbitmq.thread-pool-size}")
    private val threadPoolSize: Int
) {
    @Bean
    public fun connectionFactory(): ConnectionFactory {
        val factory = ConnectionFactory()
        factory.setUri(url)
        return factory
    }

    @Bean(destroyMethod = "shutdown")
    public fun executorService(): ExecutorService =
        java.util.concurrent.Executors.newFixedThreadPool(threadPoolSize)

    @Bean(destroyMethod = "close")
    public fun channel(): Channel = connectionFactory()
        .newConnection(executorService())
        .createChannel()

    @Bean(destroyMethod = "close")
    public fun eventStream(): RabbitMQEventStream = com.wutsi.stream.rabbitmq.RabbitMQEventStream(
        name = "linkedin",
        channel = channel(),
        handler = object : com.wutsi.stream.EventHandler {
            override fun onEvent(event: com.wutsi.stream.Event) {
                eventPublisher.publishEvent(event)
            }
        }
    )

    @Bean
    public fun rabbitMQHealthIndicator(): HealthIndicator =
        com.wutsi.stream.rabbitmq.RabbitMQHealthIndicator(channel())
}
