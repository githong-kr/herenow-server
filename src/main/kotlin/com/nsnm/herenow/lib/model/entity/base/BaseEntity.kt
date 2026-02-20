package com.nsnm.herenow.lib.model.entity.base

import com.nsnm.herenow.lib.model.entity.helper.EntityListener
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.Comment
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(EntityListener::class)
abstract class BaseEntity {
    @Column(name = "frst_reg_api_id", nullable = true)
    @Comment("최초등록API")
    open var frstRegApiId: Long? = 0

    @Column(name = "frst_reg_tmst", nullable = true)
    @Comment("최초등록일시")
    open var frstRegTmst: LocalDateTime? = null

    @Column(name = "frst_reg_guid", nullable = true)
    @Comment("최초등록Guid")
    open var frstRegGuid: String? = null

    @Column(name = "last_chng_api_id", nullable = true)
    @Comment("최종수정API")
    open var lastChngApiId: Long? = 0

    @Column(name = "last_chng_tmst", nullable = true)
    @Comment("최종수정일시")
    open var lastChngTmst: LocalDateTime? = null

    @Column(name = "last_chng_guid", nullable = true)
    @Comment("최종수정Guid")
    open var lastChngGuid: String? = null
}
