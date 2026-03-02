package com.nsnm.herenow.api.home.v1.dto

import io.swagger.v3.oas.annotations.media.Schema

data class HomeResponse(
    @Schema(description = "총 등록 물건 수")
    val totalItemCount: Int,
    
    @Schema(description = "이번 달에 새로 추가된 아이템 건수")
    val addedThisMonthCount: Int,
    
    @Schema(description = "임박(7일 이내) 또는 만료된 물건의 총 개수")
    val imminentOrExpiredCount: Int,
    
    @Schema(description = "수량 부족 항목 리스트 (최대 10개)")
    val lowStockItems: List<HomeItemDto>,
    
    @Schema(description = "최근 멤버 활동 내역 리스트 (최대 10개)")
    val recentActivities: List<HomeRecentActivityDto>,
    
    @Schema(description = "장소별 모아보기 요약 데이터 (Key: locationGroup, Value: HomeSummaryItemDto 리스트)")
    val locationsSummary: Map<String, List<HomeSummaryItemDto>>,
    
    @Schema(description = "카테고리별 모아보기 요약 데이터 (Key: categoryGroup, Value: HomeSummaryItemDto 리스트)")
    val categoriesSummary: Map<String, List<HomeSummaryItemDto>>,

    @Schema(description = "현재 조회된 대표 그룹명")
    val groupName: String? = null,

    @Schema(description = "현재 조회된 대표 그룹 ID")
    val groupId: String? = null
)

data class HomeItemDto(
    @Schema(description = "아이템 ID")
    val itemId: String,
    
    @Schema(description = "아이템 이름")
    val itemName: String,
    
    @Schema(description = "카테고리 이름 (조인본)")
    val categoryName: String?,
    
    @Schema(description = "보관 장소 이름 (조인본)")
    val locationName: String?,
    
    @Schema(description = "D-Day 텍스트 (예: D-3, 만료)")
    val dDayText: String?,
    
    @Schema(description = "최근 추가일 기준 표시 (예: 어제, 오늘)")
    val addedDateText: String?,
    
    @Schema(description = "이미지 리스트")
    val photoUrls: List<String>? = null
)

data class HomeSummaryItemDto(
    @Schema(description = "고유 식별자(ID)")
    val id: String,
    
    @Schema(description = "노출될 이름")
    val name: String
)

data class HomeRecentActivityDto(
    @Schema(description = "타임라인(로그) 고유 ID")
    val historyId: String,
    
    @Schema(description = "대상 물건 ID")
    val itemId: String,
    
    @Schema(description = "대상 물건 이름")
    val itemName: String,
    
    @Schema(description = "활동 유형 (CREATE, UPDATE, DELETE)")
    val actionType: String,
    
    @Schema(description = "활동 주체 (수행한 멤버 이름)")
    val actorName: String,
    
    @Schema(description = "활동 일시 표시 (예: 방금 전, 2시간 전)")
    val timestampText: String
)
