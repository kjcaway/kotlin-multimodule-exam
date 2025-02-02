package me.courtboard.api.api.tactics

import jakarta.validation.Valid
import me.courtboard.api.api.common.dto.ApiResult
import me.courtboard.api.api.tactics.dto.TacticsReqDto
import me.courtboard.api.api.tactics.service.TacticsService
import org.springframework.web.bind.annotation.*

@RestController
class TacticsController(
    private val tacticsService: TacticsService
) {

    @PostMapping("/api/tactics")
    fun postTactics(@RequestBody @Valid dto: TacticsReqDto): ApiResult<*> {
        val result = tacticsService.createTactic(dto)
        return ApiResult.ok(result)
    }

    @GetMapping("/api/tactics/{id}")
    fun getTactics(@PathVariable id: String): ApiResult<*> {
        val result = tacticsService.getTactic(id)
        return ApiResult.ok(result)
    }
}