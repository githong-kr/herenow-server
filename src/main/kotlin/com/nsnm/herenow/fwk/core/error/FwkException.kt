package com.nsnm.herenow.fwk.core.error

import com.nsnm.herenow.fwk.core.base.BaseException
import org.springframework.http.HttpStatus

/**
 * 프레임워크 에러일 경우 사용
 */
open class FwkException(
    override val httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    override val msgCd: String,
    e: Throwable? = null,
    override val msgArgs: List<String>? = null
) : BaseException(httpStatus, msgCd, e) {
    constructor(e: Throwable) : this(HttpStatus.INTERNAL_SERVER_ERROR, "NEW9999", e)
    constructor(e: Throwable, msgCd: String) : this(HttpStatus.INTERNAL_SERVER_ERROR, msgCd, e)
    constructor(e: Throwable, msgCd: String, msgArgs: List<String>?) : this(HttpStatus.INTERNAL_SERVER_ERROR, msgCd, e, msgArgs)
    constructor(msgCd: String) : this(HttpStatus.INTERNAL_SERVER_ERROR, msgCd)
    constructor(msgCd: String, msgArgs: List<String>?) : this(HttpStatus.INTERNAL_SERVER_ERROR, msgCd, msgArgs = msgArgs)
}