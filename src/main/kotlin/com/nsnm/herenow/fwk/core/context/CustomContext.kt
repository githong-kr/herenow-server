package com.nsnm.herenow.fwk.core.context

import com.nsnm.herenow.fwk.custom.model.pojo.ComArea
import com.nsnm.herenow.fwk.custom.model.pojo.ComPage
import com.nsnm.herenow.fwk.custom.model.pojo.ComUser
import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.Serializable

data class CustomContext(
    var com: ComArea = ComArea(),
    @JsonIgnore var user: ComUser = ComUser(),
    var page: ComPage = ComPage(),
    var headers: MutableMap<String, String> = mutableMapOf()
) : Serializable, Cloneable {
    override fun toString(): String {
        return "CustomContext(com=$com, user=$user, page=$page)"
    }
}
