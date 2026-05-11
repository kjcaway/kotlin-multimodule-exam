package api.board

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.courtboard.api.api.board.BoardCommentController
import me.courtboard.api.api.board.dto.BoardCommentReqDto
import me.courtboard.api.api.board.dto.BoardCommentResDto
import me.courtboard.api.api.board.service.BoardCommentService
import me.courtboard.api.global.error.CustomExceptionHandler
import me.courtboard.api.global.error.CustomRuntimeException
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class BoardCommentControllerTest {

    @Mock
    private lateinit var boardCommentService: BoardCommentService

    @InjectMocks
    private lateinit var boardCommentController: BoardCommentController

    private lateinit var mockMvc: MockMvc

    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(boardCommentController)
            .setControllerAdvice(CustomExceptionHandler())
            .build()
    }

    @Test
    fun `GET api board boardId comments - 트리 구조 댓글 목록 조회 성공`() {
        val boardId = "board-uuid-1"
        val now = LocalDateTime.now()
        val parent = BoardCommentResDto(
            id = "c-1",
            boardId = boardId,
            parentId = null,
            contents = "최상위 댓글",
            createdId = "member-1",
            createdName = "유저A",
            createdAvatarUrl = null,
            createdAt = now,
            updatedAt = now,
        )
        parent.replies.add(
            BoardCommentResDto(
                id = "c-2",
                boardId = boardId,
                parentId = "c-1",
                contents = "대댓글",
                createdId = "member-2",
                createdName = "유저B",
                createdAvatarUrl = null,
                createdAt = now,
                updatedAt = now,
            )
        )
        whenever(boardCommentService.getCommentsByBoard(eq(boardId)))
            .thenReturn(listOf(parent))

        mockMvc.perform(get("/api/board/{boardId}/comments", boardId))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(1)))
            .andExpect(jsonPath("$.data[0].id").value("c-1"))
            .andExpect(jsonPath("$.data[0].parentId").doesNotExist())
            .andExpect(jsonPath("$.data[0].contents").value("최상위 댓글"))
            .andExpect(jsonPath("$.data[0].createdName").value("유저A"))
            .andExpect(jsonPath("$.data[0].replies", hasSize<Any>(1)))
            .andExpect(jsonPath("$.data[0].replies[0].id").value("c-2"))
            .andExpect(jsonPath("$.data[0].replies[0].parentId").value("c-1"))
            .andExpect(jsonPath("$.data[0].replies[0].contents").value("대댓글"))
    }

    @Test
    fun `GET api board boardId comments - 빈 결과 반환`() {
        val boardId = "board-uuid-empty"
        whenever(boardCommentService.getCommentsByBoard(eq(boardId)))
            .thenReturn(emptyList())

        mockMvc.perform(get("/api/board/{boardId}/comments", boardId))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(0)))
    }

    @Test
    fun `GET api board boardId comments - 삭제된 부모는 contents null + deleted true 로 반환`() {
        val boardId = "board-uuid-soft"
        val now = LocalDateTime.now()
        val deletedParent = BoardCommentResDto(
            id = "c-deleted",
            boardId = boardId,
            parentId = null,
            contents = null,
            createdId = "member-1",
            createdName = "유저A",
            createdAvatarUrl = null,
            createdAt = now,
            updatedAt = now,
            deleted = true,
        )
        deletedParent.replies.add(
            BoardCommentResDto(
                id = "c-child",
                boardId = boardId,
                parentId = "c-deleted",
                contents = "살아있는 대댓글",
                createdId = "member-2",
                createdName = "유저B",
                createdAvatarUrl = null,
                createdAt = now,
                updatedAt = now,
            )
        )
        whenever(boardCommentService.getCommentsByBoard(eq(boardId)))
            .thenReturn(listOf(deletedParent))

        mockMvc.perform(get("/api/board/{boardId}/comments", boardId))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].deleted").value(true))
            .andExpect(jsonPath("$.data[0].contents").doesNotExist())
            .andExpect(jsonPath("$.data[0].replies[0].contents").value("살아있는 대댓글"))
    }

    @Test
    fun `POST api board boardId comments - 최상위 댓글 생성 성공`() {
        val boardId = "board-uuid-1"
        val reqDto = BoardCommentReqDto(contents = "댓글 내용입니다")
        val createdId = "comment-uuid-1"
        whenever(boardCommentService.createComment(eq(boardId), any()))
            .thenReturn(mapOf("id" to createdId))

        mockMvc.perform(
            post("/api/board/{boardId}/comments", boardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(createdId))
    }

    @Test
    fun `POST api board boardId comments - 대댓글 생성 성공`() {
        val boardId = "board-uuid-1"
        val parentId = "parent-comment-uuid"
        val reqDto = BoardCommentReqDto(contents = "답글 내용", parentId = parentId)
        val createdId = "reply-uuid-1"
        whenever(
            boardCommentService.createComment(
                eq(boardId),
                argThat { this.parentId == parentId && this.contents == "답글 내용" },
            )
        ).thenReturn(mapOf("id" to createdId))

        mockMvc.perform(
            post("/api/board/{boardId}/comments", boardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(createdId))
    }

    @Test
    fun `POST api board boardId comments - contents 누락 시 400 반환`() {
        val boardId = "board-uuid-1"
        val invalidBody = mapOf("contents" to "")

        mockMvc.perform(
            post("/api/board/{boardId}/comments", boardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `POST api board boardId comments - contents 길이 초과 시 400 반환`() {
        val boardId = "board-uuid-1"
        val invalidBody = mapOf("contents" to "a".repeat(1001))

        mockMvc.perform(
            post("/api/board/{boardId}/comments", boardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `POST api board boardId comments - 게시글 없음 시 404 반환`() {
        val boardId = "missing-board"
        val reqDto = BoardCommentReqDto(contents = "댓글 내용")
        whenever(boardCommentService.createComment(eq(boardId), any()))
            .thenThrow(CustomRuntimeException(HttpStatus.NOT_FOUND, "board not found"))

        mockMvc.perform(
            post("/api/board/{boardId}/comments", boardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("board not found"))
    }

    @Test
    fun `POST api board boardId comments - 부모 없음 시 404 반환`() {
        val boardId = "board-uuid-1"
        val reqDto = BoardCommentReqDto(contents = "답글", parentId = "missing-parent")
        whenever(boardCommentService.createComment(eq(boardId), any()))
            .thenThrow(CustomRuntimeException(HttpStatus.NOT_FOUND, "parent comment not found"))

        mockMvc.perform(
            post("/api/board/{boardId}/comments", boardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("parent comment not found"))
    }

    @Test
    fun `POST api board boardId comments - 대댓글에 대댓글 작성 시 400 반환`() {
        val boardId = "board-uuid-1"
        val reqDto = BoardCommentReqDto(contents = "이중 대댓글", parentId = "already-a-reply")
        whenever(boardCommentService.createComment(eq(boardId), any()))
            .thenThrow(CustomRuntimeException(HttpStatus.BAD_REQUEST, "Replies can only be one level deep"))

        mockMvc.perform(
            post("/api/board/{boardId}/comments", boardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Replies can only be one level deep"))
    }

    @Test
    fun `POST api board boardId comments - 삭제된 부모에 대댓글 작성 시 400 반환`() {
        val boardId = "board-uuid-1"
        val reqDto = BoardCommentReqDto(contents = "답글", parentId = "soft-deleted-parent")
        whenever(boardCommentService.createComment(eq(boardId), any()))
            .thenThrow(CustomRuntimeException(HttpStatus.BAD_REQUEST, "Cannot reply to a deleted comment"))

        mockMvc.perform(
            post("/api/board/{boardId}/comments", boardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Cannot reply to a deleted comment"))
    }

    @Test
    fun `POST api board boardId comments - 비로그인 시 401 반환`() {
        val boardId = "board-uuid-1"
        val reqDto = BoardCommentReqDto(contents = "댓글")
        whenever(boardCommentService.createComment(eq(boardId), any()))
            .thenThrow(CustomRuntimeException(HttpStatus.UNAUTHORIZED, "login required"))

        mockMvc.perform(
            post("/api/board/{boardId}/comments", boardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("login required"))
    }

    @Test
    fun `PUT api board comments id - 댓글 수정 성공`() {
        val id = "comment-uuid-1"
        val reqDto = BoardCommentReqDto(contents = "수정된 댓글 내용")
        whenever(boardCommentService.updateComment(eq(id), any()))
            .thenReturn(mapOf("id" to id))

        mockMvc.perform(
            put("/api/board/comments/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(id))
    }

    @Test
    fun `PUT api board comments id - contents 누락 시 400 반환`() {
        val id = "comment-uuid-1"
        val invalidBody = mapOf("contents" to "")

        mockMvc.perform(
            put("/api/board/comments/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `PUT api board comments id - 작성자 본인이 아니면 403 반환`() {
        val id = "comment-uuid-1"
        val reqDto = BoardCommentReqDto(contents = "남의 댓글 수정 시도")
        whenever(boardCommentService.updateComment(eq(id), any()))
            .thenThrow(CustomRuntimeException(HttpStatus.FORBIDDEN, "you don't have permission to access this resource"))

        mockMvc.perform(
            put("/api/board/comments/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `PUT api board comments id - 삭제된 댓글 수정 시 400 반환`() {
        val id = "comment-uuid-deleted"
        val reqDto = BoardCommentReqDto(contents = "삭제된 댓글 수정 시도")
        whenever(boardCommentService.updateComment(eq(id), any()))
            .thenThrow(CustomRuntimeException(HttpStatus.BAD_REQUEST, "Cannot edit deleted comment"))

        mockMvc.perform(
            put("/api/board/comments/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Cannot edit deleted comment"))
    }

    @Test
    fun `PUT api board comments id - 존재하지 않으면 404 반환`() {
        val id = "missing-id"
        val reqDto = BoardCommentReqDto(contents = "수정 시도")
        whenever(boardCommentService.updateComment(eq(id), any()))
            .thenThrow(CustomRuntimeException(HttpStatus.NOT_FOUND, "comment not found"))

        mockMvc.perform(
            put("/api/board/comments/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `PUT api board comments id - 비로그인 시 401 반환`() {
        val id = "comment-uuid-1"
        val reqDto = BoardCommentReqDto(contents = "수정")
        whenever(boardCommentService.updateComment(eq(id), any()))
            .thenThrow(CustomRuntimeException(HttpStatus.UNAUTHORIZED, "login required"))

        mockMvc.perform(
            put("/api/board/comments/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `DELETE api board comments id - 댓글 삭제 성공`() {
        val id = "comment-uuid-1"

        mockMvc.perform(delete("/api/board/comments/{id}", id))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `DELETE api board comments id - 작성자 본인이 아니면 403 반환`() {
        val id = "comment-uuid-1"
        doThrow(
            CustomRuntimeException(HttpStatus.FORBIDDEN, "you don't have permission to access this resource")
        ).whenever(boardCommentService).deleteComment(id)

        mockMvc.perform(delete("/api/board/comments/{id}", id))
            .andDo(print())
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `DELETE api board comments id - 존재하지 않으면 404 반환`() {
        val id = "missing-id"
        doThrow(CustomRuntimeException(HttpStatus.NOT_FOUND, "comment not found"))
            .whenever(boardCommentService).deleteComment(id)

        mockMvc.perform(delete("/api/board/comments/{id}", id))
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `DELETE api board comments id - 비로그인 시 401 반환`() {
        val id = "comment-uuid-1"
        doThrow(CustomRuntimeException(HttpStatus.UNAUTHORIZED, "login required"))
            .whenever(boardCommentService).deleteComment(id)

        mockMvc.perform(delete("/api/board/comments/{id}", id))
            .andDo(print())
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.success").value(false))
    }
}
