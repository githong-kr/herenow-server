package com.nsnm.herenow.domain.item.repository

import com.nsnm.herenow.domain.item.entity.ItemEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface ItemRepository : JpaRepository<ItemEntity, UUID> {
    fun findBySpaceId(spaceId: UUID): List<ItemEntity>
    fun findByStorageId(storageId: UUID): List<ItemEntity>
    fun findBySpaceIdAndExpiryDateBefore(spaceId: UUID, date: LocalDate): List<ItemEntity>
    fun findBySpaceIdAndExpiryDateBetween(spaceId: UUID, start: LocalDate, end: LocalDate): List<ItemEntity>
}
