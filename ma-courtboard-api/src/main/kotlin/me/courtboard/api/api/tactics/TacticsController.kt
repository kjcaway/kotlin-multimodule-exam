package me.courtboard.api.api.tactics

import jakarta.validation.Valid
import me.courtboard.api.api.tactics.dto.TacticsReqDto
import me.courtboard.api.api.tactics.service.TacticsService
import me.courtboard.api.global.dto.ApiResult
import org.springframework.web.bind.annotation.*

@RestController
class TacticsController(
    private val tacticsService: TacticsService
) {

    @PostMapping("/api/tactics")
    fun postTactics(@Valid @RequestBody dto: TacticsReqDto): ApiResult<*> {
        val result = tacticsService.createTactic(dto)
        return ApiResult.ok(result)
    }

    @GetMapping("/api/tactics")
    fun getTacticsList(
        @RequestParam(required = false, defaultValue = "0") start: Int,
        @RequestParam(required = false, defaultValue = "10") limit: Int
    ): ApiResult<*> {
        val result = tacticsService.getTactics(start, limit)
        return ApiResult.ok(result)
    }

    @GetMapping("/api/tactics/{id}")
    fun getTactics(@PathVariable id: String): ApiResult<*> {
        val result = tacticsService.getTactic(id)
        return ApiResult.ok(result)
    }

    @PutMapping("/api/tactics/{id}")
    fun putTactics(@PathVariable id: String, @Valid @RequestBody dto: TacticsReqDto): ApiResult<*> {
        val result = tacticsService.updateTactic(id, dto)
        return ApiResult.ok(result)
    }

    @DeleteMapping("/api/tactics/{id}")
    fun deleteTactics(@PathVariable id: String): ApiResult<*> {
        tacticsService.deleteTactic(id)
        return ApiResult.ok()
    }
}