package me.multimoduleexam.moduleapi.api

import com.google.protobuf.Api
import jakarta.validation.Valid
import me.multimoduleexam.domain.MemberRepository
import me.multimoduleexam.moduleapi.api.dto.ApiRequestDto
import me.multimoduleexam.moduleapi.api.dto.ApiResult
import me.multimoduleexam.moduleapi.api.dto.MemberRequestDto
import me.multimoduleexam.moduleapi.exception.CustomRuntimeException
import me.multimoduleexam.moduleapi.helper.BatchHelper
import me.multimoduleexam.util.DateUtil
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.sql.PreparedStatement

@RequestMapping("/api")
@RestController
class HelloController(
    val memberRepository: MemberRepository,
    val jdbcTemplate: JdbcTemplate
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

    @PostMapping("/member")
    fun postMember(@RequestBody @Valid dto: MemberRequestDto): ApiResult<*> {
        memberRepository.save(MemberRequestDto.toEntity(dto))
        return ApiResult.ok()
    }

    @GetMapping("/error")
    fun getErrorMessage(): ApiResult<*> {
        throw CustomRuntimeException(HttpStatus.BAD_REQUEST)
    }

    @PostMapping("/data-test")
    fun postDataTest(@RequestBody data: ApiRequestDto): ApiResult<*> {
        return ApiResult.ok(data)
    }

    @PostMapping("/jdbc-template-test")
    fun jdbcTemplateTest(@RequestBody data: List<MemberRequestDto>): ApiResult<*> {

        val sql = "INSERT INTO tbl_member VALUES (?,?,?,?)"
        BatchHelper.executeBatchUpdate(jdbcTemplate, sql, data) { ps: PreparedStatement, i: Int ->
            ps.setString(1, null)
            ps.setString(2, data[i].name)
            ps.setString(3, data[i].email)
            ps.setString(4, data[i].age.toString())
        }

        return ApiResult.ok()
    }
}
