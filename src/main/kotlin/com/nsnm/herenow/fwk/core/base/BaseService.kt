package com.nsnm.herenow.fwk.core.base

import com.nsnm.herenow.fwk.core.context.CustomContext
import com.nsnm.herenow.fwk.core.context.CustomContextHolder
import com.nsnm.herenow.lib.ext.logger
import org.springframework.stereotype.Component

@Component
class BaseService {
    protected val log
        get() = logger()
    protected val context: CustomContext
        get() = CustomContextHolder.getContext()

//    protected val env: Environment = Environment
}