package com.nsnm.herenow.domain.shopping.repository

import com.nsnm.herenow.domain.shopping.entity.ShoppingListEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ShoppingListRepository : JpaRepository<ShoppingListEntity, UUID> {
    fun findBySpaceIdOrderByCreatedAtDesc(spaceId: UUID): List<ShoppingListEntity>
    fun deleteBySpaceIdAndCheckedTrue(spaceId: UUID)
}
