package com.wutsi.linkedin

import com.wutsi.platform.EnableWutsiCore
import com.wutsi.platform.EnableWutsiSecurity
import com.wutsi.platform.EnableWutsiSite
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableAsync
@EnableTransactionManagement
@EnableScheduling
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWutsiCore
@EnableWutsiSite
@EnableWutsiSecurity
public class Application

public fun main(vararg args: String) {
    org.springframework.boot.runApplication<Application>(*args)
}
