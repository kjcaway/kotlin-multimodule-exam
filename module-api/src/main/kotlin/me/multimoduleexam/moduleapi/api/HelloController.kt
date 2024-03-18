package me.multimoduleexam.moduleapi.api

import me.multimoduleexam.domain.MemberRepository
import me.multimoduleexam.util.DateUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController(
    val memberRepository: MemberRepository
) {

    @GetMapping
    fun get(): ResponseEntity<*> {
        val now = DateUtil.getNowDateStr()
        return ResponseEntity.ok(now)
    }

    @GetMapping("/member")
    fun getMember(): ResponseEntity<*> {
        val result = memberRepository.findAll()
        return ResponseEntity.ok(result)
    }
}
