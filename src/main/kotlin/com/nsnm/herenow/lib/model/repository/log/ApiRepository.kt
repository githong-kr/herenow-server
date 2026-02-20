package com.nsnm.herenow.lib.model.repository.log

import com.nsnm.herenow.lib.model.entity.log.ApiEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ApiRepository : JpaRepository<ApiEntity, Long>
