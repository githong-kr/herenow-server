package com.nsnm.herenow.domain.storage.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "storages")
class StorageEntity(

    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "room_id", nullable = false, columnDefinition = "uuid")
    val roomId: UUID,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, precision = 5, scale = 2)
    var x: BigDecimal = BigDecimal("10"),

    @Column(nullable = false, precision = 5, scale = 2)
    var y: BigDecimal = BigDecimal("10"),

    @Column(nullable = false, precision = 5, scale = 2)
    var w: BigDecimal = BigDecimal("25"),

    @Column(nullable = false, precision = 5, scale = 2)
    var h: BigDecimal = BigDecimal("25"),

    @Column(nullable = false, length = 50)
    var color: String = "bg-slate-200",

    @Column(name = "top_color", nullable = false, length = 50)
    var topColor: String = "bg-slate-100",

    @Column(length = 20)
    var design: String? = "drawer",

    @Column(name = "grid_rows", nullable = false)
    var gridRows: Int = 3,

    @Column(name = "grid_cols", nullable = false)
    var gridCols: Int = 3,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var layout: String = "{}",

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)
