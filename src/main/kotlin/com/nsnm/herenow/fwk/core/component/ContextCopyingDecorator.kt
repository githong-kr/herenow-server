package com.nsnm.herenow.fwk.core.component

import com.nsnm.herenow.fwk.core.context.CustomContextHolder
import com.nsnm.herenow.lib.ext.logger
import org.slf4j.MDC
import org.springframework.core.env.Environment
import org.springframework.core.task.TaskDecorator

class ContextCopyingDecorator(
    val env: Environment
) : TaskDecorator {
    private val log = logger()

    override fun decorate(runnable: Runnable): Runnable {
        val ctx = CustomContextHolder.getContext()

        return Runnable {
            try {
                MDC.put("guid", ctx.com.guid)
                MDC.put("apiKey", (ctx.com.apiId ?: "").toString())
                CustomContextHolder.setContext(ctx)
                runnable.run()
            } finally {
                CustomContextHolder.clearContext()
                MDC.clear()
            }
        }
    }
}
