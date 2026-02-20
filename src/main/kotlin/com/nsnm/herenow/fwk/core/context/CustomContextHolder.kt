package com.nsnm.herenow.fwk.core.context

object CustomContextHolder {
    // ThreadLocal로 CommonArea 관리
    private val threadLocal: ThreadLocal<CustomContext> = ThreadLocal.withInitial { CustomContext() }

    // CommonArea 세팅하기
    fun setContext(customContext: CustomContext) {
        threadLocal.set(customContext)
    }

    // CommonArea 가져오기
    fun getContext(): CustomContext {
        return threadLocal.get()
    }

    // CommonArea 초기화
    fun clearContext() {
        threadLocal.remove()
    }
}
