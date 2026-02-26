package com.nsnm.herenow.domain.item.model.entity

import com.nsnm.herenow.lib.model.entity.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "item_history")
class ItemHistoryEntity(
    
    @Id
    var itemHistoryId: String = java.util.UUID.randomUUID().toString(),

    @Column(nullable = false, length = 36)
    val itemId: String,
    
    @Column(nullable = false, length = 36)
    val groupId: String,

    @Column(nullable = false, length = 20)
    val actionType: String, // "CREATE", "UPDATE", "DELETE"
    
    @Column(columnDefinition = "TEXT")
    val changes: String? = null, // 변경 내용을 요약하는 JSON 텍스트 등 (선택적 사용)
    
    @Column(nullable = false)
    val actionUserName: String = "알 수 없음"

) : BaseEntity()
