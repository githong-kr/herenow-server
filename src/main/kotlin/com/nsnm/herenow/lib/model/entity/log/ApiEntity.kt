package com.nsnm.herenow.lib.model.entity.log

import com.nsnm.herenow.lib.model.entity.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "api_mst")
class ApiEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var apiId: Long? = null,

    @Column(name = "http_method_nm", length = 10, nullable = false)
    var httpMethodNm: String = "",

    @Column(name = "url_path", length = 200, nullable = false)
    var urlPath: String = "",

    @Column(name = "class_nm", length = 100)
    var classNm: String = "",

    @Column(name = "method_nm", length = 100)
    var methodNm: String = "",

    @Column(name = "api_nm", length = 200)
    var apiNm: String = "",

    @Column(name = "api_desc", length = 1000)
    var apiDesc: String? = null,

    @Column(name = "use_yn", length = 1, nullable = false)
    var useYn: String = "Y"
) : BaseEntity()
