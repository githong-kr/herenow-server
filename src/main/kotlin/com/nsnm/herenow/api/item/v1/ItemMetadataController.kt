package com.nsnm.herenow.api.item.v1

import com.nsnm.herenow.api.item.service.ItemMetadataService
import com.nsnm.herenow.api.item.v1.dto.CategoryDto
import com.nsnm.herenow.api.item.v1.dto.CreateCategoryRequest
import com.nsnm.herenow.api.item.v1.dto.CreateLocationRequest
import com.nsnm.herenow.api.item.v1.dto.CreateTagRequest
import com.nsnm.herenow.api.item.v1.dto.LocationDto
import com.nsnm.herenow.api.item.v1.dto.TagDto
import com.nsnm.herenow.api.item.v1.dto.UpdateCategoryRequest
import com.nsnm.herenow.api.item.v1.dto.UpdateLocationRequest
import com.nsnm.herenow.api.item.v1.dto.UpdateTagRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Item Metadata", description = "아이템 카테고리, 장소, 태그 관리 API")
@RestController
@RequestMapping("/api/v1/groups/{groupId}")
class ItemMetadataController(
    private val itemMetadataService: ItemMetadataService
) {

    // --- Category ---
    @Operation(summary = "카테고리 목록 조회", description = "특정 그룹의 모든 카테고리를 조회합니다.")
    @GetMapping("/categories")
    fun getCategories(@PathVariable groupId: String): List<CategoryDto> {
        return itemMetadataService.getCategories(groupId)
    }

    @Operation(summary = "카테고리 생성", description = "특정 그룹에 새로운 카테고리를 생성합니다.")
    @PostMapping("/categories")
    fun createCategory(
        @PathVariable groupId: String,
        @RequestBody request: CreateCategoryRequest
    ): CategoryDto {
        return itemMetadataService.createCategory(groupId, request)
    }

    @Operation(summary = "카테고리 수정", description = "특정 그룹의 카테고리를 수정합니다.")
    @PutMapping("/categories/{categoryId}")
    fun updateCategory(
        @PathVariable groupId: String,
        @PathVariable categoryId: String,
        @RequestBody request: UpdateCategoryRequest
    ): CategoryDto {
        return itemMetadataService.updateCategory(groupId, categoryId, request)
    }

    @Operation(summary = "카테고리 삭제", description = "특정 그룹의 카테고리를 삭제합니다.")
    @DeleteMapping("/categories/{categoryId}")
    fun deleteCategory(
        @PathVariable groupId: String,
        @PathVariable categoryId: String
    ) {
        itemMetadataService.deleteCategory(groupId, categoryId)
    }

    // --- Location ---
    @Operation(summary = "장소 목록 조회", description = "특정 그룹의 모든 보관 장소를 조회합니다.")
    @GetMapping("/locations")
    fun getLocations(@PathVariable groupId: String): List<LocationDto> {
        return itemMetadataService.getLocations(groupId)
    }

    @Operation(summary = "장소 생성", description = "특정 그룹에 새로운 장소를 생성합니다.")
    @PostMapping("/locations")
    fun createLocation(
        @PathVariable groupId: String,
        @RequestBody request: CreateLocationRequest
    ): LocationDto {
        return itemMetadataService.createLocation(groupId, request)
    }

    @Operation(summary = "장소 수정", description = "특정 그룹의 보관 장소를 수정합니다.")
    @PutMapping("/locations/{locationId}")
    fun updateLocation(
        @PathVariable groupId: String,
        @PathVariable locationId: String,
        @RequestBody request: UpdateLocationRequest
    ): LocationDto {
        return itemMetadataService.updateLocation(groupId, locationId, request)
    }

    @Operation(summary = "장소 삭제", description = "특정 그룹의 보관 장소를 삭제합니다.")
    @DeleteMapping("/locations/{locationId}")
    fun deleteLocation(
        @PathVariable groupId: String,
        @PathVariable locationId: String
    ) {
        itemMetadataService.deleteLocation(groupId, locationId)
    }

    // --- Tag ---
    @Operation(summary = "태그 목록 조회", description = "특정 그룹의 모든 태그를 조회합니다.")
    @GetMapping("/tags")
    fun getTags(@PathVariable groupId: String): List<TagDto> {
        return itemMetadataService.getTags(groupId)
    }

    @Operation(summary = "태그 생성", description = "특정 그룹에 새로운 태그를 생성합니다.")
    @PostMapping("/tags")
    fun createTag(
        @PathVariable groupId: String,
        @RequestBody request: CreateTagRequest
    ): TagDto {
        return itemMetadataService.createTag(groupId, request)
    }

    @Operation(summary = "태그 수정", description = "특정 그룹의 태그를 수정합니다.")
    @PutMapping("/tags/{tagId}")
    fun updateTag(
        @PathVariable groupId: String,
        @PathVariable tagId: String,
        @RequestBody request: UpdateTagRequest
    ): TagDto {
        return itemMetadataService.updateTag(groupId, tagId, request)
    }

    @Operation(summary = "태그 삭제", description = "특정 그룹의 태그를 삭제합니다.")
    @DeleteMapping("/tags/{tagId}")
    fun deleteTag(
        @PathVariable groupId: String,
        @PathVariable tagId: String
    ) {
        itemMetadataService.deleteTag(groupId, tagId)
    }
}
