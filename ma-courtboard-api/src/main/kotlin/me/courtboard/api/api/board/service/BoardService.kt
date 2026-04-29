package me.courtboard.api.api.board.service

import me.courtboard.api.api.board.dto.BoardListResDto
import me.courtboard.api.api.board.dto.BoardReqDto
import me.courtboard.api.api.board.dto.BoardResDto
import me.courtboard.api.api.board.dto.BoardResDto.Companion.toBoardResDto
import me.courtboard.api.api.board.entity.BoardEntity
import me.courtboard.api.api.board.repository.BoardRepository
import me.courtboard.api.api.board.util.BoardHtmlSanitizer
import me.courtboard.api.api.member.repository.MemberInfoRepository
import me.courtboard.api.global.CourtboardContext
import me.courtboard.api.global.error.CustomRuntimeException
import me.multimoduleexam.util.GeneratorUtil
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.util.*
import kotlin.jvm.optionals.getOrElse

@Service
class BoardService(
    private val boardRepository: BoardRepository,
    private val memberInfoRepository: MemberInfoRepository,
) {

    fun createBoard(dto: BoardReqDto): Map<String, Any> {
        if (!CourtboardContext.isLogin()) {
            throw CustomRuntimeException(HttpStatus.UNAUTHORIZED, "login required")
        }

        val createdBy = CourtboardContext.getContext().memberId
        val sanitizedContents = BoardHtmlSanitizer.sanitize(dto.contents)

        val entity = BoardEntity(
            id = GeneratorUtil.generateUUIDWithoutDashes(),
            title = dto.title,
            contents = sanitizedContents,
            createdId = createdBy,
        )

        val saved = boardRepository.save(entity)
        return mapOf("id" to saved.id)
    }

    fun getBoards(start: Int, limit: Int): List<BoardListResDto> {
        val rows = boardRepository.findAllForList(start, limit)
        return rows.map { row ->
            BoardListResDto(
                id = row[0] as String,
                title = row[1] as String,
                createdId = row[2] as String,
                createdAt = (row[3] as Timestamp).toLocalDateTime(),
                createdName = row[4] as String?,
            )
        }
    }

    fun getBoard(id: String): BoardResDto {
        val entity = boardRepository.findById(id)
            .orElseThrow { CustomRuntimeException(HttpStatus.NOT_FOUND) }

        val result = entity.toBoardResDto()
        if (entity.createdId != "UNKNOWN") {
            val memberInfo = runCatching { UUID.fromString(entity.createdId) }
                .mapCatching { memberInfoRepository.findById(it).getOrElse { null } }
                .getOrElse { null }
            result.updateCreatedName(memberInfo?.name)
        }
        return result
    }

    fun updateBoard(id: String, dto: BoardReqDto): Map<String, Any> {
        val entity = checkOwner(id)

        entity.title = dto.title
        entity.contents = BoardHtmlSanitizer.sanitize(dto.contents)
        entity.updatedAt = java.time.LocalDateTime.now()
        entity.updatedId = CourtboardContext.getContext().memberId

        val saved = boardRepository.save(entity)
        return mapOf("id" to saved.id)
    }

    fun deleteBoard(id: String) {
        val entity = checkOwner(id)
        boardRepository.delete(entity)
    }

    private fun checkOwner(id: String): BoardEntity {
        if (!CourtboardContext.isLogin()) {
            throw CustomRuntimeException(HttpStatus.UNAUTHORIZED, "login required")
        }

        val entity = boardRepository.findById(id)
            .orElseThrow { CustomRuntimeException(HttpStatus.NOT_FOUND) }

        if (entity.createdId == "UNKNOWN") {
            throw CustomRuntimeException(HttpStatus.FORBIDDEN, "you don't have permission to access this resource")
        }

        val memberId = CourtboardContext.getContext().memberId
        if (entity.createdId != memberId) {
            throw CustomRuntimeException(HttpStatus.FORBIDDEN, "you don't have permission to access this resource")
        }

        return entity
    }
}
