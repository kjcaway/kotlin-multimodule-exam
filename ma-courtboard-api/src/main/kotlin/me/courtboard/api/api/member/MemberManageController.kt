package me.courtboard.api.api.member

import me.courtboard.api.aop.CheckPerm
import me.courtboard.api.api.member.dto.MemberGrantReqDto
import me.courtboard.api.api.member.service.MemberService
import me.courtboard.api.global.dto.ApiResult
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class MemberManageController(
    private val memberService: MemberService
) {
    @CheckPerm
    @PostMapping("/api/member-manage/grant-role")
    fun grantRoleForUser(@RequestBody dto: MemberGrantReqDto): ApiResult<*> {
        memberService.grantRoleForUser(dto.email, dto.role)
        return ApiResult.ok()
    }
}