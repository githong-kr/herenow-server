package com.nsnm.herenow.fwk.custom.filter.aop

object PointcutList {
    const val SERVICE_LAYER = "within(com.nsnm.herenow..*) && target(com.nsnm.herenow.fwk.core.base.BaseService)"
    const val CONTROLLER_LAYER = "within(com.nsnm.herenow..*) && target(com.nsnm.herenow.fwk.core.base.BaseController)"
}