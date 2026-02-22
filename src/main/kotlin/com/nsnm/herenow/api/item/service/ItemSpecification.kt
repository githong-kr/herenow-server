package com.nsnm.herenow.api.item.service

import com.nsnm.herenow.domain.item.model.entity.ItemEntity
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDate

object ItemSpecification {
    fun search(
        groupId: String,
        keyword: String?,
        categoryId: String?,
        locationId: String?,
        status: String?
    ): Specification<ItemEntity> {
        return Specification { root, query, cb ->
            val predicates = mutableListOf<Predicate>()
            
            // 공통 조건: 그룹 ID
            predicates.add(cb.equal(root.get<String>("groupId"), groupId))

            // 아이템 이름 LIKE 검색
            if (!keyword.isNullOrBlank()) {
                predicates.add(cb.like(cb.lower(root.get("itemName")), "%${keyword.trim().lowercase()}%"))
            }

            // 카테고리 일치
            if (!categoryId.isNullOrBlank()) {
                predicates.add(cb.equal(root.get<String>("categoryId"), categoryId))
            }

            // 보관장소 일치
            if (!locationId.isNullOrBlank()) {
                predicates.add(cb.equal(root.get<String>("locationId"), locationId))
            }

            // 만료일 관련 상태 필터
            if (!status.isNullOrBlank() && status != "ALL") {
                val today = LocalDate.now()
                when (status) {
                    "IMMINENT" -> {
                        // 유통기한이 존재하고, 만료일이 오늘~7일 뒤 이내인 경우
                        val limitDate = today.plusDays(7)
                        predicates.add(cb.isNotNull(root.get<LocalDate>("expiryDate")))
                        predicates.add(cb.between(root.get("expiryDate"), today, limitDate))
                    }
                    "EXPIRED" -> {
                        // 유통기한이 존재하고, 만료일이 어제(오늘 이전)인 항목
                        predicates.add(cb.isNotNull(root.get<LocalDate>("expiryDate")))
                        predicates.add(cb.lessThan(root.get("expiryDate"), today))
                    }
                }
            }

            cb.and(*predicates.toTypedArray())
        }
    }
}
