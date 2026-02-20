package com.nsnm.herenow.fwk.custom.config

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackages = ["com.nsnm.herenow.newlifeserver", "com.nsnm.herenow.lib", "com.nsnm.herenow.fwk"])
@ConfigurationPropertiesScan("com.nsnm.herenow")
class ScanConfig {
}
