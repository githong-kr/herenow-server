package com.nsnm.herenow.fwk.core.error

import com.nsnm.herenow.fwk.core.base.BaseException
import org.springframework.http.HttpStatus

/**
 * 비즈니스 에러일 경우 사용
 */
open class BizException(
    override val httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    override val msgCd: String,
    e: Throwable? = null,
    override val msgArgs: List<String>? = null
) : BaseException(httpStatus, msgCd, e) {
    constructor(e: Throwable, msgCd: String) : this(HttpStatus.INTERNAL_SERVER_ERROR, msgCd, e)
    constructor(e: Throwable, msgCd: String, msgArgs: List<String>?) : this(
        HttpStatus.INTERNAL_SERVER_ERROR,
        msgCd,
        e,
        msgArgs
    )

    constructor(msgCd: String) : this(HttpStatus.INTERNAL_SERVER_ERROR, msgCd)
    constructor(msgCd: String, msgArgs: List<String>?) : this(
        HttpStatus.INTERNAL_SERVER_ERROR,
        msgCd,
        msgArgs = msgArgs
    )
}
