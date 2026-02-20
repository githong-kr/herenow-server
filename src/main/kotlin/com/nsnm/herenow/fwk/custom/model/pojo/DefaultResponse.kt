package com.nsnm.herenow.fwk.custom.model.pojo

import com.nsnm.herenow.fwk.core.context.CustomContext
import com.nsnm.herenow.lib.utils.DateUtils
import org.springframework.http.HttpStatus
import java.sql.Timestamp

class DefaultResponse {

    companion object {
        fun of(input: Any?): Response {
            return Response(input)
        }

        fun of(input: Any?, context: CustomContext): Response {
            return Response(input, context)
        }

    }

    data class Response(
        var guid: String = "",
        var status: String = "",
        var message: String? = null,
        var data: Any? = null,
        var errorInfo: DefaultExceptionResponse? = null,
    ) {
        constructor(input: Any?) : this() {
            if (input is DefaultExceptionResponse) {
                this.errorInfo = input
                this.message = input.message
            } else {
                this.data = input
            }
        }

        constructor(input: Any?, context: CustomContext) : this() {
            if (input is DefaultExceptionResponse) {
                this.errorInfo = input
                this.message = input.message
            } else {
                this.data = input
            }

            this.fillCommon(context)
        }

        private fun fillCommon(context: CustomContext): Response {
            val ca = context.com
            this.guid = ca.guid
            this.status = ca.statCd
            return this
        }

        override fun toString(): String {
            return "Response(guid='$guid', status='$status', message=$message, data=$data, errorInfo=$errorInfo)"
        }
    }
}
