package me.courtboard.api.filter

import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import me.courtboard.api.component.JwtProvider
import me.courtboard.api.global.Constants
import me.courtboard.api.global.CourtboardContext
import me.courtboard.api.global.RequestContext
import me.courtboard.api.global.dto.ApiResult
import me.multimoduleexam.util.JsonUtil
import org.casbin.jcasbin.main.Enforcer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter


@Component
@Order(1)
class AuthFilter(
    private val jwtProvider: JwtProvider,
    private val enforcer: Enforcer,
    @Value("\${app.allowed-origin}")
    val allowedOrigin: String,
) : OncePerRequestFilter() {
    private val logger = LoggerFactory.getLogger(AuthFilter::class.java)

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val authorization = request.getHeader("Authorization")
        val jwt = authorization?.substringAfter("Bearer ")
        try {
            if (jwt != null) {
                val claims = jwtProvider.getAllClaimsFromToken(jwt)
                val memberId = claims["id"] as String
                val role = enforcer.getRolesForUser(memberId).firstOrNull() ?: Constants.ROLE_USER
                CourtboardContext.setContext(RequestContext(memberId, role))
            } else {
                CourtboardContext.setContext(RequestContext(Constants.GUEST_ID, Constants.ROLE_GUEST))
            }

            chain.doFilter(request, response)
        } catch (e: ExpiredJwtException) {
            logger.error(e.localizedMessage, e)

            response.setHeader("Access-Control-Allow-Origin", allowedOrigin)
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept")
            response.setHeader("Access-Control-Max-Age", "3600")
            response.status = HttpStatus.UNAUTHORIZED.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE

            response.writer.write(JsonUtil.convertToJsonStr(ApiResult.error("expired jwt token")))

        } catch (e: Exception) {
            logger.error(e.localizedMessage, e)
            CourtboardContext.setContext(RequestContext(Constants.GUEST_ID, Constants.ROLE_GUEST))
        }
    }


}