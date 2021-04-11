package com.wutsi.linkedin.config

import com.wutsi.tracing.RequestTracingContext
import com.wutsi.tracing.TracingContextProvider
import feign.RequestInterceptor
import org.springframework.beans.factory.`annotation`.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.`annotation`.Bean
import org.springframework.context.`annotation`.Configuration
import javax.servlet.Filter
import javax.servlet.http.HttpServletRequest

@Configuration
public class TracingConfiguration(
    @Autowired
    private val request: HttpServletRequest,
    @Autowired
    private val context: ApplicationContext
) {
    @Bean
    public fun tracingFilter(): Filter = com.wutsi.tracing.TracingFilter(tracingContextProvider())

    @Bean
    public fun requestTracingContext(): RequestTracingContext = RequestTracingContext(request)

    @Bean
    public fun tracingContextProvider(): TracingContextProvider = TracingContextProvider(context)

    @Bean
    public fun tracingRequestInterceptor(): RequestInterceptor =
        com.wutsi.tracing.TracingRequestInterceptor("linkedin-server", tracingContextProvider())
}
