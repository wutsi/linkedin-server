package com.wutsi.linkedin.service.linkedin

import com.echobox.api.linkedin.client.DefaultLinkedInClient
import com.echobox.api.linkedin.client.LinkedInClient
import com.echobox.api.linkedin.connection.v2.ShareConnection
import org.springframework.stereotype.Service

@Service
class LinkedinProvider {
    fun getLinkedInClient(accessToken: String): LinkedInClient =
        DefaultLinkedInClient(accessToken)

    fun createShareConnection(client: LinkedInClient): ShareConnection =
        ShareConnection(client)
}
