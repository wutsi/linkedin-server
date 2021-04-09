package com.wutsi.linkedin.`delegate`

import com.wutsi.linkedin.dao.SecretRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
public class RevokeSecretDelegate(private val dao: SecretRepository) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(RevokeSecretDelegate::class.java)
    }

    public fun invoke(id: Long) {
        val secret = dao.findById(id)
        if (secret.isPresent)
            dao.delete(secret.get())
    }

    fun invoke(userId: Long, siteId: Long) {
        val secret = dao.findByUserIdAndSiteId(userId, siteId)
        if (secret.isPresent) {
            dao.delete(secret.get())
            LOGGER.info("Revoking secret for User#$userId on Site#$siteId")
        }
    }
}
