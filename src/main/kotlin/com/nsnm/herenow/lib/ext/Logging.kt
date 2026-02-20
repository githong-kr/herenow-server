package com.nsnm.herenow.lib.ext

import mu.KLogger
import mu.KotlinLogging

inline fun <reified T> T.logger(): KLogger {
    val loggerName = if (T::class.isCompanion) {
        T::class.java.enclosingClass?.name ?: T::class.java.name
    } else {
        T::class.java.name
    }

    // 프록시 클래스인지 확인하여 실제 클래스 이름 설정
    val resolvedLoggerName = loggerName.takeIf { !it.contains("\$Proxy") } ?: this!!::class.java.name

    return KotlinLogging.logger(resolvedLoggerName)
}