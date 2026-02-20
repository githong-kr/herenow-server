package com.nsnm.herenow.lib.model.enum

import com.nsnm.herenow.fwk.core.base.BaseException

enum class ErrorCode(val code: String, val descTemplate: String) {
    NEW9999("NEW9999", "기타 오류입니다."),
    NEW9901("NEW9901", "@Operation 이 없는 API 가 존재합니다."),
    NEW1001("NEW1001", "잘못된 요청입니다: {0}"),
    NEW2001("NEW2001", "로그인이 필요한 요청입니다."),
    NEW3001("NEW3001", "요청한 데이터({0})를 찾을 수 없습니다.");

    fun format(vararg args: List<String>?): String {
        if (args.isEmpty()) return descTemplate

        var formattedDesc = descTemplate
        args.forEachIndexed { index, arg ->
            formattedDesc = formattedDesc.replace("{$index}", arg.toString())
        }
        return formattedDesc
    }

    companion object {
        fun getMessage(exception: BaseException): String {
            val errorCode = fromCode(exception.msgCd) ?: return "알 수 없는 오류입니다. (코드: ${exception.msgCd})"
            return errorCode.format(exception.msgArgs)
        }

        fun fromCode(code: String): ErrorCode? = entries.find { it.code == code }
    }
}
