package com.nsnm.herenow.fwk.custom.model.pojo

import com.nsnm.herenow.lib.utils.DateUtils
import java.io.Serializable
import java.time.OffsetDateTime

data class ComArea(
    var guid: String = "",                              // global id
    var method: String = "",                            // Rest Method
    var path: String = "",                              // API URL
    var pathPattern: String = "",                       // API URL Pattern (RequestMapping 에 선언한 원본 url pattern)
    var apiKey: String? = "",                           // API Key
    var statCd: String = "200",                         // 상태코드
    var startDt: OffsetDateTime? = null,                // 거래 시작시간
    var endDt: OffsetDateTime? = null,                  // 거래 종료시간
    var elapsed: Long = 0L,                             // elapsed
    var remoteIp: String = "",                          // 호출지 IP
    var isLogin: Boolean = false,                       // 로그인 여부
    var errMsg: String? = null,                         // 에러 메시지
    var err: Exception? = null,                         // Exception
    var apiNm: String? = null                           // API Name
) : Serializable {

    override fun toString(): String {
        return "ComArea(guid='$guid', method='$method', path='$path', pathPattern='$pathPattern', apiKey='$apiKey', statCd='$statCd', startDt=$startDt, endDt=$endDt, elapsed=$elapsed, remoteIp='$remoteIp', isLogin=$isLogin, errMsg=$errMsg, err=$err, apiNm=$apiNm)"
    }
}
