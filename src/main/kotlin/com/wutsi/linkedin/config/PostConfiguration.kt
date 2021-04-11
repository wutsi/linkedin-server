package com.wutsi.linkedin.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.post.PostApi
import com.wutsi.post.PostApiBuilder
import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles

@Configuration
public class PostConfiguration(
    @Autowired private val env: Environment,
    @Autowired private val mapper: ObjectMapper,
    @Autowired private val tracingRequestInterceptor: RequestInterceptor
) {
    @Bean
    fun postApi(): PostApi =
        PostApiBuilder()
            .build(
                env = postEnvironment(),
                mapper = mapper,
                interceptors = listOf(tracingRequestInterceptor)
            )

    fun postEnvironment(): com.wutsi.post.Environment =
        if (env.acceptsProfiles(Profiles.of("prod")))
            com.wutsi.post.Environment.PRODUCTION
        else
            com.wutsi.post.Environment.SANDBOX
}
