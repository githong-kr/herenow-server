package com.nsnm.herenow.api.home.service

import com.nsnm.herenow.api.home.v1.dto.HomeItemDto
import com.nsnm.herenow.api.home.v1.dto.HomeResponse
import com.nsnm.herenow.domain.group.repository.UserGroupRepository
import com.nsnm.herenow.domain.item.model.entity.CategoryEntity
import com.nsnm.herenow.domain.item.model.entity.ItemEntity
import com.nsnm.herenow.domain.item.model.entity.LocationEntity
import com.nsnm.herenow.domain.item.repository.CategoryRepository
import com.nsnm.herenow.domain.item.repository.ItemRepository
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
    private val locationRepository: LocationRepository
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

        // 카테고리/장소 메타 데이터 맵 (빠른 매핑용)
        val catMap = categories.associateBy { it.categoryId }
        val locMap = locations.associateBy { it.locationId }

        val now = LocalDate.now()

        // 3. 통계 계산
        val totalItemCount = items.size
        val addedThisMonthCount = items.count { it.frstRegTmst?.toLocalDate()?.isAfter(now.withDayOfMonth(1).minusDays(1)) == true || (it.purchaseDate != null && it.purchaseDate!! >= now.withDayOfMonth(1)) }
        val imminentOrExpiredCount = items.count { it.expiryDate != null && ChronoUnit.DAYS.between(now, it.expiryDate) <= 7 }

        // 4. 임박 리스트 (만료일자가 임박한 순 정렬, D-7 이내 항목 위주)
        val imminentItems = items
            .filter { it.expiryDate != null }
            .sortedBy { it.expiryDate }
            .take(10)
            .map { convertToHomeItemDto(it, catMap, locMap, now) }

        // 5. 최근 아이템 리스트 (등록일/구매일 기준 역순)
        val recentItems = items
            .sortedByDescending { it.frstRegTmst }
            .take(10)
            .map { convertToHomeItemDto(it, catMap, locMap, now) }

        // 6. 위치/카테고리 요약 데이터
        val locationsSummary = locations
            .filter { it.locationGroup != null }
            .groupBy { it.locationGroup!! }
            .mapValues { entry -> entry.value.sortedBy { it.displayOrder }.map { it.locationName } }

        val categoriesSummary = categories
            .filter { it.categoryGroup != null }
            .groupBy { it.categoryGroup!! }
            .mapValues { entry -> entry.value.sortedBy { it.displayOrder }.map { it.categoryName } }

        return HomeResponse(
            totalItemCount = totalItemCount,
            addedThisMonthCount = addedThisMonthCount,
            imminentOrExpiredCount = imminentOrExpiredCount,
            imminentItems = imminentItems,
            recentItems = recentItems,
            locationsSummary = locationsSummary,
            categoriesSummary = categoriesSummary,
            groupName = groupName,
            groupId = groupId
        )
    }

    private fun createEmptyResponse() = HomeResponse(
        totalItemCount = 0, addedThisMonthCount = 0, imminentOrExpiredCount = 0,
        imminentItems = emptyList(), recentItems = emptyList(),
        locationsSummary = emptyMap(), categoriesSummary = emptyMap()
    )

    private fun convertToHomeItemDto(
        item: ItemEntity, catMap: Map<String, CategoryEntity>, locMap: Map<String, LocationEntity>, now: LocalDate
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
            addedDateText = addedTextText
        )
    }
}
