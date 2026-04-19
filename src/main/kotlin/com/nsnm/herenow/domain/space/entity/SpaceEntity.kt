package com.nsnm.herenow.domain.space.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "spaces")
class SpaceEntity(

    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(name = "invite_code", unique = true, length = 10)
    var inviteCode: String? = null,

    @Column(name = "owner_id", nullable = false, columnDefinition = "uuid")
    var ownerId: UUID,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)
