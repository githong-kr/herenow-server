package com.nsnm.herenow.domain.item.repository

import com.nsnm.herenow.domain.item.model.entity.CategoryEntity
import com.nsnm.herenow.domain.item.model.entity.ItemEntity
import com.nsnm.herenow.domain.item.model.entity.ItemPhotoEntity
import com.nsnm.herenow.domain.item.model.entity.ItemTagEntity
import com.nsnm.herenow.domain.item.model.entity.LocationEntity
import com.nsnm.herenow.domain.item.model.entity.TagEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<CategoryEntity, String> {
    fun findByGroupId(groupId: String): List<CategoryEntity>
}

@Repository
interface LocationRepository : JpaRepository<LocationEntity, String> {
    fun findByGroupId(groupId: String): List<LocationEntity>
}

@Repository
interface TagRepository : JpaRepository<TagEntity, String> {
    fun findByGroupId(groupId: String): List<TagEntity>
}

@Repository
interface ItemTagRepository : JpaRepository<ItemTagEntity, String> {
    fun findByItemId(itemId: String): List<ItemTagEntity>
    fun deleteByItemId(itemId: String)
}

@Repository
interface ItemRepository : JpaRepository<ItemEntity, String>, JpaSpecificationExecutor<ItemEntity> {
    fun findByGroupId(groupId: String): List<ItemEntity>
    fun existsByCategoryId(categoryId: String): Boolean
    fun existsByLocationId(locationId: String): Boolean
}

@Repository
interface ItemPhotoRepository : JpaRepository<ItemPhotoEntity, String> {
    fun findByItemId(itemId: String): List<ItemPhotoEntity>
    fun findByItemIdIn(itemIds: List<String>): List<ItemPhotoEntity>
    fun deleteByItemId(itemId: String)
}
