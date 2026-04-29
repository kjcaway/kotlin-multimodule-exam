package api.board

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.courtboard.api.api.board.BoardController
import me.courtboard.api.api.board.dto.BoardListResDto
import me.courtboard.api.api.board.dto.BoardReqDto
import me.courtboard.api.api.board.dto.BoardResDto
import me.courtboard.api.api.board.service.BoardService
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
class BoardControllerTest {

    @Mock
    private lateinit var boardService: BoardService

    @InjectMocks
    private lateinit var boardController: BoardController

    private lateinit var mockMvc: MockMvc

    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(boardController)
            .setControllerAdvice(CustomExceptionHandler())
            .build()
    }

    @Test
    fun `POST api board - 게시물 생성 성공`() {
        val reqDto = validBoardReqDto()
        val createdId = "board-uuid-123"
        whenever(boardService.createBoard(any())).thenReturn(mapOf("id" to createdId))

        mockMvc.perform(
            post("/api/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(createdId))
    }

    @Test
    fun `POST api board - title 누락 시 400 반환`() {
        val invalidBody = mapOf(
            "title" to "",
            "contents" to "<p>본문 내용</p>"
        )

        mockMvc.perform(
            post("/api/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `POST api board - title 길이 초과 시 400 반환`() {
        val invalidBody = mapOf(
            "title" to "a".repeat(257),
            "contents" to "<p>본문 내용</p>"
        )

        mockMvc.perform(
            post("/api/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `POST api board - contents 누락 시 400 반환`() {
        val invalidBody = mapOf(
            "title" to "정상 제목"
        )

        mockMvc.perform(
            post("/api/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `POST api board - 비로그인 시 401 반환`() {
        val reqDto = validBoardReqDto()
        whenever(boardService.createBoard(any()))
            .thenThrow(CustomRuntimeException(HttpStatus.UNAUTHORIZED, "login required"))

        mockMvc.perform(
            post("/api/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("login required"))
    }

    @Test
    fun `POST api board - Quill HTML(base64 이미지 포함) 정상 처리`() {
        val reqDto = BoardReqDto(
            title = "코트 캡처 게시물",
            contents = "<p>설명</p><p><img src=\"data:image/png;base64,iVBORw0KGgo=\" width=\"50%\"></p>"
        )
        val createdId = "board-uuid-456"
        whenever(boardService.createBoard(any())).thenReturn(mapOf("id" to createdId))

        mockMvc.perform(
            post("/api/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(createdId))
    }

    @Test
    fun `GET api board - 게시물 목록 조회 성공`() {
        val now = LocalDateTime.now()
        val list = listOf(
            BoardListResDto(
                id = "board-1",
                title = "첫 번째 게시물",
                createdId = "member-1",
                createdName = "관리자A",
                createdAt = now,
            ),
            BoardListResDto(
                id = "board-2",
                title = "두 번째 게시물",
                createdId = "member-2",
                createdName = null,
                createdAt = now,
            ),
        )
        whenever(boardService.getBoards(eq(0), eq(10))).thenReturn(list)

        mockMvc.perform(
            get("/api/board")
                .param("start", "0")
                .param("limit", "10")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(2)))
            .andExpect(jsonPath("$.data[0].id").value("board-1"))
            .andExpect(jsonPath("$.data[0].title").value("첫 번째 게시물"))
            .andExpect(jsonPath("$.data[0].createdName").value("관리자A"))
            .andExpect(jsonPath("$.data[1].id").value("board-2"))
            .andExpect(jsonPath("$.data[1].createdName").doesNotExist())
    }

    @Test
    fun `GET api board - 쿼리 파라미터 미지정 시 기본값 사용`() {
        whenever(boardService.getBoards(eq(0), eq(10))).thenReturn(emptyList())

        mockMvc.perform(get("/api/board"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(0)))
    }

    @Test
    fun `GET api board id - 게시물 단건 조회 성공`() {
        val id = "board-uuid-789"
        val resDto = BoardResDto(
            id = id,
            title = "상세 조회 테스트",
            contents = "<p>본문 내용</p><p><img src=\"data:image/png;base64,abc\"></p>",
            createdId = "member-3",
            createdName = "관리자B",
            createdAt = LocalDateTime.now(),
        )
        whenever(boardService.getBoard(id)).thenReturn(resDto)

        mockMvc.perform(get("/api/board/{id}", id))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(id))
            .andExpect(jsonPath("$.data.title").value("상세 조회 테스트"))
            .andExpect(jsonPath("$.data.contents").value(resDto.contents))
            .andExpect(jsonPath("$.data.createdName").value("관리자B"))
    }

    @Test
    fun `GET api board id - 존재하지 않으면 404 반환`() {
        val id = "missing-id"
        whenever(boardService.getBoard(id))
            .thenThrow(CustomRuntimeException(HttpStatus.NOT_FOUND))

        mockMvc.perform(get("/api/board/{id}", id))
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `PUT api board id - 게시물 수정 성공`() {
        val id = "board-uuid-update"
        val reqDto = validBoardReqDto()
        whenever(boardService.updateBoard(eq(id), any())).thenReturn(mapOf("id" to id))

        mockMvc.perform(
            put("/api/board/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(id))
    }

    @Test
    fun `PUT api board id - 작성자 본인이 아니면 403 반환`() {
        val id = "board-uuid-update"
        val reqDto = validBoardReqDto()
        whenever(boardService.updateBoard(eq(id), any()))
            .thenThrow(CustomRuntimeException(HttpStatus.FORBIDDEN, "you don't have permission to access this resource"))

        mockMvc.perform(
            put("/api/board/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `PUT api board id - 비로그인 시 401 반환`() {
        val id = "board-uuid-update"
        val reqDto = validBoardReqDto()
        whenever(boardService.updateBoard(eq(id), any()))
            .thenThrow(CustomRuntimeException(HttpStatus.UNAUTHORIZED, "login required"))

        mockMvc.perform(
            put("/api/board/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `PUT api board id - 존재하지 않으면 404 반환`() {
        val id = "missing-id"
        val reqDto = validBoardReqDto()
        whenever(boardService.updateBoard(eq(id), any()))
            .thenThrow(CustomRuntimeException(HttpStatus.NOT_FOUND))

        mockMvc.perform(
            put("/api/board/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `DELETE api board id - 게시물 삭제 성공`() {
        val id = "board-uuid-delete"

        mockMvc.perform(delete("/api/board/{id}", id))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `DELETE api board id - 작성자 본인이 아니면 403 반환`() {
        val id = "board-uuid-delete"
        org.mockito.kotlin.doThrow(
            CustomRuntimeException(HttpStatus.FORBIDDEN, "you don't have permission to access this resource")
        ).whenever(boardService).deleteBoard(id)

        mockMvc.perform(delete("/api/board/{id}", id))
            .andDo(print())
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `DELETE api board id - 비로그인 시 401 반환`() {
        val id = "board-uuid-delete"
        org.mockito.kotlin.doThrow(
            CustomRuntimeException(HttpStatus.UNAUTHORIZED, "login required")
        ).whenever(boardService).deleteBoard(id)

        mockMvc.perform(delete("/api/board/{id}", id))
            .andDo(print())
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `DELETE api board id - 존재하지 않으면 404 반환`() {
        val id = "missing-id"
        org.mockito.kotlin.doThrow(CustomRuntimeException(HttpStatus.NOT_FOUND))
            .whenever(boardService).deleteBoard(id)

        mockMvc.perform(delete("/api/board/{id}", id))
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
    }

    private fun validBoardReqDto(): BoardReqDto {
        return BoardReqDto(
            title = "테스트 게시물 제목",
            contents = "<p>본문 내용입니다.</p><p><strong>강조</strong></p>"
        )
    }
}
