package com.nsnm.herenow.fwk.custom.model.pojo

import com.nsnm.herenow.lib.utils.DateUtils
import java.io.Serializable
import java.time.OffsetDateTime

data class ComArea(
    var appName: String = "",                           // application 명
    var appVer: String = "",                            // application version
    var trDt: String = "",                              // 거래일자(yyMMdd)
    var guid: String = "",                              // global id
    var guidLast: String = "",                          // last guid
    var method: String = "",                            // Rest Method
    var apiId: Long = 0,                               // API ID
    var apiNm: String? = "",                            // API Name
    var path: String = "",                              // API URL
    var pathPattern: String = "",                       // API URL Pattern (RequestMapping 에 선언한 원본 url pattern)
    var apiKey: String? = "",                           // API Key
    var isHttps: Boolean? = false,                       // HTTPS 여부
    var statCd: String = "200",                         // 상태코드
    var startDt: OffsetDateTime? = null,                // 거래 시작시간
    var endDt: OffsetDateTime? = null,                  // 거래 종료시간
    var elapsed: Long = 0L,                             // elapsed
    var hostNm: String = "",                            // 호스트명
    var remoteIp: String = "",                          // 호출지 IP
    var queryString: String? = null,                    // 입력 쿼리스트링
    var body: String? = null,                           // 입력 바디
    var isLogin: Boolean = false,                       // 로그인 여부
    var errMsg: String? = null,                         // 에러 메시지
    var err: Exception? = null,                         // Exception
    var profile: String? = null,                        // 스프링 프로파일
    val startupTime: Any = Companion.startupTime,       // 기동시간
    var referrer: String? = null,                       // 호출지 URL
    var isDev: Boolean = true,                           // 로컬도 true
    var isLocal: Boolean = true,                         // 로컬만 true
    var isTest: Boolean = false,                         // 테스트 여부
    var isPrd: Boolean = false,                          // 운영계 여부
    var isBatch: Boolean = false,                        // 배치 여부
) : Serializable {
    companion object {
        val startupTime = DateUtils.currentTimestampString // 컨테이너 기동시간
    }

    override fun toString(): String {
        return "ComArea(appName='$appName', appVer='$appVer', guid='$guid', guidLast='$guidLast', date='$trDt', method='$method', apiId='$apiId', apiNm='$apiNm', path='$path', pathPattern='$pathPattern', apiKey='$apiKey', statCd='$statCd', startDt=$startDt, endDt=$endDt, elapsed=$elapsed, hostName='$hostNm', remoteIp='$remoteIp', queryString=$queryString, body=$body, isLogin=$isLogin, errMsg=$errMsg, err=$err, profile=$profile, startupTime=$startupTime, referrer=$referrer, isDev=$isDev, isLocal=$isLocal, isTest=$isTest, isPrd=$isPrd, isBatch=$isBatch)"
    }

}
