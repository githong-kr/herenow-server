package com.nsnm.herenow.api.sample.model

import java.time.LocalDateTime

data class SampleResponse(
    val id: Long,
    val name: String,
    val description: String,
    val frstRegTmst: LocalDateTime? = null,
    val frstRegGuid: String? = null,
    val lastChngTmst: LocalDateTime? = null,
    val lastChngGuid: String? = null
)

data class SampleRequest(
    val name: String,
    val description: String
)
