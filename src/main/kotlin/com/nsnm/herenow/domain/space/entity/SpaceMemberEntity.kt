package com.nsnm.herenow.domain.space.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "space_members",
    uniqueConstraints = [UniqueConstraint(columnNames = ["space_id", "user_id"])]
)
class SpaceMemberEntity(

    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "space_id", nullable = false, columnDefinition = "uuid")
    val spaceId: UUID,

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    val userId: UUID,

    @Column(nullable = false, length = 10)
    var role: String = "MEMBER",

    @Column(name = "joined_at", nullable = false, updatable = false)
    val joinedAt: OffsetDateTime = OffsetDateTime.now()
)
