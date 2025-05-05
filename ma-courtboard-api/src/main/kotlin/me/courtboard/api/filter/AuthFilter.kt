package me.courtboard.api.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import me.courtboard.api.component.JwtProvider
import me.courtboard.api.global.Constants
import me.courtboard.api.global.CourtboardContext
import me.courtboard.api.global.RequestContext
import org.casbin.jcasbin.main.Enforcer
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean


@Component
@Order(1)
class AuthFilter(
    private val jwtProvider: JwtProvider,
    private val enforcer: Enforcer
) : GenericFilterBean() {
    private val logger = LoggerFactory.getLogger(AuthFilter::class.java)

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpServletRequest = request as HttpServletRequest
        val authorization = httpServletRequest.getHeader("Authorization")
        val jwt = authorization?.substringAfter("Bearer ")
        try {
            if (jwt != null) {
                val claims = jwtProvider.getAllClaimsFromToken(jwt)
                val memberId = claims["id"] as String
                val role = enforcer.getRolesForUser(memberId).firstOrNull() ?: Constants.ROLE_USER
                CourtboardContext.setContext(RequestContext(memberId, role))
            } else {
                CourtboardContext.setContext(RequestContext("UNKNOWN", Constants.ROLE_GUEST))
            }
        } catch (e: Exception) {
            logger.error(e.localizedMessage, e)

//            (response as HttpServletResponse).status = HttpServletResponse.SC_UNAUTHORIZED
//            response.contentType = "application/json"
//            response.writer.write(JsonUtil.convertToJsonStr(ApiResult.error("Invalid JWT token")))
            (response as HttpServletResponse).addCookie(Cookie("courtboardJWT", ""))
            CourtboardContext.setContext(RequestContext("UNKNOWN", Constants.ROLE_GUEST))
        } finally {
            chain.doFilter(request, response)
        }
    }
}