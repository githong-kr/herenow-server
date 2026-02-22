package com.nsnm.herenow.api.item.service

import com.nsnm.herenow.api.item.v1.dto.CreateItemRequest
import com.nsnm.herenow.api.item.v1.dto.ItemResponse
import com.nsnm.herenow.api.item.v1.dto.UpdateItemRequest
import com.nsnm.herenow.domain.item.model.entity.ItemEntity
import com.nsnm.herenow.domain.item.model.entity.ItemPhotoEntity
import com.nsnm.herenow.domain.item.model.entity.ItemTagEntity
import com.nsnm.herenow.domain.item.model.entity.TagEntity
import com.nsnm.herenow.domain.item.repository.CategoryRepository
import com.nsnm.herenow.domain.item.repository.ItemPhotoRepository
import com.nsnm.herenow.domain.item.repository.ItemRepository
import com.nsnm.herenow.domain.item.repository.ItemTagRepository
import com.nsnm.herenow.domain.item.repository.LocationRepository
import com.nsnm.herenow.domain.item.repository.TagRepository
import com.nsnm.herenow.api.item.v1.dto.SearchItemRequest
import com.nsnm.herenow.fwk.core.error.BizException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import com.nsnm.herenow.fwk.core.base.BaseService

@Service
class ItemService(
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository,
    private val locationRepository: LocationRepository,
    private val tagRepository: TagRepository,
    private val itemTagRepository: ItemTagRepository,
    private val itemPhotoRepository: ItemPhotoRepository
) : BaseService() {

    @Transactional(readOnly = true)
    fun getItems(groupId: String, request: SearchItemRequest, pageable: Pageable): Page<ItemResponse> {
        val spec = ItemSpecification.search(
            groupId = groupId,
            keyword = request.keyword,
            categoryId = request.categoryId,
            locationId = request.locationId,
            status = request.status
        )
        val entityPage = itemRepository.findAll(spec, pageable)
        return entityPage.map { mapToItemResponse(it) }
    }

    @Transactional
    fun createItem(groupId: String, request: CreateItemRequest): ItemResponse {
        // 1. 참조 데이터 유효성 검증
        if (request.categoryId != null && !categoryRepository.existsById(request.categoryId)) {
            throw BizException("존재하지 않는 카테고리입니다.")
        }
        if (request.locationId != null && !locationRepository.existsById(request.locationId)) {
            throw BizException("존재하지 않는 보관장소입니다.")
        }

        // 2. 아이템 기본 정보 저장
        val itemEntity = ItemEntity(
            groupId = groupId,
            categoryId = request.categoryId,
            locationId = request.locationId,
            itemName = request.itemName,
            quantity = request.quantity,
            minQuantity = request.minQuantity,
            purchaseDate = request.purchaseDate,
            purchasePlace = request.purchasePlace,
            price = request.price,
            expiryDate = request.expiryDate,
            memo = request.memo
        )
        val savedItem = itemRepository.save(itemEntity)

        // 3. 사진 정보 일괄 저장
        val savedPhotos = request.photoUrls.mapIndexed { index, url ->
            itemPhotoRepository.save(ItemPhotoEntity(
                itemId = savedItem.itemId,
                photoUrl = url,
                displayOrder = index
            ))
        }

        // 4. 태그 처리 (없으면 생성, 있으면 연결)
        val savedTagNames = mutableListOf<String>()
        request.tags.forEach { tagNameOrId ->
            // 그룹 내 이미 존재하는 태그인지 이름으로 검색 (ID가 넘어올 수도 있으나, 여기선 이름 기반 UPSERT 처리)
            val existingTags = tagRepository.findByGroupId(groupId)
            var tag = existingTags.find { it.tagName == tagNameOrId || it.tagId == tagNameOrId }
            
            if (tag == null) {
                // 새 태그 생성
                tag = tagRepository.save(TagEntity(groupId = groupId, tagName = tagNameOrId))
            }
            
            // ItemTag 매핑 테이블 삽입
            itemTagRepository.save(ItemTagEntity(
                itemId = savedItem.itemId,
                tagId = tag.tagId
            ))
            
            savedTagNames.add(tag.tagName)
        }

        return ItemResponse(
            itemId = savedItem.itemId,
            itemName = savedItem.itemName,
            categoryId = savedItem.categoryId,
            locationId = savedItem.locationId,
            quantity = savedItem.quantity,
            minQuantity = savedItem.minQuantity,
            expiryDate = savedItem.expiryDate,
            memo = savedItem.memo,
            tags = savedTagNames,
            photoUrls = savedPhotos.map { it.photoUrl }
        )
    }

    @Transactional
    fun updateItem(groupId: String, itemId: String, request: UpdateItemRequest): ItemResponse {
        val itemEntity = itemRepository.findById(itemId)
            .filter { it.groupId == groupId }
            .orElseThrow { BizException("존재하지 않거나 권한이 없는 아이템입니다.") }

        if (request.categoryId != null && !categoryRepository.existsById(request.categoryId)) {
            throw BizException("존재하지 않는 카테고리입니다.")
        }
        if (request.locationId != null && !locationRepository.existsById(request.locationId)) {
            throw BizException("존재하지 않는 보관장소입니다.")
        }

        // 1. 아이템 정보 업데이트
        itemEntity.apply {
            categoryId = request.categoryId
            locationId = request.locationId
            itemName = request.itemName
            quantity = request.quantity
            minQuantity = request.minQuantity
            purchaseDate = request.purchaseDate
            purchasePlace = request.purchasePlace
            price = request.price
            expiryDate = request.expiryDate
            memo = request.memo
        }
        val savedItem = itemRepository.save(itemEntity)

        // 2. 사진 정보 업데이트 (기존 삭제 후 재생성)
        itemPhotoRepository.deleteByItemId(itemId)
        val savedPhotos = request.photoUrls.mapIndexed { index, url ->
            itemPhotoRepository.save(ItemPhotoEntity(
                itemId = savedItem.itemId,
                photoUrl = url,
                displayOrder = index
            ))
        }

        // 3. 태그 정보 업데이트 (기존 매핑 삭제 후 재생성)
        itemTagRepository.deleteByItemId(itemId)
        val savedTagNames = mutableListOf<String>()
        request.tags.forEach { tagNameOrId ->
            val existingTags = tagRepository.findByGroupId(groupId)
            var tag = existingTags.find { it.tagName == tagNameOrId || it.tagId == tagNameOrId }
            if (tag == null) {
                tag = tagRepository.save(TagEntity(groupId = groupId, tagName = tagNameOrId))
            }
            itemTagRepository.save(ItemTagEntity(
                itemId = savedItem.itemId,
                tagId = tag.tagId
            ))
            savedTagNames.add(tag.tagName)
        }

        return ItemResponse(
            itemId = savedItem.itemId,
            itemName = savedItem.itemName,
            categoryId = savedItem.categoryId,
            locationId = savedItem.locationId,
            quantity = savedItem.quantity,
            minQuantity = savedItem.minQuantity,
            expiryDate = savedItem.expiryDate,
            memo = savedItem.memo,
            tags = savedTagNames,
            photoUrls = savedPhotos.map { it.photoUrl }
        )
    }

    @Transactional
    fun deleteItem(groupId: String, itemId: String) {
        val itemEntity = itemRepository.findById(itemId)
            .filter { it.groupId == groupId }
            .orElseThrow { BizException("존재하지 않거나 권한이 없는 아이템입니다.") }

        // 하위 엔티티들을 모두 명시적으로 삭제 (DB 설정에 cascade 가 없을 것을 대비)
        itemTagRepository.deleteByItemId(itemId)
        itemPhotoRepository.deleteByItemId(itemId)
        
        // 아이템 엔티티 삭제
        itemRepository.delete(itemEntity)
    }

    private fun mapToItemResponse(itemEntity: ItemEntity): ItemResponse {
        val tags = itemTagRepository.findByItemId(itemEntity.itemId)
            .map { tagRepository.findById(it.tagId).orElse(null)?.tagName ?: "" }
            .filter { it.isNotBlank() }
        
        val photoUrls = itemPhotoRepository.findByItemId(itemEntity.itemId)
            .map { it.photoUrl }

        return ItemResponse(
            itemId = itemEntity.itemId,
            itemName = itemEntity.itemName,
            categoryId = itemEntity.categoryId,
            locationId = itemEntity.locationId,
            quantity = itemEntity.quantity,
            minQuantity = itemEntity.minQuantity,
            expiryDate = itemEntity.expiryDate,
            memo = itemEntity.memo,
            tags = tags,
            photoUrls = photoUrls
        )
    }
}
