package com.wutsi.linkedin.endpoint

import com.wutsi.linkedin.`delegate`.StoreSecretDelegate
import com.wutsi.linkedin.dto.StoreSecretRequest
import com.wutsi.linkedin.dto.StoreSecretResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid

@RestController
public class StoreSecretController(
    private val `delegate`: StoreSecretDelegate
) {
    @PostMapping("/v1/linkedin/secrets")
    @PreAuthorize(value = "hasAuthority('linkedin')")
    public fun invoke(@Valid @RequestBody request: StoreSecretRequest): StoreSecretResponse =
        delegate.invoke(request)
}
