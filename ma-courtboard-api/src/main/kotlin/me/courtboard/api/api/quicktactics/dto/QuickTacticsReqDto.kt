package me.courtboard.api.api.quicktactics.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

/**
 * 퀵보드 저장 요청 - 단일 정지 상태(포메이션 1개)만 다룬다.
 */
data class QuickTacticsReqDto(
    @field:NotNull(message = "Players are required")
    @field:Valid
    val players: List<Player>,

    @field:NotNull(message = "Ball information is required")
    @field:Valid
    val ball: Ball,

    @field:NotNull(message = "PlayerInfo are required")
    @field:Valid
    val playerInfo: List<PlayerInfo>,

    val isHalfCourt: Boolean = false,
) {
    data class Player(
        @field:Positive(message = "Invalid player ID")
        val id: Long,
        val x: Int,
        val y: Int,
    )

    data class Ball(
        val x: Int,
        val y: Int,
    )

    data class PlayerInfo(
        @field:Positive(message = "Invalid player ID")
        val id: Long,
        @field:NotBlank(message = "Color is required")
        val color: String,
        @field:NotBlank(message = "Name is required")
        val name: String,
        val showGhost: Boolean = false,
    )
}
