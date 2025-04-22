package me.courtboard.api.api.member

import me.courtboard.api.global.dto.ApiResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MemberController(
) {
    @GetMapping("/api/member")
    fun getMembers(): ApiResult<*> {
        return ApiResult.ok()
    }
}