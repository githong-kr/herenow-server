package com.nsnm.herenow.fwk.custom.config

import com.nsnm.herenow.domain.group.model.entity.UserGroupEntity
import com.nsnm.herenow.domain.group.model.entity.UserGroupMemberEntity
import com.nsnm.herenow.domain.group.model.enums.GroupRole
import com.nsnm.herenow.domain.group.repository.UserGroupMemberRepository
import com.nsnm.herenow.domain.group.repository.UserGroupRepository
import com.nsnm.herenow.domain.item.model.entity.CategoryEntity
import com.nsnm.herenow.domain.item.model.entity.ItemCommentEntity
import com.nsnm.herenow.domain.item.model.entity.ItemEntity
import com.nsnm.herenow.domain.item.model.entity.LocationEntity
import com.nsnm.herenow.domain.item.repository.CategoryRepository
import com.nsnm.herenow.domain.item.repository.ItemCommentRepository
import com.nsnm.herenow.domain.item.repository.ItemRepository
import com.nsnm.herenow.domain.item.repository.LocationRepository
import com.nsnm.herenow.domain.user.model.entity.ProfileEntity
import com.nsnm.herenow.domain.user.repository.ProfileRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Component
@Profile("local")
class DummyDataInitializer(
    private val profileRepository: ProfileRepository,
    private val userGroupRepository: UserGroupRepository,
    private val userGroupMemberRepository: UserGroupMemberRepository,
    private val categoryRepository: CategoryRepository,
    private val locationRepository: LocationRepository,
    private val itemRepository: ItemRepository,
    private val itemCommentRepository: ItemCommentRepository
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun run(args: ApplicationArguments?) {
        if (profileRepository.count() > 0) {
            log.info("Dummy data already initialized.")
            return
        }

        log.info("Initializing dummy data for local/test profile...")

        // 1. 더미 유저 프로필 생성
        val dummyUid = "dummy-uid-1234"
        val profile = ProfileEntity(
            profileId = dummyUid,
            name = "테스트 유저",
            marketingConsent = true
        )

        val dummyMemberUid = "dummy-uid-5678"
        val memberProfile = ProfileEntity(
            profileId = dummyMemberUid,
            name = "테스트 모임원",
            marketingConsent = true
        )

        // 2. 더미 그룹 생성
        val group = UserGroupEntity(
            groupName = "우리집 스페이스",
            ownerProfileId = dummyUid
        )
        userGroupRepository.save(group)

        profile.representativeGroupId = group.groupId
        profileRepository.save(profile)

        val member = UserGroupMemberEntity(
            groupId = group.groupId,
            profileId = dummyUid,
            role = GroupRole.OWNER
        )
        userGroupMemberRepository.save(member)

        memberProfile.representativeGroupId = group.groupId
        profileRepository.save(memberProfile)

        val normalMember = UserGroupMemberEntity(
            groupId = group.groupId,
            profileId = dummyMemberUid,
            role = GroupRole.MEMBER
        )
        userGroupMemberRepository.save(normalMember)

        val groupId = group.groupId

        // 3. 더미 카테고리 생성 (앱에서 기본적으로 필요한 템플릿성 데이터)
        val categories = listOf(
            CategoryEntity(groupId = groupId, categoryName = "가공식품", categoryGroup = "식품 / 음료"),
            CategoryEntity(groupId = groupId, categoryName = "냉동식품", categoryGroup = "식품 / 음료"),
            CategoryEntity(groupId = groupId, categoryName = "간식", categoryGroup = "식품 / 음료"),
            CategoryEntity(groupId = groupId, categoryName = "상비약", categoryGroup = "의약품 / 건강"),
            CategoryEntity(groupId = groupId, categoryName = "영양제", categoryGroup = "의약품 / 건강")
        )
        categoryRepository.saveAll(categories)

        // 4. 더미 장소 생성
        val locations = listOf(
            LocationEntity(groupId = groupId, locationName = "냉장고", locationGroup = "주방 구역"),
            LocationEntity(groupId = groupId, locationName = "팬트리", locationGroup = "주방 구역"),
            LocationEntity(groupId = groupId, locationName = "상부장", locationGroup = "주방 구역"),
            LocationEntity(groupId = groupId, locationName = "TV장", locationGroup = "거실 구역"),
            LocationEntity(groupId = groupId, locationName = "수건장", locationGroup = "욕실 구역")
        )
        locationRepository.saveAll(locations)

        // 5. 더미 아이템 생성 (다양한 유통기한 및 등록일자 셋업)
        val items = mutableListOf<ItemEntity>()
        
        // 카테고리/장소 매핑
        val catFood = categories.find { it.categoryGroup == "식품 / 음료" } ?: categories[0]
        val catMed = categories.find { it.categoryGroup == "의약품 / 건강" } ?: categories[3]
        val locFridge = locations.find { it.locationName == "냉장고" } ?: locations[0]
        val locPantry = locations.find { it.locationName == "팬트리" } ?: locations[1]
        val locMeds = locations.find { it.locationName == "TV장" } ?: locations[3]

        // 이번 달 추가 항목들
        for (i in 1..10) {
            items.add(
                ItemEntity(
                    groupId = groupId,
                    categoryId = catFood.categoryId,
                    locationId = locPantry.locationId,
                    itemName = "가공식품 $i",
                    quantity = i,
                    purchaseDate = LocalDate.now().minusDays(i.toLong()),
                    expiryDate = LocalDate.now().plusMonths(i.toLong()),
                    price = BigDecimal(1000 * i)
                )
            )
        }

        // 임박/만료 항목들 (유통기한 지난 것, D-1, D-3 등)
        items.apply {
            add(
                ItemEntity(
                    groupId = groupId, categoryId = catFood.categoryId, locationId = locFridge.locationId,
                    itemName = "유통기한 지난 우유", quantity = 1, purchaseDate = LocalDate.now().minusMonths(1),
                    expiryDate = LocalDate.now().minusDays(2), price = BigDecimal("3000") // 만료
                )
            )
            add(
                ItemEntity(
                    groupId = groupId, categoryId = catMed.categoryId, locationId = locMeds.locationId,
                    itemName = "타이레놀", quantity = 2, purchaseDate = LocalDate.now().minusYears(1),
                    expiryDate = LocalDate.now().plusDays(1), price = BigDecimal("4500") // 1일 남음
                )
            )
            add(
                ItemEntity(
                    groupId = groupId, categoryId = catFood.categoryId, locationId = locPantry.locationId,
                    itemName = "컵라면 세트", quantity = 6, purchaseDate = LocalDate.now().minusMonths(2),
                    expiryDate = LocalDate.now().plusDays(4), price = BigDecimal("5000") // 4일 남음
                )
            )
        }

        val savedItems = itemRepository.saveAll(items)

        // 6. 더미 코멘트(방명록) 생성
        if (savedItems.isNotEmpty()) {
            val comments = mutableListOf<ItemCommentEntity>()
            // 첫 번째 아이템에 대화형 코멘트 추가
            val targetItem = savedItems[0]
            comments.add(ItemCommentEntity(
                itemId = targetItem.itemId,
                groupId = groupId,
                writerId = dummyUid,
                content = "우유 1개 꺼내먹음!"
            ))
            comments.add(ItemCommentEntity(
                itemId = targetItem.itemId,
                groupId = groupId,
                writerId = dummyMemberUid,
                content = "앗 내일 내가 사올게 ㅎㅎ"
            ))
            
            // 타이레놀(또는 다른 임의 아이템)에 코멘트 추가
            val medItem = savedItems.find { it.itemName == "타이레놀" }
            if (medItem != null) {
                comments.add(ItemCommentEntity(
                    itemId = medItem.itemId,
                    groupId = groupId,
                    writerId = dummyMemberUid,
                    content = "머리아파서 한 알 먹었어~"
                ))
            }
            
            itemCommentRepository.saveAll(comments)
        }

        log.info("Dummy data initialization completed successfully! Created dummy user: \$dummyUid with \${items.size} items.")
    }
}
