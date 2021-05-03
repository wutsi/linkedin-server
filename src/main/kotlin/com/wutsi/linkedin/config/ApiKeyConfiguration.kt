package com.wutsi.linkedin.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.security.SecurityApi
import com.wutsi.security.apikey.ApiKeyContext
import com.wutsi.security.apikey.ApiKeyProvider
import com.wutsi.security.apikey.ApiKeyRequestInterceptor
import com.wutsi.stream.EventStream
import com.wutsi.stream.EventSubscription
import com.wutsi.tracing.TracingRequestInterceptor
import org.springframework.beans.factory.`annotation`.Autowired
import org.springframework.beans.factory.`annotation`.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.`annotation`.Bean
import org.springframework.context.`annotation`.Configuration
import org.springframework.core.env.Environment
import kotlin.String

@Configuration
public class ApiKeyConfiguration(
    @Autowired
    private val context: ApplicationContext,
    @Autowired
    private val env: Environment,
    @Autowired
    private val mapper: ObjectMapper,
    @Autowired
    private val tracingRequestInterceptor: TracingRequestInterceptor,
    @Autowired
    private val eventStream: EventStream,
    @Value(value = "\${security.api-key.id}")
    private val apiKeyId: String,
    @Value(value = "\${security.api-key.header}")
    private val apiKeyHeader: String
) {
    @Bean
    public fun apiKeyRequestInterceptor(): ApiKeyRequestInterceptor =
        ApiKeyRequestInterceptor(apiKeyContext())

    @Bean
    public fun apiKeyContext(): ApiKeyContext = com.wutsi.security.apikey.DynamicApiKeyContext(
        headerName = apiKeyHeader,
        apiKeyId = apiKeyId,
        context = context
    )

    @Bean
    public fun apiKeyProvider(): ApiKeyProvider = ApiKeyProvider(securityApi())

    @Bean
    public fun securitySubscription(): EventSubscription =
        EventSubscription(com.wutsi.security.event.SecurityEventStream.NAME, eventStream)

    public fun securityEnvironment(): com.wutsi.security.Environment = if
    (env.acceptsProfiles(org.springframework.core.env.Profiles.of("prod")))
        com.wutsi.security.Environment.PRODUCTION
    else
        com.wutsi.security.Environment.SANDBOX

    @Bean
    public fun securityApi(): SecurityApi = com.wutsi.security.SecurityApiBuilder()
        .build(
            env = securityEnvironment(),
            mapper = mapper,
            interceptors = kotlin.collections.listOf(
                tracingRequestInterceptor,
                apiKeyRequestInterceptor()
            )
        )
}
