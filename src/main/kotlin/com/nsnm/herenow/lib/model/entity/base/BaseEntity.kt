package com.nsnm.herenow.lib.model.entity.base

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    
    @CreatedBy
    @Column(name = "frst_reg_guid", nullable = true, updatable = false)
    @Comment("최초등록Guid")
    open var frstRegGuid: String? = null

    @CreatedDate
    @Column(name = "frst_reg_tmst", nullable = true, updatable = false)
    @Comment("최초등록일시")
    open var frstRegTmst: LocalDateTime? = null

    @LastModifiedBy
    @Column(name = "last_chng_guid", nullable = true)
    @Comment("최종수정Guid")
    open var lastChngGuid: String? = null

    @LastModifiedDate
    @Column(name = "last_chng_tmst", nullable = true)
    @Comment("최종수정일시")
    open var lastChngTmst: LocalDateTime? = null
}
