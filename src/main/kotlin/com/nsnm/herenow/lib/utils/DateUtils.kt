package com.nsnm.herenow.lib.utils

import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtils {
    val zoneId: ZoneId = ZoneId.of("Asia/Seoul")

    val now: LocalDateTime
        get() = LocalDateTime.ofInstant(Instant.now(), zoneId)

    // 현재 일시를 지정한 포맷으로 리턴
    fun currentDateTimeFormat(format: String): String = now.format(DateTimeFormatter.ofPattern(format))

    // 현재 일자
    val currentDate: String
        get() = currentDateTimeFormat("yyyy-MM-dd")
    val currentDt: String
        get() = currentDateTimeFormat("yyyyMMdd")

    // TimeStamp(yyyy.MM.dd HH:mm:ss) 리턴
    val currentTmst: Timestamp
        get() = Timestamp.valueOf(now)
    val currentTimestampString: String
        get() = currentDateTimeFormat("yyyy-MM-dd HH:mm:ss")
    val currentHms: String
        get() = currentDateTimeFormat("HHmmss")

    val currentTktm: String
        get() = currentDateTimeFormat("HHmmssSSS")
}