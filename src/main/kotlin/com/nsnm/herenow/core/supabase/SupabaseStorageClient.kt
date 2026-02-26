package com.nsnm.herenow.core.supabase

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class SupabaseStorageClient(
    @Value("\${supabase.url:}") private val supabaseUrl: String,
    @Value("\${supabase.secret-key:}") private val supabaseSecretKey: String,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restTemplate = RestTemplate()

    private fun getHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer $supabaseSecretKey")
        headers.set("apikey", supabaseSecretKey)
        headers.contentType = MediaType.APPLICATION_JSON
        return headers
    }

    data class StorageObject(
        val name: String,
        val id: String?,
        val updated_at: String?,
        val created_at: String?,
        val last_accessed_at: String?,
        val metadata: Map<String, Any>?
    )

    fun listObjects(bucketName: String, prefix: String = "", limit: Int = 100, offset: Int = 0): List<StorageObject> {
        if (!isValidUrl(supabaseUrl) || supabaseSecretKey.isBlank()) {
            log.warn("Supabase 환경 변수(URL: $supabaseUrl, SecretKey)가 누락되거나 잘못되어 Storage 접속을 건너뜁니다.")
            return emptyList()
        }

        val requestBody = mapOf(
            "prefix" to prefix,
            "limit" to limit,
            "offset" to offset,
            "sortBy" to mapOf("column" to "name", "order" to "asc")
        )

        return try {
            val entity = HttpEntity(requestBody, getHeaders())
            val response = restTemplate.exchange(
                "$supabaseUrl/storage/v1/object/list/$bucketName",
                HttpMethod.POST,
                entity,
                String::class.java
            )

            val responseBody = response.body
            if (responseBody.isNullOrBlank()) {
                emptyList()
            } else {
                objectMapper.readValue(
                    responseBody,
                    objectMapper.typeFactory.constructCollectionType(List::class.java, StorageObject::class.java)
                )
            }
        } catch (e: Exception) {
            log.error("Supabase Storage 객체 목록 조회 중 에러 발생 (버킷: $bucketName): ${e.message}", e)
            emptyList()
        }
    }

    /**
     * 지정된 버킷의 모든 객체(폴더 내부 포함)를 재귀적으로 조회하여 파일 목록만 반환합니다.
     */
    fun listAllObjectsRecursive(bucketName: String, initialPrefix: String = ""): List<StorageObject> {
        val allFiles = mutableListOf<StorageObject>()
        val prefixesToExplore = ArrayDeque<String>()
        prefixesToExplore.add(initialPrefix)

        while (prefixesToExplore.isNotEmpty()) {
            val currentPrefix = prefixesToExplore.removeFirst()
            var offset = 0
            val limit = 100

            while (true) {
                val items = listObjects(bucketName, currentPrefix, limit, offset)
                for (item in items) {
                    // id가 null이거나 metadata가 비어있으면 폴더로 취급 (단독 파일이 아님)
                    if (item.id == null || item.name.endsWith("/")) {
                        if (item.name.isNotBlank() && item.name != ".emptyFolderPlaceholder") {
                            val nextPrefix = if (currentPrefix.isEmpty()) "${item.name}/" else "$currentPrefix${item.name}/"
                            prefixesToExplore.add(nextPrefix)
                        }
                    } else if (item.name != ".emptyFolderPlaceholder") {
                        // 파일인 경우 경로 정보를 포함한 이름으로 교체하여 결과셋에 저장
                        val fullPathName = if (currentPrefix.isEmpty()) item.name else "$currentPrefix${item.name}"
                        allFiles.add(item.copy(name = fullPathName))
                    }
                }
                
                if (items.size < limit) break
                offset += limit
                if (offset > 100000) break // 무한 루프 안전장치
            }
        }
        return allFiles
    }

    fun deleteObjects(bucketName: String, fileNames: List<String>): Boolean {
        if (fileNames.isEmpty()) return true
        if (!isValidUrl(supabaseUrl) || supabaseSecretKey.isBlank()) return false

        val requestBody = mapOf("prefixes" to fileNames)

        return try {
            val entity = HttpEntity(requestBody, getHeaders())
            restTemplate.exchange(
                "$supabaseUrl/storage/v1/object/$bucketName",
                HttpMethod.DELETE,
                entity,
                String::class.java
            )
            true
        } catch (e: Exception) {
            log.error("Supabase Storage 객체 삭제 중 에러 발생 (버킷: $bucketName): ${e.message}", e)
            false
        }
    }

    @Async
    fun deleteObjectsAsync(bucketName: String, fileNames: List<String>) {
        if (fileNames.isEmpty()) return
        log.info("비동기 Storage 객체 삭제 요청 - 버킷: $bucketName, 파일 수: ${fileNames.size}")
        deleteObjects(bucketName, fileNames)
    }

    /**
     * Public URL (https://.../storage/v1/object/public/{bucketName}/{fileName})에서
     * 실제 스토리지 Delete API 구동을 위한 fileName 경로만 추출하여 반환합니다.
     */
    fun extractFilePathFromUrl(publicUrl: String, bucketName: String): String? {
        val token = "/public/\$bucketName/"
        val index = publicUrl.indexOf(token)
        if (index == -1) return null
        return java.net.URLDecoder.decode(publicUrl.substring(index + token.length), "UTF-8")
    }

    private fun isValidUrl(url: String): Boolean {
        if (url.isBlank()) return false
        // 환경변수 주입에 실패하여 'https://.supabase.co' 같이 치환된 경우 방지
        return url.startsWith("http://") || (url.startsWith("https://") && !url.contains("://."))
    }
}
