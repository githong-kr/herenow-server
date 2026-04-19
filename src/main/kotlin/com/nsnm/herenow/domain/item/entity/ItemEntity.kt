package com.nsnm.herenow.domain.item.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "items")
class ItemEntity(

    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "space_id", nullable = false, columnDefinition = "uuid")
    val spaceId: UUID,

    @Column(name = "storage_id", columnDefinition = "uuid")
    var storageId: UUID? = null,

    @Column(name = "row_pos")
    var rowPos: Int? = null,

    @Column(name = "col_pos")
    var colPos: Int? = null,

    @Column(nullable = false, length = 200)
    var name: String,

    @Column(nullable = false, length = 10)
    var icon: String = "📦",

    @Column(name = "photo_url")
    var photoUrl: String? = null,

    @Column(name = "category_id", length = 50)
    var categoryId: String? = null,

    @Column(nullable = false)
    var quantity: Int = 1,

    @Column(name = "min_quantity", nullable = false)
    var minQuantity: Int = 0,

    @Column(name = "expiry_date")
    var expiryDate: LocalDate? = null,

    @Column(columnDefinition = "text")
    var memo: String? = "",

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var tags: String = "[]",

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)
