package me.courtboard.api.api.member

import me.courtboard.api.aop.CheckPerm
import me.courtboard.api.api.member.service.MemberService
import me.courtboard.api.global.dto.ApiResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class MemberAdminController(
    private val memberService: MemberService
) {
    @CheckPerm
    @GetMapping("/api/admin/users")
    fun getAllMembers(
        @RequestParam(required = false, defaultValue = "0") start: Int,
        @RequestParam(required = false, defaultValue = "10") limit: Int
    ): ApiResult<*> {
        val result = memberService.getAllMembers(start, limit)
        return ApiResult.ok(result)
    }
}
