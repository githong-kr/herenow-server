package com.nsnm.herenow.lib.model.entity

import com.nsnm.herenow.lib.model.entity.base.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "device_token")
class DeviceTokenEntity(
    @Id
    @Column(name = "token_id")
    var tokenId: String,

    @Column(name = "profile_id", nullable = false)
    var profileId: String,

    @Column(name = "expo_push_token", nullable = false)
    var expoPushToken: String
) : BaseEntity()
