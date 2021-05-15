package com.wutsi.linkedin.endpoint

import com.wutsi.linkedin.`delegate`.ShareDelegate
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.`annotation`.GetMapping
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.constraints.NotNull
import kotlin.Long

@RestController
public class ShareController(
    private val `delegate`: ShareDelegate
) {
    @GetMapping("/v1/linkedin/share")
    @PreAuthorize(value = "hasAuthority('linkedin')")
    public fun invoke(@RequestParam(name = "story-id", required = true) @NotNull storyId: Long) {
        delegate.invoke(storyId)
    }
}
