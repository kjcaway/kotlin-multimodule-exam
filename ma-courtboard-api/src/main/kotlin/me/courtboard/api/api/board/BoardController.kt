package me.courtboard.api.api.board

import jakarta.validation.Valid
import me.courtboard.api.aop.CheckPerm
import me.courtboard.api.api.board.dto.BoardReqDto
import me.courtboard.api.api.board.service.BoardImageService
import me.courtboard.api.api.board.service.BoardService
import me.courtboard.api.global.CourtboardContext
import me.courtboard.api.global.dto.ApiResult
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class BoardController(
    private val boardService: BoardService,
    private val boardImageService: BoardImageService,
) {

    @CheckPerm
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

    @GetMapping("/api/board/by-author")
    fun getBoardByAuthor(
        @RequestParam name: String,
        @RequestParam title: String,
    ): ApiResult<*> {
        val result = boardService.getBoardByAuthor(name, title)
        return ApiResult.ok(result)
    }

    @GetMapping("/api/board/{id}")
    fun getBoard(@PathVariable id: String): ApiResult<*> {
        val result = boardService.getBoard(id)
        return ApiResult.ok(result)
    }

    @CheckPerm
    @PutMapping("/api/board/{id}")
    fun putBoard(
        @PathVariable id: String,
        @Valid @RequestBody dto: BoardReqDto,
    ): ApiResult<*> {
        val result = boardService.updateBoard(id, dto)
        return ApiResult.ok(result)
    }

    @CheckPerm
    @DeleteMapping("/api/board/{id}")
    fun deleteBoard(@PathVariable id: String): ApiResult<*> {
        boardService.deleteBoard(id)
        return ApiResult.ok()
    }

    @CheckPerm
    @PostMapping("/api/board/images")
    fun uploadImage(@RequestPart("file") file: MultipartFile): ApiResult<*> {
        val memberId = CourtboardContext.getContext().memberId
        val result = boardImageService.upload(file, memberId)
        return ApiResult.ok(result)
    }
}
