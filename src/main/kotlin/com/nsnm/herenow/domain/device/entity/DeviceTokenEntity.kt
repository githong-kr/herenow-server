package com.nsnm.herenow.domain.device.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "device_tokens",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "push_token"])]
)
class DeviceTokenEntity(

    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    val userId: UUID,

    @Column(name = "push_token", nullable = false, length = 300)
    var pushToken: String,

    @Column(length = 10)
    var platform: String = "android",

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)
