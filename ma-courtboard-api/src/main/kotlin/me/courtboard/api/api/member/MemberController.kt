package me.courtboard.api.api.member

import me.courtboard.api.api.member.dto.MemberCodeCheckReqDto
import me.courtboard.api.api.member.dto.MemberLoginReqDto
import me.courtboard.api.api.member.dto.MemberReqDto
import me.courtboard.api.api.member.service.MemberService
import me.courtboard.api.global.dto.ApiResult
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class MemberController(
    val memberService: MemberService
) {

    @PostMapping("/api/member/login")
    fun login(@RequestBody dto: MemberLoginReqDto): ApiResult<*> {
        val tokens = memberService.getToken(dto)
        return ApiResult.ok(tokens)
    }

    @PostMapping("/api/member")
    fun createMember(@RequestBody dto: MemberReqDto): ApiResult<*> {
        memberService.createNewMember(dto)
        return ApiResult.ok()
    }

    @PostMapping("/api/member/check-verification-code")
    fun checkVerificationCode(@RequestBody dto: MemberCodeCheckReqDto): ApiResult<*> {
        val check = memberService.checkVerificationCode(dto.email, dto.code ?: "")
        return ApiResult.ok(check)
    }
}