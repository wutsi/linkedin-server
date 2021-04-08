package com.wutsi.linkedin.servlet

import org.springframework.stereotype.Component
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

@Component
public class CorsFilter : Filter {
    public override fun doFilter(
        req: ServletRequest,
        resp: ServletResponse,
        chain: FilterChain
    ) {
        (resp as javax.servlet.http.HttpServletResponse).addHeader(
            "Access-Control-Allow-Origin",
            "*"
        )
        resp.addHeader(
            "Access-Control-Allow-Methods",
            "GET, OPTIONS, HEAD, PUT, POST, DELETE"
        )
        resp.addHeader(
            "Access-Control-Allow-Headers",
            "Content-Type, Authorization, Content-Length,X-Requested-With"
        )
        chain.doFilter(req, resp)
    }
}
