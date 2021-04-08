package com.wutsi.linkedin.endpoint

import com.wutsi.linkedin.dao.SecretRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.RestTemplate
import kotlin.test.assertFalse

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/RevokeSecretController.sql"])
internal class RevokeSecretControllerTest {
    @LocalServerPort
    private val port = 0

    private lateinit var url: String

    private val rest: RestTemplate = RestTemplate()

    @Autowired
    private lateinit var dao: SecretRepository

    @BeforeEach
    fun setUp() {
        url = "http://localhost:$port/v1/linkedin/secrets/{id}"
    }

    @Test
    operator fun invoke() {
        rest.delete(url, "1")

        val secret = dao.findById(1)
        assertFalse(secret.isPresent)
    }
}
