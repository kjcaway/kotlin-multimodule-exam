package me.courtboard.api.api.member

import jakarta.validation.Valid
import me.courtboard.api.aop.CheckPerm
import me.courtboard.api.api.member.dto.MemberGrantReqDto
import me.courtboard.api.api.member.dto.MemberRoleUpdateReqDto
import me.courtboard.api.api.member.service.MemberService
import me.courtboard.api.global.dto.ApiResult
import org.springframework.web.bind.annotation.*

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

    @CheckPerm
    @GetMapping("/api/admin/users/count")
    fun getAllMembersCount(): ApiResult<*> {
        val count = memberService.getAllMembersCount()
        return ApiResult.ok(mapOf("count" to count))
    }

    @CheckPerm
    @PostMapping("/api/admin/grant-role")
    fun grantRoleForUser(@RequestBody dto: MemberGrantReqDto): ApiResult<*> {
        memberService.grantRoleForUser(dto.email, dto.role)
        return ApiResult.ok()
    }

    @CheckPerm
    @PutMapping("/api/admin/users/{id}/role")
    fun updateMemberRole(
        @PathVariable id: String,
        @Valid @RequestBody dto: MemberRoleUpdateReqDto,
    ): ApiResult<*> {
        memberService.updateMemberRole(id, dto.role)
        return ApiResult.ok()
    }
}
