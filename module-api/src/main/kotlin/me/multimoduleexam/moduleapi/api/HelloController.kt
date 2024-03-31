package me.multimoduleexam.moduleapi.api

import me.multimoduleexam.domain.MemberRepository
import me.multimoduleexam.moduleapi.api.dto.ApiRequestDto
import me.multimoduleexam.moduleapi.api.dto.ApiResult
import me.multimoduleexam.moduleapi.exception.CustomRuntimeException
import me.multimoduleexam.util.DateUtil
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api")
@RestController
class HelloController(
    val memberRepository: MemberRepository
) {

    @GetMapping
    fun get(): ApiResult<*> {
        val now = DateUtil.getNowDateStr()
        return ApiResult.ok(now)
    }

    @GetMapping("/member")
    fun getMember(): ApiResult<*> {
        val result = memberRepository.findAll()
        return ApiResult.ok(result)
    }

    @GetMapping("/error")
    fun getErrorMessage(): ApiResult<*> {
        throw CustomRuntimeException(HttpStatus.BAD_REQUEST)
    }

    @PostMapping("/data-test")
    fun postDataTest(@RequestBody data: ApiRequestDto): ApiResult<*> {
        return ApiResult.ok(data)
    }
}
