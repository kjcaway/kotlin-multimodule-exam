package me.courtboard.api.api.tactics.dto

import jakarta.validation.constraints.*
import me.courtboard.api.api.tactics.entity.TacticsEntity
import me.multimoduleexam.util.GeneratorUtil
import me.multimoduleexam.validator.XssChecker

data class TacticsReqDto(
    @field:NotBlank(message = "Title is required")
    @field:Size(min = 4, max = 100, message = "Title must be between 4 and 100 characters")
    @field:XssChecker(message = "Title contains invalid characters")
    val title: String,
    @field:Size(max = 1000, message = "Description must not exceed 1000 characters")
    @field:XssChecker(message = "Description contains invalid characters")
    val description: String?,
    @field:NotNull(message = "Formations are required")
    val formations: Map<String, Formation>,
    @field:NotNull(message = "PlayerInfo are required")
    val playerInfo: List<PlayerInfo>,
    val isPublic: Boolean = false,
) {
    data class Formation(
        @field:NotEmpty(message = "Players list cannot be empty")
        val players: List<Player>,
        @field:NotNull(message = "Ball information is required")
        val ball: Ball
    )

    data class PlayerInfo(
        @field:Positive(message = "Invalid player ID")
        val id: Long,
        @field:NotBlank(message = "Color is required")
        val color: String,
        @field:NotBlank(message = "Name is required")
        val name: String,
    )

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

    fun toEntity(): TacticsEntity {
        return TacticsEntity(
            id = GeneratorUtil.generateUUIDWithoutDashes(),
            name = this.title,
            description = this.description,
            isPublic = this.isPublic,
        )
    }

    fun hasAllSameBallPosition(): Boolean {
        if (formations.size <= 1) return false

        // x좌표들만 모아서 비교
        val allSameX = formations.values
            .map { it.ball.x }
            .distinct()
            .size == 1

        // y좌표들만 모아서 비교
        val allSameY = formations.values
            .map { it.ball.y }
            .distinct()
            .size == 1

        return allSameX && allSameY
    }

    fun hasAllSamePlayerPosition(): Boolean {
        if (formations.size <= 1) return false

        val formationsList = formations.values.toList()
        var isAllSame = false
        for (player in formationsList[0].players) {
            val playerId = player.id

            // 특정 player의 모든 formation에서의 x좌표만 비교
            val allSameX = formationsList
                .mapNotNull { formation ->
                    formation.players.find { it.id == playerId }?.x
                }
                .distinct()
                .size == 1

            // 특정 player의 모든 formation에서의 y좌표만 비교
            val allSameY = formationsList
                .mapNotNull { formation ->
                    formation.players.find { it.id == playerId }?.y
                }
                .distinct()
                .size == 1

            if (allSameX && allSameY) {
                isAllSame = true
            } else {
                // 하나라도 이동된 player가 있다면 false
                return false
            }
        }
        return isAllSame
    }
}