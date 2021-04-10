package com.wutsi.linkedin.servlet

import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MDCFilter : Filter {
    public override fun doFilter(
        req: ServletRequest,
        resp: ServletResponse,
        chain: FilterChain
    ) {
        val requestId = (req as HttpServletRequest).getHeader("X-Request-ID")
        if (requestId != null)
            MDC.put("request_id", requestId.toString())

        chain.doFilter(req, resp)
    }
}
