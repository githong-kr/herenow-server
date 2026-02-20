package com.nsnm.herenow.api.sample.repository

import com.nsnm.herenow.api.sample.model.SampleEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SampleRepository : JpaRepository<SampleEntity, Long>
