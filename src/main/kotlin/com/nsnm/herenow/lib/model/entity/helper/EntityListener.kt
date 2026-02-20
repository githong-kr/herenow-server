package com.nsnm.herenow.lib.model.entity.helper

import com.nsnm.herenow.fwk.core.context.CustomContextHolder
import com.nsnm.herenow.lib.ext.logger
import com.nsnm.herenow.lib.model.entity.base.BaseEntity
import com.nsnm.herenow.lib.utils.DateUtils
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.time.LocalDateTime

class EntityListener {

    private val log = logger()

    @PrePersist
    fun prePersist(entity: BaseEntity) {
        val currentTimestamp = DateUtils.now
        val context = CustomContextHolder.getContext()
        updateEntityFields(entity, null, context.com.guid, currentTimestamp, isCreate = true)
        log.info("PrePersist called for entity: $entity")
    }

    @PreUpdate
    fun preUpdate(entity: BaseEntity) {
        val currentTimestamp = DateUtils.now
        val context = CustomContextHolder.getContext()
        updateEntityFields(entity, null, context.com.guid, currentTimestamp, isCreate = false)
        log.info("PreUpdate called for entity: $entity")
    }

    private fun updateEntityFields(
        entity: BaseEntity,
        apiId: Long? = 0,
        guid: String,
        currentTimestamp: LocalDateTime,
        isCreate: Boolean
    ) {
        if (isCreate) {
            // entity.frstRegApiId = apiId  // 삭제됨
            entity.frstRegTmst = currentTimestamp
            entity.frstRegGuid = guid
        }
        // entity.lastChngApiId = apiId  // 삭제됨
        entity.lastChngTmst = currentTimestamp
        entity.lastChngGuid = guid
    }
}
