package me.courtboard.api.api.my

import jakarta.validation.Valid
import me.courtboard.api.aop.CheckPerm
import me.courtboard.api.api.member.dto.ChangeNameReqDto
import me.courtboard.api.api.member.dto.ChangePasswordReqDto
import me.courtboard.api.api.member.service.MemberService
import me.courtboard.api.api.tactics.service.TacticsService
import me.courtboard.api.global.dto.ApiResult
import org.springframework.web.bind.annotation.*

@RestController
class MyController(
    private val tacticsService: TacticsService,
    private val memberService: MemberService
) {
    @CheckPerm
    @GetMapping("/api/my/tactics")
    fun getTactics(): ApiResult<*> {
        val result = tacticsService.getMyTactics()
        return ApiResult.ok(result)
    }

    @CheckPerm
    @GetMapping("/api/my/info")
    fun getMyInfo(): ApiResult<*> {
        val result = memberService.getMyInfo()
        return ApiResult.ok(result)
    }

    @CheckPerm
    @PutMapping("/api/my/info")
    fun changeMyInfo(@Valid @RequestBody dto: ChangeNameReqDto): ApiResult<*> {
        val result = memberService.changeName(dto)
        return ApiResult.ok(result)
    }

    @CheckPerm
    @PutMapping("/api/my/password")
    fun changePassword(@Valid @RequestBody dto: ChangePasswordReqDto): ApiResult<*> {
        memberService.changePassword(dto)
        return ApiResult.ok()
    }

    @CheckPerm
    @DeleteMapping("/api/my/account")
    fun deleteMember(): ApiResult<*> {
        memberService.deleteMember()
        return ApiResult.ok()
    }
}