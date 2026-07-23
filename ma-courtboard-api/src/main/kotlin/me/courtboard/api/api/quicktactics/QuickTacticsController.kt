package me.courtboard.api.api.quicktactics

import jakarta.validation.Valid
import me.courtboard.api.aop.CheckLogin
import me.courtboard.api.api.quicktactics.dto.QuickTacticsReqDto
import me.courtboard.api.api.quicktactics.service.QuickTacticsService
import me.courtboard.api.global.dto.ApiResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class QuickTacticsController(
    private val quickTacticsService: QuickTacticsService,
) {

    @CheckLogin
    @GetMapping("/api/quick-tactics")
    fun getMyQuickTactics(): ApiResult<*> {
        val result = quickTacticsService.getMyQuickTactics()
        return ApiResult.ok(result)
    }

    @CheckLogin
    @PutMapping("/api/quick-tactics")
    fun putMyQuickTactics(@Valid @RequestBody dto: QuickTacticsReqDto): ApiResult<*> {
        val result = quickTacticsService.saveMyQuickTactics(dto)
        return ApiResult.ok(result)
    }
}
