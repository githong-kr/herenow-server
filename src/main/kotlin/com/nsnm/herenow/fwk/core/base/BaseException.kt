package com.nsnm.herenow.fwk.core.base

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatus

@JsonInclude(JsonInclude.Include.NON_NULL)
open class BaseException(
    open val httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    open val msgCd: String,
    e: Throwable? = null,
    open val msgArgs: List<String>? = null,
    open val fieldName: String? = null
) : RuntimeException(msgCd, e) {
    constructor(e: Throwable, msgCd: String) : this(httpStatus = HttpStatus.INTERNAL_SERVER_ERROR, msgCd = msgCd, e = e, fieldName = null)
}