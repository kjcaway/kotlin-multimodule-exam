package me.courtboard.api.api.board.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import me.multimoduleexam.validator.XssChecker

data class BoardCommentReqDto(
    @field:NotBlank(message = "Contents is required")
    @field:Size(max = 1000, message = "Comment must be at most 1000 characters")
    @field:XssChecker(message = "Contents contains invalid characters")
    val contents: String,

    val parentId: String? = null,
)
