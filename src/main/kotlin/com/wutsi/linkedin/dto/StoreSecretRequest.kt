package com.wutsi.linkedin.dto

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import kotlin.Long
import kotlin.String

public data class StoreSecretRequest(
    @get:NotNull
    public val siteId: Long = 0,
    @get:NotNull
    public val userId: Long = 0,
    @get:NotBlank
    public val linkedinId: String = "",
    @get:NotBlank
    public val accessToken: String = "",
    @get:NotBlank
    public val accessTokenSecret: String = ""
)
