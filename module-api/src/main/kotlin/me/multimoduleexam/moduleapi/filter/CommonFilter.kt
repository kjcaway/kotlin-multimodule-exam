package me.multimoduleexam.moduleapi.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean

@Component
@Order(1)
class CommonFilter : GenericFilterBean() {


    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        val httpServletRequest = request as HttpServletRequest
        if (includeUrlPattern(httpServletRequest)) {
            logger.info("Reqeust method : ${httpServletRequest.method}, url : ${httpServletRequest.requestURL}")
        }
        chain?.doFilter(request, response)
    }

    private fun includeUrlPattern(httpServletRequest: HttpServletRequest): Boolean {
        val uri = httpServletRequest.requestURI
        if (uri.startsWith("/api")) {
            return true
        }
        return false
    }
}