package com.nsnm.herenow.fwk.core.base

import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.InitBinder

@Validated
@Transactional
class BaseObject {
    @InitBinder
    fun initBinder(binder: WebDataBinder) {
//        binder.addValidators(CustomValidator())
    }
}