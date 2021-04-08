package com.wutsi.linkedin.dao

import com.wutsi.linkedin.entity.ShareEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ShareRepository : CrudRepository<ShareEntity, Long>
