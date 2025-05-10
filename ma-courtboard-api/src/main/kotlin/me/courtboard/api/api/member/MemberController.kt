package me.courtboard.api.api.member

import jakarta.validation.Valid
import me.courtboard.api.api.member.dto.*
import me.courtboard.api.api.member.service.MemberService
import me.courtboard.api.global.dto.ApiResult
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class MemberController(
    val memberService: MemberService
) {

    @PostMapping("/api/member")
    fun createMember(@Valid @RequestBody dto: MemberReqDto): ApiResult<*> {
        memberService.createNewMember(dto)
        return ApiResult.ok()
    }

    @PostMapping("/api/member/login")
    fun login(@Valid @RequestBody dto: MemberLoginReqDto): ApiResult<*> {
        val tokens = memberService.getToken(dto)
        return ApiResult.ok(tokens)
    }

    @PostMapping("/api/member/refresh")
    fun refresh(@Valid @RequestBody dto: RefreshTokenReqDto): ApiResult<*> {
        val tokens = memberService.getTokenByRefreshToken(dto)
        return ApiResult.ok(tokens)
    }

    @PostMapping("/api/member/send-verification-code")
    fun sendVerifyCodeEmail(@Valid @RequestBody dto: MemberSendCodeReqDto): ApiResult<*> {
        memberService.sendVerificationCodeToEmail(dto.email)
        return ApiResult.ok()
    }

    @PostMapping("/api/member/check-verification-code")
    fun checkVerificationCode(@Valid @RequestBody dto: MemberCodeCheckReqDto): ApiResult<*> {
        val check = memberService.checkVerificationCode(dto.email, dto.code ?: "")
        return ApiResult.ok(check)
    }


}