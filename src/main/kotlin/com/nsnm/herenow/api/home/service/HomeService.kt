package com.nsnm.herenow.api.home.service

import com.nsnm.herenow.api.home.v1.dto.HomeItemDto
import com.nsnm.herenow.api.home.v1.dto.HomeResponse
import com.nsnm.herenow.api.home.v1.dto.HomeSummaryItemDto
import com.nsnm.herenow.domain.group.repository.UserGroupRepository
import com.nsnm.herenow.domain.item.model.entity.CategoryEntity
import com.nsnm.herenow.domain.item.model.entity.ItemEntity
import com.nsnm.herenow.domain.item.model.entity.LocationEntity
import com.nsnm.herenow.domain.item.model.entity.ItemPhotoEntity
import com.nsnm.herenow.domain.item.repository.CategoryRepository
import com.nsnm.herenow.domain.item.repository.ItemPhotoRepository
import com.nsnm.herenow.domain.item.repository.ItemRepository
import com.nsnm.herenow.domain.item.repository.ItemHistoryRepository
import com.nsnm.herenow.domain.item.repository.LocationRepository
import com.nsnm.herenow.domain.user.repository.ProfileRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import com.nsnm.herenow.fwk.core.base.BaseService

@Service
@Transactional(readOnly = true)
class HomeService(
    private val profileRepository: ProfileRepository,
    private val userGroupRepository: UserGroupRepository,
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository,
    private val locationRepository: LocationRepository,
    private val itemPhotoRepository: ItemPhotoRepository,
    private val itemHistoryRepository: ItemHistoryRepository
) : BaseService() {

    fun getHomeDashboardData(uid: String): HomeResponse {
        // 1. 프로필 조회 및 대표 그룹 식별 (대표 그룹이 없으면 기본 0 반환)
        val profile = profileRepository.findById(uid).orElse(null)
            ?: return createEmptyResponse()
        
        val groupId = profile.representativeGroupId ?: return createEmptyResponse()
        
        // 1-1. 대표 그룹 그룹명 조회
        val groupName = userGroupRepository.findById(groupId).orElse(null)?.groupName

        // 2. 그룹 내 전체 데이터 조회
        val items = itemRepository.findByGroupId(groupId)
        val categories = categoryRepository.findByGroupId(groupId)
        val locations = locationRepository.findByGroupId(groupId)
        val itemPhotos = itemPhotoRepository.findByItemIdIn(items.map { it.itemId })

        // 카테고리/장소 메타 데이터 맵 (빠른 매핑용)
        val catMap = categories.associateBy { it.categoryId }
        val locMap = locations.associateBy { it.locationId }
        val photoMap = itemPhotos.groupBy { it.itemId }

        val now = LocalDate.now()

        // 3. 통계 계산
        val totalItemCount = items.size
        val addedThisMonthCount = items.count { it.frstRegTmst?.toLocalDate()?.isAfter(now.withDayOfMonth(1).minusDays(1)) == true || (it.purchaseDate != null && it.purchaseDate!! >= now.withDayOfMonth(1)) }
        val imminentOrExpiredCount = items.count { it.expiryDate != null && ChronoUnit.DAYS.between(now, it.expiryDate) <= 7 }

        // 4. 수량 부족 리스트 (최소수량보다 적게 남은 아이템, minQuantity > 0 기준)
        val lowStockItems = items
            .filter { it.minQuantity > 0 && it.quantity <= it.minQuantity }
            .sortedBy { it.quantity - it.minQuantity } // 부족분이 큰 순(또는 음수 작은 순)
            .take(10)
            .map { convertToHomeItemDto(it, catMap, locMap, photoMap, now) }

        // 5. 최근 활동 통계 (그룹 내 ItemHistory 내역 최근 10개)
        val recentHistory = itemHistoryRepository.findByGroupIdOrderByFrstRegTmstDesc(groupId).take(10)
        
        // 이력 활동자 이름을 가져오기 위해 profile 맵 구성
        val actorIds = recentHistory.map { it.actionUserId }.distinct()
        val actorProfiles = profileRepository.findAllById(actorIds).associateBy { it.profileId }

        val recentActivities = recentHistory.map { history ->
            val timestamp = history.frstRegTmst ?: java.time.LocalDateTime.now()
            val timeDiffMinutes = java.time.temporal.ChronoUnit.MINUTES.between(timestamp, java.time.LocalDateTime.now())
            val timestampText = when {
                timeDiffMinutes < 60 -> if (timeDiffMinutes <= 0) "방금 전" else "${timeDiffMinutes}분 전"
                timeDiffMinutes < 24 * 60 -> "${timeDiffMinutes / 60}시간 전"
                else -> "${timeDiffMinutes / (24 * 60)}일 전"
            }
            
            // 삭제된 항목 등은 itemName을 파싱하거나 보조해야 하지만, 기본적으로 Entity에 있는 걸 쓴다고 가정 (또는 json파싱 필요).
            // 해당 서비스/엔티티에 itemName 저장이 없으므로 items 기준 fallback.
            val itemNameFallback = items.find { it.itemId == history.itemId }?.itemName ?: "삭제된 물건"

            com.nsnm.herenow.api.home.v1.dto.HomeRecentActivityDto(
                historyId = history.itemHistoryId,
                itemId = history.itemId,
                itemName = itemNameFallback,
                actionType = history.actionType,
                actorName = actorProfiles[history.actionUserId]?.name ?: "알 수 없음",
                timestampText = timestampText
            )
        }

        // 6. 위치/카테고리 요약 데이터
        val locationsSummary = locations
            .filter { it.locationGroup != null }
            .groupBy { it.locationGroup!! }
            .mapValues { entry -> entry.value.sortedBy { it.displayOrder }.map { HomeSummaryItemDto(id = it.locationId, name = it.locationName) } }

        val categoriesSummary = categories
            .filter { it.categoryGroup != null }
            .groupBy { it.categoryGroup!! }
            .mapValues { entry -> entry.value.sortedBy { it.displayOrder }.map { HomeSummaryItemDto(id = it.categoryId, name = it.categoryName) } }

        return HomeResponse(
            totalItemCount = totalItemCount,
            addedThisMonthCount = addedThisMonthCount,
            imminentOrExpiredCount = imminentOrExpiredCount, // 하위호환성 유지 혹은 필요시 제거
            lowStockItems = lowStockItems,
            recentActivities = recentActivities,
            locationsSummary = locationsSummary,
            categoriesSummary = categoriesSummary,
            groupName = groupName,
            groupId = groupId
        )
    }

    private fun createEmptyResponse() = HomeResponse(
        totalItemCount = 0, addedThisMonthCount = 0, imminentOrExpiredCount = 0,
        lowStockItems = emptyList(), recentActivities = emptyList(),
        locationsSummary = emptyMap(), categoriesSummary = emptyMap()
    )

    private fun convertToHomeItemDto(
        item: ItemEntity, catMap: Map<String, CategoryEntity>, locMap: Map<String, LocationEntity>, photoMap: Map<String, List<ItemPhotoEntity>>, now: LocalDate
    ): HomeItemDto {
        val catName = item.categoryId?.let { catMap[it]?.categoryName }
        val locName = item.locationId?.let { locMap[it]?.locationName }
        
        var dDayText: String? = null
        if (item.expiryDate != null) {
            val days = ChronoUnit.DAYS.between(now, item.expiryDate)
            dDayText = when {
                days < 0 -> "만료"
                days == 0L -> "D-Day"
                else -> "D-$days"
            }
        }
        
        var addedTextText: String? = null
        val targetDate = item.frstRegTmst?.toLocalDate() ?: item.purchaseDate
        if (targetDate != null) {
            val addedDays = ChronoUnit.DAYS.between(targetDate, now)
            addedTextText = when(addedDays) {
                0L -> "오늘"
                1L -> "어제"
                else -> "${addedDays}일 전"
            }
        }

        return HomeItemDto(
            itemId = item.itemId,
            itemName = item.itemName,
            categoryName = catName,
            locationName = locName,
            dDayText = dDayText,
            addedDateText = addedTextText,
            photoUrls = photoMap[item.itemId]?.map { it.photoUrl }
        )
    }
}
