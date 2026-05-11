package me.courtboard.api.api.board

import jakarta.validation.Valid
import me.courtboard.api.aop.CheckPerm
import me.courtboard.api.api.board.dto.BoardCommentReqDto
import me.courtboard.api.api.board.service.BoardCommentService
import me.courtboard.api.global.dto.ApiResult
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class BoardCommentController(
    private val boardCommentService: BoardCommentService,
) {

    @GetMapping("/api/board/{boardId}/comments")
    fun getComments(@PathVariable boardId: String): ApiResult<*> {
        val result = boardCommentService.getCommentsByBoard(boardId)
        return ApiResult.ok(result)
    }

    @CheckPerm
    @PostMapping("/api/board/{boardId}/comments")
    fun postComment(
        @PathVariable boardId: String,
        @Valid @RequestBody dto: BoardCommentReqDto,
    ): ApiResult<*> {
        val result = boardCommentService.createComment(boardId, dto)
        return ApiResult.ok(result)
    }

    @CheckPerm
    @PutMapping("/api/board/comments/{id}")
    fun putComment(
        @PathVariable id: String,
        @Valid @RequestBody dto: BoardCommentReqDto,
    ): ApiResult<*> {
        val result = boardCommentService.updateComment(id, dto)
        return ApiResult.ok(result)
    }

    @CheckPerm
    @DeleteMapping("/api/board/comments/{id}")
    fun deleteComment(@PathVariable id: String): ApiResult<*> {
        boardCommentService.deleteComment(id)
        return ApiResult.ok()
    }
}
