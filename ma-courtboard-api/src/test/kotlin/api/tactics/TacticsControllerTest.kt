package api.tactics

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.courtboard.api.api.tactics.TacticsController
import me.courtboard.api.api.tactics.dto.TacticsListResDto
import me.courtboard.api.api.tactics.dto.TacticsReqDto
import me.courtboard.api.api.tactics.dto.TacticsResDto
import me.courtboard.api.api.tactics.service.TacticsService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class TacticsControllerTest {

    @Mock
    private lateinit var tacticsService: TacticsService

    @InjectMocks
    private lateinit var tacticsController: TacticsController

    private lateinit var mockMvc: MockMvc

    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tacticsController)
            .setControllerAdvice(CustomExceptionHandler())
            .build()
    }

    @Test
    fun `POST api tactics - 전술 생성 성공`() {
        val reqDto = validTacticsReqDto()
        val createdId = "abc123"
        whenever(tacticsService.createTactic(any())).thenReturn(mapOf("id" to createdId))

        mockMvc.perform(
            post("/api/tactics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(createdId))
    }

    @Test
    fun `POST api tactics - title 누락 시 400 반환`() {
        val invalidBody = mapOf(
            "title" to "",
            "description" to "desc",
            "formations" to emptyMap<String, Any>(),
            "playerInfo" to emptyList<Any>()
        )

        mockMvc.perform(
            post("/api/tactics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `POST api tactics - 서비스에서 BAD_REQUEST 던지면 400 반환`() {
        val reqDto = validTacticsReqDto()
        whenever(tacticsService.createTactic(any()))
            .thenThrow(CustomRuntimeException(HttpStatus.BAD_REQUEST, "each ball formations cannot be equals"))

        mockMvc.perform(
            post("/api/tactics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("each ball formations cannot be equals"))
    }

    @Test
    fun `GET api tactics - 전술 목록 조회 성공`() {
        val now = LocalDateTime.now()
        val list = listOf(
            TacticsListResDto(
                id = "id-1",
                name = "Offense 1",
                description = "desc 1",
                isPublic = true,
                createdAt = now,
                createdName = "tester"
            ),
            TacticsListResDto(
                id = "id-2",
                name = "Offense 2",
                description = null,
                isPublic = true,
                createdAt = now,
                createdName = null
            )
        )
        whenever(tacticsService.getTactics(eq(0), eq(10))).thenReturn(list)

        mockMvc.perform(
            get("/api/tactics")
                .param("start", "0")
                .param("limit", "10")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(2)))
            .andExpect(jsonPath("$.data[0].id").value("id-1"))
            .andExpect(jsonPath("$.data[0].name").value("Offense 1"))
            .andExpect(jsonPath("$.data[1].id").value("id-2"))
    }

    @Test
    fun `GET api tactics - 쿼리 파라미터 미지정 시 기본값 사용`() {
        whenever(tacticsService.getTactics(eq(0), eq(10))).thenReturn(emptyList())

        mockMvc.perform(get("/api/tactics"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(0)))
    }

    @Test
    fun `GET api tactics id - 전술 단건 조회 성공`() {
        val id = "tactic-123"
        val resDto = TacticsResDto(
            id = id,
            name = "My Tactic",
            description = "desc",
            states = TacticsResDto.States(
                formations = mapOf(
                    "step1" to TacticsResDto.Formation(
                        ball = TacticsResDto.Ball(100, 200),
                        players = listOf(TacticsResDto.Player(1L, 10, 20))
                    )
                ),
                playerInfo = listOf(
                    TacticsResDto.PlayerInfo(id = 1L, name = "p1", color = "#fff", showGhost = false)
                )
            ),
            isPublic = true,
            isHalfCourt = false,
            createdAt = LocalDateTime.now(),
            createdName = "tester",
            createdId = "member-1"
        )
        whenever(tacticsService.getTactic(id)).thenReturn(resDto)

        mockMvc.perform(get("/api/tactics/{id}", id))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(id))
            .andExpect(jsonPath("$.data.name").value("My Tactic"))
            .andExpect(jsonPath("$.data.isPublic").value(true))
            .andExpect(jsonPath("$.data.states.formations.step1.ball.x").value(100))
            .andExpect(jsonPath("$.data.states.playerInfo[0].id").value(1))
    }

    @Test
    fun `GET api tactics id - 존재하지 않으면 404 반환`() {
        val id = "missing-id"
        whenever(tacticsService.getTactic(id))
            .thenThrow(CustomRuntimeException(HttpStatus.NOT_FOUND))

        mockMvc.perform(get("/api/tactics/{id}", id))
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
    }

    private fun validTacticsReqDto(): TacticsReqDto {
        return TacticsReqDto(
            title = "Fast Break",
            description = "quick transition",
            formations = mapOf(
                "step1" to TacticsReqDto.Formation(
                    players = listOf(
                        TacticsReqDto.Player(id = 1L, x = 10, y = 20),
                        TacticsReqDto.Player(id = 2L, x = 30, y = 40)
                    ),
                    ball = TacticsReqDto.Ball(x = 50, y = 60)
                ),
                "step2" to TacticsReqDto.Formation(
                    players = listOf(
                        TacticsReqDto.Player(id = 1L, x = 100, y = 200),
                        TacticsReqDto.Player(id = 2L, x = 300, y = 400)
                    ),
                    ball = TacticsReqDto.Ball(x = 500, y = 600)
                )
            ),
            playerInfo = listOf(
                TacticsReqDto.PlayerInfo(id = 1L, color = "#fff", name = "p1"),
                TacticsReqDto.PlayerInfo(id = 2L, color = "#000", name = "p2")
            ),
            isPublic = false,
            isHalfCourt = false
        )
    }
}
