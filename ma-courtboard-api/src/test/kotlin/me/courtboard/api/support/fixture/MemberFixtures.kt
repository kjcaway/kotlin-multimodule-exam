package me.courtboard.api.support.fixture

import me.courtboard.api.api.member.dto.MemberReqDto

object MemberFixtures {
    fun validReqDto(): MemberReqDto = MemberReqDto(
        email = "abc@gmail.com",
        name = "tester",
        avatarUrl = null,
        passwd = "password123",
        code = "123456",
    )
}
