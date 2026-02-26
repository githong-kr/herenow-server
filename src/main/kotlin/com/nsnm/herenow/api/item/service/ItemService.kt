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
import com.nsnm.herenow.fwk.core.base.BaseService

@Service
class ItemService(
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository,
    private val locationRepository: LocationRepository,
    private val tagRepository: TagRepository,
    private val itemTagRepository: ItemTagRepository,
    private val itemPhotoRepository: ItemPhotoRepository,
    private val profileRepository: com.nsnm.herenow.domain.user.repository.ProfileRepository,
    private val itemHistoryRepository: com.nsnm.herenow.domain.item.repository.ItemHistoryRepository
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

    @Transactional(readOnly = true)
    fun getItem(groupId: String, itemId: String): ItemResponse {
        val itemEntity = itemRepository.findById(itemId)
            .filter { it.groupId == groupId }
            .orElseThrow { BizException("존재하지 않거나 권한이 없는 아이템입니다.") }
        return mapToItemResponse(itemEntity)
    }

    @Transactional
    fun createItem(groupId: String, userId: String, request: CreateItemRequest): ItemResponse {
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

        // 5. 이력 기록 (CREATE)
        itemHistoryRepository.save(com.nsnm.herenow.domain.item.model.entity.ItemHistoryEntity(
            itemId = savedItem.itemId,
            groupId = groupId,
            actionType = "CREATE",
            changes = "물건 신규 등재",
            actionUserId = userId
        ))

        return ItemResponse(
            itemId = savedItem.itemId,
            itemName = savedItem.itemName,
            categoryId = savedItem.categoryId,
            categoryName = savedItem.categoryId?.let { categoryRepository.findById(it).orElse(null)?.categoryName },
            locationId = savedItem.locationId,
            locationName = savedItem.locationId?.let { locationRepository.findById(it).orElse(null)?.locationName },
            quantity = savedItem.quantity,
            minQuantity = savedItem.minQuantity,
            expiryDate = savedItem.expiryDate,
            memo = savedItem.memo,
            tags = savedTagNames,
            photoUrls = savedPhotos.map { it.photoUrl },
            frstRegTmst = savedItem.frstRegTmst,
            frstRegName = savedItem.frstRegGuid?.let { profileRepository.findById(it).orElse(null)?.name },
            lastChngTmst = savedItem.lastChngTmst,
            lastChngName = savedItem.lastChngGuid?.let { profileRepository.findById(it).orElse(null)?.name }
        )
    }

    @Transactional
    fun updateItem(groupId: String, userId: String, itemId: String, request: UpdateItemRequest): ItemResponse {
        val itemEntity = itemRepository.findById(itemId)
            .filter { it.groupId == groupId }
            .orElseThrow { BizException("존재하지 않거나 권한이 없는 아이템입니다.") }

        if (request.categoryId != null && !categoryRepository.existsById(request.categoryId)) {
            throw BizException("존재하지 않는 카테고리입니다.")
        }
        if (request.locationId != null && !locationRepository.existsById(request.locationId)) {
            throw BizException("존재하지 않는 보관장소입니다.")
        }

        // 변경점 추적
        val changedFields = mutableListOf<String>()
        
        fun String?.orEmptyText() = this ?: "없음"
        fun java.math.BigDecimal?.orEmptyText() = this?.toString() ?: "없음"
        fun java.time.LocalDate?.orEmptyText() = this?.toString() ?: "없음"

        if (itemEntity.categoryId != request.categoryId) {
            val oldCat = itemEntity.categoryId?.let { categoryRepository.findById(it).orElse(null)?.categoryName } ?: "없음"
            val newCat = request.categoryId?.let { categoryRepository.findById(it).orElse(null)?.categoryName } ?: "없음"
            changedFields.add("카테고리: $oldCat -> $newCat")
        }
        if (itemEntity.locationId != request.locationId) {
            val oldLoc = itemEntity.locationId?.let { locationRepository.findById(it).orElse(null)?.locationName } ?: "없음"
            val newLoc = request.locationId?.let { locationRepository.findById(it).orElse(null)?.locationName } ?: "없음"
            changedFields.add("보관장소: $oldLoc -> $newLoc")
        }
        if (itemEntity.itemName != request.itemName) changedFields.add("이름: ${itemEntity.itemName} -> ${request.itemName}")
        if (itemEntity.quantity != request.quantity) changedFields.add("수량: ${itemEntity.quantity} -> ${request.quantity}")
        if (itemEntity.minQuantity != request.minQuantity) changedFields.add("경고수량: ${itemEntity.minQuantity} -> ${request.minQuantity}")
        if (itemEntity.purchaseDate != request.purchaseDate) changedFields.add("구입일: ${itemEntity.purchaseDate.orEmptyText()} -> ${request.purchaseDate.orEmptyText()}")
        if (itemEntity.purchasePlace != request.purchasePlace) changedFields.add("구입처: ${itemEntity.purchasePlace.orEmptyText()} -> ${request.purchasePlace.orEmptyText()}")
        if (itemEntity.price != request.price) changedFields.add("가격: ${itemEntity.price.orEmptyText()} -> ${request.price.orEmptyText()}")
        if (itemEntity.expiryDate != request.expiryDate) changedFields.add("소비기한: ${itemEntity.expiryDate.orEmptyText()} -> ${request.expiryDate.orEmptyText()}")
        if (itemEntity.memo != request.memo) changedFields.add("메모: ${itemEntity.memo.orEmptyText()} -> ${request.memo.orEmptyText()}")

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

        // 2. 사진 정보 업데이트 (기존 내역과 비교 후 재생성)
        val oldPhotos = itemPhotoRepository.findByItemId(itemId).map { it.photoUrl }
        val newPhotos = request.photoUrls
        if (oldPhotos != newPhotos) {
            val added = newPhotos.count { !oldPhotos.contains(it) }
            val removed = oldPhotos.count { !newPhotos.contains(it) }
            if (added > 0 || removed > 0) {
                val parts = mutableListOf<String>()
                if (added > 0) parts.add("${added}장 추가")
                if (removed > 0) parts.add("${removed}장 삭제")
                changedFields.add("사진: ${parts.joinToString(", ")}")
            }
        }
        itemPhotoRepository.deleteByItemId(itemId)
        val savedPhotos = request.photoUrls.mapIndexed { index, url ->
            itemPhotoRepository.save(ItemPhotoEntity(
                itemId = savedItem.itemId,
                photoUrl = url,
                displayOrder = index
            ))
        }

        // 3. 태그 정보 업데이트 (기존 매핑 삭제 후 재생성)
        val oldTagsEntities = itemTagRepository.findByItemId(itemId)
        val oldTagNames = oldTagsEntities.mapNotNull { tagRepository.findById(it.tagId).orElse(null)?.tagName }
        val newTagNamesInput = request.tags // 보통 클라이언트에서 tagName 문자열 배열을 넘김

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
        
        // 태그 변경내역 기록
        if (oldTagNames.sorted() != savedTagNames.sorted()) {
            val addedTags = savedTagNames.filter { !oldTagNames.contains(it) }
            val removedTags = oldTagNames.filter { !savedTagNames.contains(it) }
            if (addedTags.isNotEmpty() || removedTags.isNotEmpty()) {
                val parts = mutableListOf<String>()
                if (addedTags.isNotEmpty()) parts.add("추가(${addedTags.joinToString(", ")})")
                if (removedTags.isNotEmpty()) parts.add("삭제(${removedTags.joinToString(", ")})")
                changedFields.add("태그: ${parts.joinToString(", ")}")
            }
        }

        val changesText = if (changedFields.isEmpty()) "세부 정보 유지" else changedFields.joinToString("\n")

        itemHistoryRepository.save(com.nsnm.herenow.domain.item.model.entity.ItemHistoryEntity(
            itemId = savedItem.itemId,
            groupId = groupId,
            actionType = "UPDATE",
            changes = changesText,
            actionUserId = userId
        ))

        return ItemResponse(
            itemId = savedItem.itemId,
            itemName = savedItem.itemName,
            categoryId = savedItem.categoryId,
            categoryName = savedItem.categoryId?.let { categoryRepository.findById(it).orElse(null)?.categoryName },
            locationId = savedItem.locationId,
            locationName = savedItem.locationId?.let { locationRepository.findById(it).orElse(null)?.locationName },
            quantity = savedItem.quantity,
            minQuantity = savedItem.minQuantity,
            expiryDate = savedItem.expiryDate,
            memo = savedItem.memo,
            tags = savedTagNames,
            photoUrls = savedPhotos.map { it.photoUrl },
            frstRegTmst = savedItem.frstRegTmst,
            frstRegName = savedItem.frstRegGuid?.let { profileRepository.findById(it).orElse(null)?.name },
            lastChngTmst = savedItem.lastChngTmst,
            lastChngName = savedItem.lastChngGuid?.let { profileRepository.findById(it).orElse(null)?.name }
        )
    }

    @Transactional
    fun deleteItem(groupId: String, userId: String, itemId: String) {
        val itemEntity = itemRepository.findById(itemId)
            .filter { it.groupId == groupId }
            .orElseThrow { BizException("존재하지 않거나 권한이 없는 아이템입니다.") }

        // 하위 엔티티들을 모두 명시적으로 삭제 (DB 설정에 cascade 가 없을 것을 대비)
        itemTagRepository.deleteByItemId(itemId)
        itemPhotoRepository.deleteByItemId(itemId)
        
        // 아이템 엔티티 삭제
        itemRepository.delete(itemEntity)

        // 이력 기록 (DELETE) - 이미 삭제되었지만 로그 추적을 위해 저장
        itemHistoryRepository.save(com.nsnm.herenow.domain.item.model.entity.ItemHistoryEntity(
            itemId = itemId,
            groupId = groupId,
            actionType = "DELETE",
            changes = "물건 삭제",
            actionUserId = userId
        ))
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
            categoryName = itemEntity.categoryId?.let { categoryRepository.findById(it).orElse(null)?.categoryName },
            locationId = itemEntity.locationId,
            locationName = itemEntity.locationId?.let { locationRepository.findById(it).orElse(null)?.locationName },
            quantity = itemEntity.quantity,
            minQuantity = itemEntity.minQuantity,
            expiryDate = itemEntity.expiryDate,
            memo = itemEntity.memo,
            tags = tags,
            photoUrls = photoUrls,
            frstRegTmst = itemEntity.frstRegTmst,
            frstRegName = itemEntity.frstRegGuid?.let { profileRepository.findById(it).orElse(null)?.name },
            lastChngTmst = itemEntity.lastChngTmst,
            lastChngName = itemEntity.lastChngGuid?.let { profileRepository.findById(it).orElse(null)?.name }
        )
    }

    @Transactional(readOnly = true)
    fun getItemHistory(itemId: String): List<com.nsnm.herenow.api.item.v1.dto.ItemHistoryResponse> {
        val historyList = itemHistoryRepository.findByItemIdOrderByFrstRegTmstDesc(itemId)
        return historyList.map {
            com.nsnm.herenow.api.item.v1.dto.ItemHistoryResponse(
                itemHistoryId = it.itemHistoryId,
                actionType = it.actionType,
                changes = it.changes,
                actionUserName = profileRepository.findById(it.actionUserId).orElse(null)?.name ?: "알 수 없음",
                tmst = it.frstRegTmst?.toString()
            )
        }
    }
}
