package com.nsnm.herenow.api.item.service

import com.nsnm.herenow.api.item.v1.dto.CategoryDto
import com.nsnm.herenow.api.item.v1.dto.CreateCategoryRequest
import com.nsnm.herenow.api.item.v1.dto.CreateLocationRequest
import com.nsnm.herenow.api.item.v1.dto.CreateTagRequest
import com.nsnm.herenow.api.item.v1.dto.LocationDto
import com.nsnm.herenow.api.item.v1.dto.TagDto
import com.nsnm.herenow.domain.item.model.entity.CategoryEntity
import com.nsnm.herenow.domain.item.model.entity.LocationEntity
import com.nsnm.herenow.domain.item.model.entity.TagEntity
import com.nsnm.herenow.domain.item.repository.CategoryRepository
import com.nsnm.herenow.domain.item.repository.LocationRepository
import com.nsnm.herenow.domain.item.repository.TagRepository
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
            CategoryDto(it.categoryId, it.categoryName, it.iconName, it.parentCategoryId, it.displayOrder)
        }
    }

    @Transactional
    fun createCategory(groupId: String, request: CreateCategoryRequest): CategoryDto {
        val entity = CategoryEntity(
            groupId = groupId,
            categoryName = request.categoryName,
            iconName = request.iconName,
            parentCategoryId = request.parentCategoryId,
            displayOrder = request.displayOrder
        )
        val saved = categoryRepository.save(entity)
        return CategoryDto(saved.categoryId, saved.categoryName, saved.iconName, saved.parentCategoryId, saved.displayOrder)
    }

    // --- Location ---
    @Transactional(readOnly = true)
    fun getLocations(groupId: String): List<LocationDto> {
        return locationRepository.findByGroupId(groupId).map {
            LocationDto(it.locationId, it.locationName, it.iconName, it.photoUrl, it.parentLocationId, it.displayOrder)
        }
    }

    @Transactional
    fun createLocation(groupId: String, request: CreateLocationRequest): LocationDto {
        val entity = LocationEntity(
            groupId = groupId,
            locationName = request.locationName,
            iconName = request.iconName,
            photoUrl = request.photoUrl,
            parentLocationId = request.parentLocationId,
            displayOrder = request.displayOrder
        )
        val saved = locationRepository.save(entity)
        return LocationDto(saved.locationId, saved.locationName, saved.iconName, saved.photoUrl, saved.parentLocationId, saved.displayOrder)
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
}
