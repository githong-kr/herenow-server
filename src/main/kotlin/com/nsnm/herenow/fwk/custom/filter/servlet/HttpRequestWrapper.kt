package com.nsnm.herenow.fwk.custom.filter.servlet


import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import org.springframework.util.StreamUtils
import java.io.*
import java.util.*

/**
 * 커스텀 HttpRequest
 * - 헤더 추가/삭제 가능
 * - 스트림 재사용 가능
 */
class HttpRequestWrapper @Throws(IOException::class)
constructor(request: HttpServletRequest) : HttpServletRequestWrapper(request) {

    private var headerMap: MutableMap<String, String> = HashMap()
    private val bodyData: ByteArray

    init {
        val inputStream = super.getInputStream()
        bodyData = StreamUtils.copyToByteArray(inputStream)
    }

    fun addHeader(name: String, value: String) {
        headerMap[name] = value
    }

    override fun getHeader(name: String): String? {
        return if (headerMap.containsKey(name))
            headerMap[name]
        else
            super.getHeader(name)
    }

    override fun getHeaderNames(): Enumeration<String> {
        val names = super.getHeaderNames().toList() as MutableList<String>
        names.addAll(headerMap.keys)
        return Collections.enumeration(names)
    }

    override fun getHeaders(name: String): Enumeration<String> {
        val values = super.getHeaders(name).toList() as MutableList<String>
        if (headerMap.containsKey(name))
            headerMap[name]?.let { values.add(it) }
        return Collections.enumeration(values)
    }

    override fun getRemoteAddr(): String {
        return getHeader("real-ip")?.let {
            getHeader("real-ip")
        }.let {
            super.getRemoteAddr()
        }
    }

    @Throws(IOException::class)
    override fun getInputStream(): ServletInputStream = ServletInputStreamImpl(ByteArrayInputStream(bodyData))

    @Throws(IOException::class)
    override fun getReader(): BufferedReader = BufferedReader(InputStreamReader(ByteArrayInputStream(bodyData)))

    internal inner class ServletInputStreamImpl(private val inputStream: InputStream) : ServletInputStream() {

        @Throws(IOException::class)
        override fun read(): Int = inputStream.read()

        @Throws(IOException::class)
        override fun read(b: ByteArray): Int = inputStream.read(b)

        override fun isFinished(): Boolean {
            TODO("Not yet implemented")
        }

        override fun isReady(): Boolean {
            TODO("Not yet implemented")
        }

        override fun setReadListener(p0: ReadListener?) {
            TODO("Not yet implemented")
        }

    }

}
