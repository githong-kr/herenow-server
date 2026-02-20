package com.nsnm.herenow.fwk.core.utils

import jakarta.annotation.PostConstruct
import org.springframework.beans.BeanWrapper
import org.springframework.beans.BeanWrapperImpl
import org.springframework.beans.InvalidPropertyException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class BeanUtils:ApplicationContextAware {
    companion object {
        private lateinit var instance: BeanUtils
        private lateinit var context: ApplicationContext
        fun <T> getBean(bean: Class<T>) = instance.getBean(bean)

        fun copyProperties(source: Any, target: Any) {
            return org.springframework.beans.BeanUtils.copyProperties(source, target, *getNullPropertyNames(source))
        }

        fun <T:Any> copyAndReturn(source: Any, target: T): T {
            org.springframework.beans.BeanUtils.copyProperties(source, target, *getNullPropertyNames(source))
            return target
        }

        private fun getNullPropertyNames(source: Any): Array<String> {
            val src: BeanWrapper = BeanWrapperImpl(source)
            val pds = src.propertyDescriptors
            val emptyNames: MutableSet<String> = HashSet()
            for (pd in pds) {
                try {
                    val srcValue = src.getPropertyValue(pd.name)
                    if(srcValue == null) {
                        emptyNames.add(pd.name)
                    }
                } catch (e: InvalidPropertyException) {
                    emptyNames.add(pd.name)
                }
            }
            return emptyNames.toTypedArray<String>()
        }
    }

    @PostConstruct
    fun init() {
        instance = this
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }

    fun <T> getBean(bean:Class<T>) = context.getBean(bean)
}