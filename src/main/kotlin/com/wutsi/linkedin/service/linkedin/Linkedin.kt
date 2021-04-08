package com.wutsi.linkedin.service.linkedin

import com.echobox.api.linkedin.client.LinkedInClient
import com.echobox.api.linkedin.connection.v2.AssetsConnection
import com.echobox.api.linkedin.types.ContentEntity
import com.echobox.api.linkedin.types.Share
import com.echobox.api.linkedin.types.ShareContent
import com.echobox.api.linkedin.types.ShareText
import com.echobox.api.linkedin.types.assets.RegisterUploadRequestBody
import com.echobox.api.linkedin.types.assets.RelationshipType
import com.echobox.api.linkedin.types.request.ShareRequestBody
import com.echobox.api.linkedin.types.urn.URN
import com.wutsi.linkedin.entity.SecretEntity
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.net.URL
import java.nio.file.Files

@Service
class Linkedin(
    @Autowired private val provider: LinkedinProvider
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(Linkedin::class.java)
    }

    fun postShare(
        title: String,
        text: String,
        url: String,
        pictureUrl: String?,
        includeLink: Boolean,
        secret: SecretEntity
    ): Share {
        val client = provider.getLinkedInClient(secret.accessToken)
        val cnn = provider.createShareConnection(client)
        val body = createBody(title, text, url, pictureUrl, includeLink, secret, client)
        return cnn.postShare(body)
    }

    private fun createBody(
        title: String,
        text: String,
        url: String,
        pictureUrl: String?,
        includeLink: Boolean,
        secret: SecretEntity,
        client: LinkedInClient
    ): ShareRequestBody {
        val body = ShareRequestBody(urn(secret))
        body.content = createContent(title, url, pictureUrl, includeLink, secret, client)
        body.text = createText(text)
        return body
    }

    private fun createText(text: String): ShareText {
        val shareText = ShareText()
        shareText.text = text
        return shareText
    }

    private fun createContent(
        title: String,
        url: String,
        pictureUrl: String?,
        includeLink: Boolean,
        secret: SecretEntity,
        client: LinkedInClient
    ): ShareContent {
        val entities = mutableListOf<ContentEntity>()
        if (includeLink) {
            entities.add(createUrlEntity(url))
        } else if (pictureUrl != null) {
            entities.add(createPictureEntity(pictureUrl, secret, client))
        }

        val content = ShareContent()
        content.contentEntities = entities
        content.title = title
        return content
    }

    private fun createUrlEntity(url: String): ContentEntity {
        val entity = ContentEntity()
        entity.entityLocation = url
        return entity
    }

    private fun createPictureEntity(pictureUrl: String, secret: SecretEntity, client: LinkedInClient): ContentEntity {
        val entity = ContentEntity()
        entity.entity = uploadPicture(pictureUrl, secret, client)
        return entity
    }

    private fun uploadPicture(pictureUrl: String, secret: SecretEntity, client: LinkedInClient): URN {
        val file = downloadPicture(pictureUrl)
        val cnn = AssetsConnection(client)
        val body = createUploadRequestBody(secret, file)
        val result = cnn.uploadImageAsset(body, file.name, file)

        LOGGER.info("Uploaded $file to $result")
        return result
    }

    private fun createUploadRequestBody(secret: SecretEntity, file: File): RegisterUploadRequestBody {
        val registerUploadRequest = RegisterUploadRequestBody.RegisterUploadRequest(urn(secret))
        registerUploadRequest.setRecipes(listOf(RegisterUploadRequestBody.RecipeURN.FEED_SHARE_IMAGE))
        registerUploadRequest.fileSize = FileUtils.sizeOf(file)
        registerUploadRequest.supportedUploadMechanism = listOf(RegisterUploadRequestBody.SupportedUploadMechanism.SINGLE_REQUEST_UPLOAD)
        registerUploadRequest.serviceRelationships =
            listOf(RegisterUploadRequestBody.ServiceRelationships("urn:li:userGeneratedContent", RelationshipType.OWNER))

        return RegisterUploadRequestBody(registerUploadRequest)
    }

    private fun downloadPicture(pictureUrl: String): File {
        val url = URL(pictureUrl)
        val ext = FilenameUtils.getExtension(url.file)
        val file = Files.createTempFile(null, ".$ext").toFile()

        LOGGER.info("Downloading $url to $file")
        FileUtils.copyURLToFile(url, file)
        return file
    }

    private fun urn(secret: SecretEntity): URN = URN("urn:li:person:${secret.linkedinId}")
}
