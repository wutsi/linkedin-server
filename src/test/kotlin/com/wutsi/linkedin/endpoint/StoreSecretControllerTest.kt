package com.wutsi.linkedin.endpoint

import com.wutsi.linkedin.dao.SecretRepository
import com.wutsi.linkedin.dto.StoreSecretRequest
import com.wutsi.linkedin.dto.StoreSecretResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus.OK
import org.springframework.test.context.jdbc.Sql
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/StoreSecretController.sql"])
internal class StoreSecretControllerTest : ControllerTestBase() {
    @LocalServerPort
    private val port = 0

    @Autowired
    private lateinit var dao: SecretRepository

    @BeforeEach
    override fun setUp() {
        super.setUp()

        login("linkedin")
    }

    @Test
    fun `create secret`() {
        val request = StoreSecretRequest(
            siteId = 1L,
            userId = 11L,
            accessTokenSecret = "secret",
            accessToken = "token",
            linkedinId = "111"
        )

        val url = "http://localhost:$port/v1/linkedin/secrets"
        val response = post(url, request, StoreSecretResponse::class.java)
        assertEquals(OK, response.statusCode)

        val secret = dao.findById(response.body.secretId).get()
        assertEquals(request.userId, secret.userId)
        assertEquals(request.siteId, secret.siteId)
        assertEquals(request.linkedinId, secret.linkedinId)
        assertEquals(request.accessToken, secret.accessToken)
    }

    @Test
    fun `update secret`() {
        val request = StoreSecretRequest(
            siteId = 1L,
            userId = 1L,
            accessTokenSecret = "secret",
            accessToken = "token",
            linkedinId = "222"
        )

        val url = "http://localhost:$port/v1/linkedin/secrets"
        val response = post(url, request, StoreSecretResponse::class.java)
        assertEquals(OK, response.statusCode)

        val secret = dao.findById(response.body.secretId).get()
        assertEquals(request.userId, secret.userId)
        assertEquals(request.linkedinId, secret.linkedinId)
        assertEquals(request.siteId, secret.siteId)
        assertEquals(request.accessToken, secret.accessToken)
    }
}
