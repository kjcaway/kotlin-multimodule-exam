package me.courtboard.api.api.board.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import me.multimoduleexam.validator.XssChecker

/**
 * 게시물 작성/수정 요청 DTO.
 *
 * - title: 일반 텍스트. 기존 [XssChecker] 정규식 검증 사용.
 * - contents: 리치 HTML(에디터 산출물). [XssChecker]는 base64 data URI 이미지를 금지하므로 사용 불가.
 *             서비스 레이어에서 jsoup 기반 [me.courtboard.api.api.board.util.BoardHtmlSanitizer]로 화이트리스트 정화한다.
 */
data class BoardReqDto(
    @field:NotBlank(message = "Title is required")
    @field:Size(min = 2, max = 256, message = "Title must be between 2 and 256 characters")
    @field:XssChecker(message = "Title contains invalid characters")
    val title: String,

    @field:NotNull(message = "Contents is required")
    val contents: String,
)
