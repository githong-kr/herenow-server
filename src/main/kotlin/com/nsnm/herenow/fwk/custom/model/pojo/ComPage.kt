package com.nsnm.herenow.fwk.custom.model.pojo

import java.io.Serializable

data class ComPage(
    var pageNo: Int = 1,            // 페이지 번호
    var pageSize: Int = 20,         // 페이지 최대개수
    var total: Int = 0,             // 총 건수
) : Serializable