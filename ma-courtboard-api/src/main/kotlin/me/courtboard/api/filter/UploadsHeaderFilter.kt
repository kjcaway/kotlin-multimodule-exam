package me.courtboard.api.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

// /uploads 정적 리소스 응답에 보안 헤더(nosniff, CSP, Content-Disposition: inline)를 강제한다.
// 매직 바이트 검증을 통과해도 이중 안전장치로 두어 브라우저의 MIME sniffing 및
// 비이미지 컨텐츠 실행 가능성을 차단한다.
@Component
@Order(2)
class UploadsHeaderFilter : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        if (request.requestURI.startsWith("/uploads/")) {
            response.setHeader("X-Content-Type-Options", "nosniff")
            response.setHeader("Content-Security-Policy", "default-src 'none'; img-src 'self' data:; style-src 'unsafe-inline'")
            response.setHeader("Content-Disposition", "inline")
        }
        chain.doFilter(request, response)
    }
}
