package me.courtboard.api.api.board

import jakarta.validation.Valid
import me.courtboard.api.api.board.dto.BoardReqDto
import me.courtboard.api.api.board.service.BoardService
import me.courtboard.api.global.dto.ApiResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class BoardController(
    private val boardService: BoardService,
) {

    @PostMapping("/api/board")
    fun postBoard(@Valid @RequestBody dto: BoardReqDto): ApiResult<*> {
        val result = boardService.createBoard(dto)
        return ApiResult.ok(result)
    }

    @GetMapping("/api/board")
    fun getBoardList(
        @RequestParam(required = false, defaultValue = "0") start: Int,
        @RequestParam(required = false, defaultValue = "10") limit: Int,
    ): ApiResult<*> {
        val result = boardService.getBoards(start, limit)
        return ApiResult.ok(result)
    }

    @GetMapping("/api/board/{id}")
    fun getBoard(@PathVariable id: String): ApiResult<*> {
        val result = boardService.getBoard(id)
        return ApiResult.ok(result)
    }
}
