package me.courtboard.api.support.fixture

import me.courtboard.api.api.board.dto.BoardReqDto

object BoardFixtures {
    fun validReqDto(): BoardReqDto = BoardReqDto(
        title = "테스트 게시물 제목",
        contents = "<p>본문 내용입니다.</p><p><strong>강조</strong></p>",
    )
}
