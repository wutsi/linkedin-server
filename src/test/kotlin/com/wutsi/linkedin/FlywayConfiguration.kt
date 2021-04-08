package com.wutsi.linkedin

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal class FlywayConfiguration {
    companion object {
        var cleaned: Boolean = false
    }

    @Bean
    fun flywayMigrationStrategy(): FlywayMigrationStrategy {
        return FlywayMigrationStrategy { flyway ->
            if (!cleaned) {
                flyway.clean()
                cleaned = true
            }
            flyway.migrate()
        }
    }
}
