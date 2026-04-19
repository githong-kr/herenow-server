package com.nsnm.herenow.fwk.custom.config

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 로컬(H2) 환경에서만 동작하는 더미 데이터 초기화.
 * 신규 Entity 작성 완료 후 재작성 예정.
 */
@Component
@Profile("local")
class DummyDataInitializer : ApplicationRunner {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun run(args: ApplicationArguments?) {
        log.info("DummyDataInitializer: skipped (pending new entity migration)")
    }
}
