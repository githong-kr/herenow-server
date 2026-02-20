package com.nsnm.herenow.lib.model.repository.log

import com.nsnm.herenow.lib.model.entity.log.ApiCallLogEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ApiCallLogRepository : JpaRepository<ApiCallLogEntity, Long>
