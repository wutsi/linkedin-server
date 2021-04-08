package com.wutsi.linkedin.endpoint

import com.wutsi.linkedin.`delegate`.ShareDelegate
import org.springframework.web.bind.`annotation`.GetMapping
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.bind.`annotation`.RestController
import kotlin.Long

@RestController
public class ShareController(
    private val `delegate`: ShareDelegate
) {
    @GetMapping("/v1/linkedin/share")
    public fun invoke(@RequestParam(name = "story-id", required = false) storyId: Long) {
        delegate.invoke(storyId)
    }
}
