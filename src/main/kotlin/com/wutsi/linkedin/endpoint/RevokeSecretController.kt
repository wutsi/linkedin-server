package com.wutsi.linkedin.endpoint

import com.wutsi.linkedin.`delegate`.RevokeSecretDelegate
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.`annotation`.DeleteMapping
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RestController
import kotlin.Long

@RestController
public class RevokeSecretController(
    private val `delegate`: RevokeSecretDelegate
) {
    @DeleteMapping("/v1/linkedin/secrets/{id}")
    @PreAuthorize(value = "hasAuthority('linkedin')")
    public fun invoke(@PathVariable(name = "id") id: Long) {
        delegate.invoke(id)
    }
}
