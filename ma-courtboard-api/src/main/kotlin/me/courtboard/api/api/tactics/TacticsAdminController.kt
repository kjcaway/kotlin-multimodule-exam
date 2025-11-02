package me.courtboard.api.api.tactics

import me.courtboard.api.aop.CheckPerm
import me.courtboard.api.api.tactics.service.TacticsService
import me.courtboard.api.global.dto.ApiResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class TacticsAdminController(
    private val tacticsService: TacticsService
) {
    @CheckPerm
    @GetMapping("/api/admin/tactics")
    fun getTacticsList(
        @RequestParam(required = false, defaultValue = "0") start: Int,
        @RequestParam(required = false, defaultValue = "10") limit: Int
    ): ApiResult<*> {
        val result = tacticsService.getAllTactics(start, limit)
        return ApiResult.ok(result)
    }
}