package com.wutsi.linkedin.config

import com.wutsi.tracing.TracingContext
import com.wutsi.tracing.TracingRequestInterceptor
import org.springframework.beans.factory.`annotation`.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.`annotation`.Bean
import org.springframework.context.`annotation`.Configuration
import javax.servlet.Filter

@Configuration
public class TracingConfiguration(
    @Autowired
    private val context: ApplicationContext
) {
    @Bean
    public fun tracingFilter(): Filter = com.wutsi.tracing.TracingFilter(tracingContext())

    @Bean
    public fun tracingContext(): TracingContext = com.wutsi.tracing.DynamicTracingContext(context)

    @Bean
    public fun tracingRequestInterceptor(): TracingRequestInterceptor =
        TracingRequestInterceptor("linkedin-server", tracingContext())
}
