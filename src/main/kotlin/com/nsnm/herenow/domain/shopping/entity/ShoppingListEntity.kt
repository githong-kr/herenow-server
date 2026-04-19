package com.nsnm.herenow.domain.shopping.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "shopping_list")
class ShoppingListEntity(

    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "space_id", nullable = false, columnDefinition = "uuid")
    val spaceId: UUID,

    @Column(nullable = false, length = 200)
    var name: String,

    @Column(nullable = false)
    var checked: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)
