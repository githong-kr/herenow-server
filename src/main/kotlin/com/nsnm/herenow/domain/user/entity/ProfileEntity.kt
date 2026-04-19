package com.nsnm.herenow.domain.user.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "profiles")
class ProfileEntity(

    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID,

    @Column(length = 100)
    var name: String? = null,

    @Column(name = "avatar_url")
    var avatarUrl: String? = null,

    @Column(name = "default_space_id", columnDefinition = "uuid")
    var defaultSpaceId: UUID? = null,

    @Column(name = "marketing_consent", nullable = false)
    var marketingConsent: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)
