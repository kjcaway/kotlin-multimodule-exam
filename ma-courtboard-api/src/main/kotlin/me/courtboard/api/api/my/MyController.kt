package me.courtboard.api.api.my

import me.courtboard.api.aop.CheckPerm
import me.courtboard.api.api.member.service.MemberService
import me.courtboard.api.api.tactics.service.TacticsService
import me.courtboard.api.global.dto.ApiResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

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
}