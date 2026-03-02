package com.nsnm.herenow.lib.model.entity

import com.nsnm.herenow.lib.model.entity.base.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table

enum class NotificationType {
    ITEM_CREATED,
    ITEM_UPDATED,
    ITEM_DELETED,
    COMMENT_ADDED,
    GROUP_MEMBER_JOINED,
    GROUP_MEMBER_REMOVED,
    INVITE_RECEIVED
}

@Entity
@Table(name = "notification")
class NotificationEntity(
    @Id
    @Column(name = "notification_id")
    var notificationId: String,

    @Column(name = "profile_id", nullable = false)
    var profileId: String,

    @Column(name = "title", nullable = false)
    var title: String,

    @Column(name = "body", columnDefinition = "TEXT", nullable = false)
    var body: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: NotificationType,

    @Column(name = "target_id")
    var targetId: String? = null,

    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false
) : BaseEntity()
