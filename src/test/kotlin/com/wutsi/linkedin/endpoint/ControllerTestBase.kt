package com.wutsi.linkedin.endpoint

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.platform.security.apikey.ApiKeyContext
import com.wutsi.security.SecurityApi
import com.wutsi.security.dto.ApiKey
import com.wutsi.security.dto.GetApiKeyResponse
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.util.UUID

open class ControllerTestBase {
    @MockBean
    private lateinit var securityApi: SecurityApi

    @MockBean
    private lateinit var context: ApiKeyContext

    @BeforeEach
    open fun setUp() {
        doReturn(null).whenever(context).id()
    }

    protected fun login(scope: String? = null): ApiKey {
        val apiKeyId = UUID.randomUUID().toString()
        doReturn(apiKeyId).whenever(context).id()

        val apiKey = ApiKey(
            id = "api-key",
            name = "test",
            scopes = scope?.let { listOf(it) } ?: emptyList()
        )
        doReturn(GetApiKeyResponse(apiKey)).whenever(securityApi).get(any())
        return apiKey
    }

    protected fun <T> get(url: String, type: Class<T>): ResponseEntity<T> {
        return exchange(url, GET, "", type)
    }

    protected fun delete(url: String) {
        exchange(url, DELETE, "", Any::class.java)
    }

    protected fun <T> post(url: String, body: Any, type: Class<T>): ResponseEntity<T> {
        return exchange(url, POST, body, type)
    }

    protected fun <T> exchange(url: String, method: HttpMethod, body: Any, type: Class<T>): ResponseEntity<T> {
        val headers = HttpHeaders()
        val apiKey = context.id()
        if (apiKey != null)
            headers.put("Authorization", listOf(apiKey))

        val request = HttpEntity(body, headers)
        return RestTemplate().exchange(url, method, request, type)
    }
}
