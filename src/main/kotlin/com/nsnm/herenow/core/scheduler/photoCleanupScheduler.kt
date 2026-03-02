package com.nsnm.herenow.core.scheduler

import com.nsnm.herenow.core.supabase.SupabaseStorageClient
import com.nsnm.herenow.domain.item.repository.ItemPhotoRepository
import com.nsnm.herenow.domain.item.repository.LocationRepository
import com.nsnm.herenow.domain.user.repository.ProfileRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class photoCleanupScheduler(
    private val supabaseStorageClient: SupabaseStorageClient,
    private val itemPhotoRepository: ItemPhotoRepository,
    private val locationRepository: LocationRepository,
    private val profileRepository: ProfileRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // 매일 새벽 3시에 실행
    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupOrphanPhotos() {
        log.info("고아 이미지 청소 스케줄러 시작...")
        
        val bucketsToClear = listOf("items", "locations", "profiles")
        
        for (bucketName in bucketsToClear) {
            val dbPhotoUrls = when (bucketName) {
                "items" -> itemPhotoRepository.findAllPhotoUrls()
                "locations" -> locationRepository.findAllPhotoUrls()
                "profiles" -> profileRepository.findAllAvatarUrls()
                else -> emptyList()
            }.toSet()

            // URL에서 파일명만 추출
            val dbFileNames = dbPhotoUrls.map { extractFileName(it) }.filter { it.isNotBlank() }.toSet()
            val storageItems = supabaseStorageClient.listAllObjectsRecursive(bucketName)
            println("dbFileNames : $dbFileNames")
            println("storageItems : $storageItems")
            val now = Instant.now()
            val objectsToDelete = mutableListOf<String>()

            for (item in storageItems) {
                val fullPath = item.name
                val pureFileName = if (fullPath.contains("/")) fullPath.substringAfterLast("/") else fullPath

                if (fullPath.isBlank() || pureFileName == ".emptyFolderPlaceholder") continue

                val createdAtStr = item.created_at
                if (createdAtStr != null) {
                    try {
                        val createdAt = Instant.parse(createdAtStr)
                        // 생성된 지 24시간이 지난 파일 탐색
                        if (createdAt.isBefore(now.minus(24, ChronoUnit.HOURS))) {
                            if (!dbFileNames.contains(pureFileName)) {
                                objectsToDelete.add(fullPath) // 삭제 시에는 폴더가 포함된 전체 경로를 던져야 함
                            }
                        }
                    } catch (e: Exception) {
                        log.warn("Storage 날짜 파싱 오류 (버킷: $bucketName, 파일: $fullPath): ${e.message}")
                    }
                }
            }

            if (objectsToDelete.isNotEmpty()) {
                log.info("버킷 '$bucketName'에서 ${objectsToDelete.size} 개의 고아 이미지를 발견하여 삭제를 진행합니다.")
                log.debug("대상 파일 목록: $objectsToDelete")
                supabaseStorageClient.deleteObjects(bucketName, objectsToDelete)
            } else {
                log.info("버킷 '$bucketName'에는 삭제가 필요한 고아 이미지가 없습니다.")
            }
        }
    }

    private fun extractFileName(url: String): String {
        return url.substringAfterLast("/")
    }
}
