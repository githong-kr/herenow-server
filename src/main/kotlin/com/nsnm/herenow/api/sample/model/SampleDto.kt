package com.nsnm.herenow.api.sample.model

data class SampleResponse(
    val id: Long,
    val name: String,
    val description: String
)

data class SampleRequest(
    val name: String,
    val description: String
)
