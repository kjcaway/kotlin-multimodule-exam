package me.courtboard.api.aop

import jakarta.servlet.http.HttpServletResponse
import me.courtboard.api.global.CourtboardContext
import me.courtboard.api.global.error.CustomRuntimeException
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.casbin.jcasbin.main.Enforcer
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*

@Aspect
@Component
class CheckPermAop(
    private val enforcer: Enforcer
) {
    private val logger = LoggerFactory.getLogger(CheckPermAop::class.java)

    @Around("@annotation(me.courtboard.api.aop.CheckPerm)")
    fun checkPermission(proceedingJoinPoint: ProceedingJoinPoint): Any {
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        val response: HttpServletResponse? =
            (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).response
        val requestPath = request.requestURI
        val method = request.method
        val context = CourtboardContext.getContext()
        val memberId = context.memberId

        logger.info("memberId : $memberId, role : ${context.role}, requestPath : $requestPath, method : $method")

        try {
            val hasPerm = enforcer.enforce(memberId, "courtboard", requestPath, method.lowercase(Locale.getDefault()))
            if (!hasPerm) {
                throw CustomRuntimeException(HttpStatus.FORBIDDEN, "you don't have permission to access this resource")
            }
            return proceedingJoinPoint.proceed(proceedingJoinPoint.args);
        } finally {
            CourtboardContext.clearContext()
        }
    }
}