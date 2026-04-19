package com.nsnm.herenow.domain.room.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "rooms")
class RoomEntity(

    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "space_id", nullable = false, columnDefinition = "uuid")
    val spaceId: UUID,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, length = 50)
    var icon: String = "Home",

    @Column(nullable = false, length = 50)
    var color: String = "bg-emerald-500",

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)
