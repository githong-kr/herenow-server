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
        val tmst: Timestamp = DateUtils.currentTmst,
        val timeStamp: String = DateUtils.currentTimestampString,
        var guid: String = "",
        var status: String = "",
        var statusName: String = "",
        var path: String = "",
        var api: Long? = 0,
        var isError: Boolean = false,
        var data: Any? = null,
        var errorInfo: DefaultExceptionResponse? = null,
    ) {
        constructor(input: Any?) : this() {
            if (input is DefaultExceptionResponse)
                this.errorInfo = input
            else
                this.data = input
        }

        constructor(input: Any?, context: CustomContext) : this() {
            if (input is DefaultExceptionResponse)
                this.errorInfo = input
            else
                this.data = input

            this.fillCommon(context)
        }

        private fun fillCommon(context: CustomContext): Response {
            val ca = context.com
            this.guid = ca.guid
            this.status = ca.statCd
            this.statusName = HttpStatus.valueOf(ca.statCd.toInt()).name
            this.path = ca.path
            this.api = ca.apiId
            this.isError = this.errorInfo != null

            if (this.status == "404")
                this.isError = true

            return this
        }

        override fun toString(): String {
            return "Response(tmst=$tmst, timeStamp='$timeStamp', guid='$guid', status='$status', statusName='$statusName', path='$path', api='$api', isError=$isError, data=$data, errorInfo=$errorInfo)"
        }

    }
}
