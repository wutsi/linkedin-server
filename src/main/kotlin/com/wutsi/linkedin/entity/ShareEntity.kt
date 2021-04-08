package com.wutsi.linkedin.entity

import java.time.OffsetDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "T_SHARE")
data class ShareEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "site_id")
    val siteId: Long = -1,

    @Column(name = "story_id")
    val storyId: Long = -1,

    @Column(name = "linkedin_share_id")
    val linkedinShareId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "secret_fk")
    val secret: SecretEntity = SecretEntity(),

    @Column(name = "share_date_time")
    val shareDateTime: OffsetDateTime = OffsetDateTime.now(),

    val success: Boolean = true,

    @Column(name = "error_code")
    val errorCode: Int? = null,

    @Column(name = "error_description")
    val errorMessage: String? = null,

    @Column(name = "post_id")
    val postId: Long? = null
)
