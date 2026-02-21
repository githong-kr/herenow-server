package com.nsnm.herenow.api.item.service

import com.nsnm.herenow.api.item.v1.dto.CategoryDto
import com.nsnm.herenow.api.item.v1.dto.CreateCategoryRequest
import com.nsnm.herenow.api.item.v1.dto.CreateLocationRequest
import com.nsnm.herenow.api.item.v1.dto.CreateTagRequest
import com.nsnm.herenow.api.item.v1.dto.LocationDto
import com.nsnm.herenow.api.item.v1.dto.TagDto
import com.nsnm.herenow.api.item.v1.dto.UpdateCategoryRequest
import com.nsnm.herenow.api.item.v1.dto.UpdateLocationRequest
import com.nsnm.herenow.api.item.v1.dto.UpdateTagRequest
import com.nsnm.herenow.domain.item.model.entity.CategoryEntity
import com.nsnm.herenow.domain.item.model.entity.LocationEntity
import com.nsnm.herenow.domain.item.model.entity.TagEntity
import com.nsnm.herenow.domain.item.repository.CategoryRepository
import com.nsnm.herenow.domain.item.repository.LocationRepository
import com.nsnm.herenow.domain.item.repository.TagRepository
import com.nsnm.herenow.fwk.core.error.BizException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ItemMetadataService(
    private val categoryRepository: CategoryRepository,
    private val locationRepository: LocationRepository,
    private val tagRepository: TagRepository
) {

    // --- Category ---
    @Transactional(readOnly = true)
    fun getCategories(groupId: String): List<CategoryDto> {
        return categoryRepository.findByGroupId(groupId).map {
            CategoryDto(it.categoryId, it.categoryName, it.iconName, it.categoryGroup, it.displayOrder)
        }
    }

    @Transactional
    fun createCategory(groupId: String, request: CreateCategoryRequest): CategoryDto {
        val entity = CategoryEntity(
            groupId = groupId,
            categoryName = request.categoryName,
            iconName = request.iconName,
            categoryGroup = request.categoryGroup,
            displayOrder = request.displayOrder
        )
        val saved = categoryRepository.save(entity)
        return CategoryDto(saved.categoryId, saved.categoryName, saved.iconName, saved.categoryGroup, saved.displayOrder)
    }

    @Transactional
    fun updateCategory(groupId: String, categoryId: String, request: UpdateCategoryRequest): CategoryDto {
        val entity = categoryRepository.findById(categoryId)
            .filter { it.groupId == groupId }
            .orElseThrow { BizException("존재하지 않거나 권한이 없는 카테고리입니다.") }
            
        entity.categoryName = request.categoryName
        entity.iconName = request.iconName
        entity.categoryGroup = request.categoryGroup
        entity.displayOrder = request.displayOrder
        
        val saved = categoryRepository.save(entity)
        return CategoryDto(saved.categoryId, saved.categoryName, saved.iconName, saved.categoryGroup, saved.displayOrder)
    }

    @Transactional
    fun deleteCategory(groupId: String, categoryId: String) {
        val entity = categoryRepository.findById(categoryId)
            .filter { it.groupId == groupId }
            .orElseThrow { BizException("존재하지 않거나 권한이 없는 카테고리입니다.") }
        
        // TODO: 만약 카테고리에 속한 아이템이 있다면 삭제 거부 로직 추가 가능
        categoryRepository.delete(entity)
    }

    // --- Location ---
    @Transactional(readOnly = true)
    fun getLocations(groupId: String): List<LocationDto> {
        return locationRepository.findByGroupId(groupId).map {
            LocationDto(it.locationId, it.locationName, it.iconName, it.photoUrl, it.locationGroup, it.displayOrder)
        }
    }

    @Transactional
    fun createLocation(groupId: String, request: CreateLocationRequest): LocationDto {
        val entity = LocationEntity(
            groupId = groupId,
            locationName = request.locationName,
            iconName = request.iconName,
            photoUrl = request.photoUrl,
            locationGroup = request.locationGroup,
            displayOrder = request.displayOrder
        )
        val saved = locationRepository.save(entity)
        return LocationDto(saved.locationId, saved.locationName, saved.iconName, saved.photoUrl, saved.locationGroup, saved.displayOrder)
    }

    @Transactional
    fun updateLocation(groupId: String, locationId: String, request: UpdateLocationRequest): LocationDto {
        val entity = locationRepository.findById(locationId)
            .filter { it.groupId == groupId }
            .orElseThrow { BizException("존재하지 않거나 권한이 없는 보관장소입니다.") }
            
        entity.locationName = request.locationName
        entity.iconName = request.iconName
        entity.photoUrl = request.photoUrl
        entity.locationGroup = request.locationGroup
        entity.displayOrder = request.displayOrder
        
        val saved = locationRepository.save(entity)
        return LocationDto(saved.locationId, saved.locationName, saved.iconName, saved.photoUrl, saved.locationGroup, saved.displayOrder)
    }

    @Transactional
    fun deleteLocation(groupId: String, locationId: String) {
        val entity = locationRepository.findById(locationId)
            .filter { it.groupId == groupId }
            .orElseThrow { BizException("존재하지 않거나 권한이 없는 보관장소입니다.") }
            
        // TODO: 만약 위치에 속한 아이템이나 하위 위치가 있다면 삭제 거부 로직 추가 가능
        locationRepository.delete(entity)
    }

    // --- Tag ---
    @Transactional(readOnly = true)
    fun getTags(groupId: String): List<TagDto> {
        return tagRepository.findByGroupId(groupId).map {
            TagDto(it.tagId, it.tagName)
        }
    }

    @Transactional
    fun createTag(groupId: String, request: CreateTagRequest): TagDto {
        val entity = TagEntity(
            groupId = groupId,
            tagName = request.tagName
        )
        val saved = tagRepository.save(entity)
        return TagDto(saved.tagId, saved.tagName)
    }

    @Transactional
    fun updateTag(groupId: String, tagId: String, request: UpdateTagRequest): TagDto {
        val entity = tagRepository.findById(tagId)
            .filter { it.groupId == groupId }
            .orElseThrow { BizException("존재하지 않거나 권한이 없는 태그입니다.") }
            
        entity.tagName = request.tagName
        val saved = tagRepository.save(entity)
        return TagDto(saved.tagId, saved.tagName)
    }

    @Transactional
    fun deleteTag(groupId: String, tagId: String) {
        val entity = tagRepository.findById(tagId)
            .filter { it.groupId == groupId }
            .orElseThrow { BizException("존재하지 않거나 권한이 없는 태그입니다.") }
            
        tagRepository.delete(entity)
    }
}
