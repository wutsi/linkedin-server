package com.wutsi.linkedin.config

import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.wutsi.stream.EventStream
import com.wutsi.tracing.TracingContext
import org.springframework.beans.factory.`annotation`.Autowired
import org.springframework.beans.factory.`annotation`.Value
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.`annotation`.Bean
import org.springframework.context.`annotation`.Configuration
import org.springframework.scheduling.`annotation`.Scheduled
import java.util.concurrent.ExecutorService
import kotlin.Int
import kotlin.Long
import kotlin.String

@Configuration
@ConditionalOnProperty(
    value = ["rabbitmq.enabled"],
    havingValue = "true"
)
public class MQueueRemoteConfiguration(
    @Autowired
    private val tracingContext: TracingContext,
    @Autowired
    private val eventPublisher: ApplicationEventPublisher,
    @Value(value = "\${rabbitmq.url}")
    private val url: String,
    @Value(value = "\${rabbitmq.thread-pool-size}")
    private val threadPoolSize: Int,
    @Value(value = "\${rabbitmq.max-retries}")
    private val maxRetries: Int,
    @Value(value = "\${rabbitmq.queue-ttl-seconds}")
    private val queueTtlSeconds: Long
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
    public fun eventStream(): EventStream = com.wutsi.stream.rabbitmq.RabbitMQEventStream(
        name = "linkedin",
        channel = channel(),
        queueTtlSeconds = queueTtlSeconds,
        maxRetries = maxRetries,
        handler = object : com.wutsi.stream.EventHandler {
            override fun onEvent(event: com.wutsi.stream.Event) {
                com.wutsi.tracing.TracingMDCHelper.initMDC(tracingContext)
                eventPublisher.publishEvent(event)
            }
        }
    )

    @Bean
    public fun rabbitMQHealthIndicator(): HealthIndicator =
        com.wutsi.stream.rabbitmq.RabbitMQHealthIndicator(channel())

    @Scheduled(cron = "\${rabbitmq.replay-cron}")
    public fun replayDlq() {
        (eventStream() as com.wutsi.stream.rabbitmq.RabbitMQEventStream).replayDlq()
    }
}
