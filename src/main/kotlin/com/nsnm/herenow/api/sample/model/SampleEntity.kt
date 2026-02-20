package com.nsnm.herenow.api.sample.model

import com.nsnm.herenow.lib.model.entity.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "sample_table")
class SampleEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    
    var name: String = "",
    
    var description: String = ""
) : BaseEntity()
