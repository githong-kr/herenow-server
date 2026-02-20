package com.nsnm.herenow.lib.model.entity.log

import com.nsnm.herenow.lib.model.entity.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "api_call_log")
class ApiCallLogEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    
    @Column(name = "req_guid", length = 50)
    var reqGuid: String? = null,
    
    @Column(name = "api_key", length = 50)
    var apiKey: String? = null,
    
    @Column(name = "service_nm", length = 100)
    var serviceNm: String? = null,
    
    @Column(name = "req_url", length = 200)
    var reqUrl: String? = null,
    
    @Column(name = "client_ip", length = 50)
    var clientIp: String? = null,
    
    @Column(name = "req_param", columnDefinition = "TEXT")
    var reqParam: String? = null,
    
    @Column(name = "req_body", columnDefinition = "TEXT")
    var reqBody: String? = null,
    
    @Column(name = "elapsed_ms")
    var elapsedMs: Long? = 0,
    
    @Column(name = "err_yn", length = 1)
    var errYn: String = "N",
    
    @Column(name = "err_msg", length = 1000)
    var errMsg: String? = null,
    
    @Column(name = "err_stack", columnDefinition = "TEXT")
    var errStack: String? = null
) : BaseEntity()
