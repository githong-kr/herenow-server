package com.nsnm.herenow.domain.space.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "space_join_requests")
class SpaceJoinRequestEntity(

    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "space_id", nullable = false, columnDefinition = "uuid")
    val spaceId: UUID,

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    val userId: UUID,

    @Column(name = "invite_code_used", length = 10)
    val inviteCodeUsed: String? = null,

    @Column(nullable = false, length = 10)
    var status: String = "PENDING",

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)
