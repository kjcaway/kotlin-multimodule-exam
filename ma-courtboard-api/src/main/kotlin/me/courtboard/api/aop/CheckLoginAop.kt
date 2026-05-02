package me.courtboard.api.aop

import me.courtboard.api.global.CourtboardContext
import me.courtboard.api.global.error.CustomRuntimeException
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Aspect
@Component
@Order(1)
class CheckLoginAop {
    @Around("@annotation(me.courtboard.api.aop.CheckLogin)")
    fun checkLogin(proceedingJoinPoint: ProceedingJoinPoint): Any {
        if (!CourtboardContext.isLogin()) {
            throw CustomRuntimeException(HttpStatus.UNAUTHORIZED, "login required")
        }
        return proceedingJoinPoint.proceed(proceedingJoinPoint.args)
    }
}
