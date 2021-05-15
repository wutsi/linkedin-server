package com.wutsi.linkedin.config

import com.wutsi.platform.security.apikey.ApiKeyAuthenticationProvider
import com.wutsi.platform.security.apikey.ApiKeyProvider
import org.springframework.beans.factory.`annotation`.Autowired
import org.springframework.context.`annotation`.Configuration
import org.springframework.security.config.`annotation`.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.`annotation`.web.builders.HttpSecurity
import org.springframework.security.config.`annotation`.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.util.matcher.RequestMatcher
import javax.servlet.Filter

@Configuration
public class SecurityConfiguration(
    @Autowired
    private val apiKeyProvider: ApiKeyProvider
) : WebSecurityConfigurerAdapter() {
    public override fun configure(http: HttpSecurity) {
        http
            .csrf()
            .disable()
            .sessionManagement()
            .sessionCreationPolicy(
                org.springframework.security.config.http.SessionCreationPolicy.STATELESS
            )
            .and()
            .authorizeRequests()
            .requestMatchers(SECURED_ENDPOINTS).authenticated()
            .anyRequest().permitAll()
            .and()
            .addFilterBefore(
                authenticationFilter(),
                org.springframework.security.web.authentication.AnonymousAuthenticationFilter::class.java
            )
    }

    public override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(apiKeyAuthenticationProvider())
    }

    private fun apiKeyAuthenticationProvider(): ApiKeyAuthenticationProvider =
        ApiKeyAuthenticationProvider()

    public fun authenticationFilter(): Filter {
        val filter = com.wutsi.platform.security.apikey.ApiKeyAuthenticationFilter(
            apiProvider = apiKeyProvider,
            requestMatcher = SECURED_ENDPOINTS
        )
        filter.setAuthenticationManager(authenticationManagerBean())
        return filter
    }

    public companion object {
        public val SECURED_ENDPOINTS: RequestMatcher =
            org.springframework.security.web.util.matcher.OrRequestMatcher(
                org.springframework.security.web.util.matcher.AntPathRequestMatcher("/v1/linkedin/share", "GET"),
                org.springframework.security.web.util.matcher.AntPathRequestMatcher("/v1/linkedin/secrets", "POST"),
                org.springframework.security.web.util.matcher.AntPathRequestMatcher("/v1/linkedin/secrets/*", "DELETE")
            )
    }
}
