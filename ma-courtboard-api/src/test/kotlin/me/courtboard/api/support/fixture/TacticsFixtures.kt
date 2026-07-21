package me.courtboard.api.support.fixture

import me.courtboard.api.api.tactics.dto.TacticsReqDto

object TacticsFixtures {
    fun validReqDto(): TacticsReqDto = TacticsReqDto(
        title = "Fast Break",
        description = "quick transition",
        formations = mapOf(
            "step1" to TacticsReqDto.Formation(
                players = listOf(
                    TacticsReqDto.Player(id = 1L, x = 10, y = 20),
                    TacticsReqDto.Player(id = 2L, x = 30, y = 40),
                ),
                ball = TacticsReqDto.Ball(x = 50, y = 60),
            ),
            "step2" to TacticsReqDto.Formation(
                players = listOf(
                    TacticsReqDto.Player(id = 1L, x = 100, y = 200),
                    TacticsReqDto.Player(id = 2L, x = 300, y = 400),
                ),
                ball = TacticsReqDto.Ball(x = 500, y = 600),
            ),
        ),
        playerInfo = listOf(
            TacticsReqDto.PlayerInfo(id = 1L, color = "#fff", name = "p1"),
            TacticsReqDto.PlayerInfo(id = 2L, color = "#000", name = "p2"),
        ),
        isPublic = false,
        isHalfCourt = false,
    )
}
