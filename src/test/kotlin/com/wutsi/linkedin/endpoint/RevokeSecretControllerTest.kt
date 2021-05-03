package com.wutsi.linkedin.endpoint

import com.wutsi.linkedin.dao.SecretRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.jdbc.Sql
import kotlin.test.assertFalse

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/RevokeSecretController.sql"])
internal class RevokeSecretControllerTest : ControllerTestBase() {
    @LocalServerPort
    private val port = 0

    @Autowired
    private lateinit var dao: SecretRepository

    @Test
    operator fun invoke() {
        super.login("linkedin")

        val url = "http://localhost:$port/v1/linkedin/secrets/1"
        delete(url)

        val secret = dao.findById(1)
        assertFalse(secret.isPresent)
    }
}
