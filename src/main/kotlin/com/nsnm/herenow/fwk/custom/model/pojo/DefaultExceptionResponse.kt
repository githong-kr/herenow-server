package com.nsnm.herenow.fwk.custom.model.pojo

data class DefaultExceptionResponse(
    var messageCode: String? = null,
    var message: String? = null,
    var type: String = "S",
    var classType: String? = null,
    var serviceName: String? = null,
    var lineNumber: Int? = null,
    var methodName: String? = null
)

