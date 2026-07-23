package me.courtboard.api.api.quicktactics

import me.courtboard.api.api.quicktactics.dto.QuickTacticsReqDto
import me.courtboard.api.api.quicktactics.dto.QuickTacticsResDto
import me.courtboard.api.api.quicktactics.service.QuickTacticsService
import me.courtboard.api.global.CourtboardContext
import me.courtboard.api.global.RequestContext
import me.courtboard.api.support.ControllerTestSupport
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class QuickTacticsControllerTest : ControllerTestSupport() {

    @Mock
    private lateinit var quickTacticsService: QuickTacticsService

    @InjectMocks
    private lateinit var quickTacticsController: QuickTacticsController

    private lateinit var mockMvc: MockMvc

    private val testMemberId = "11111111-2222-3333-4444-555555555555"

    @BeforeEach
    fun setUp() {
        mockMvc = buildMockMvc(quickTacticsController)
        CourtboardContext.setContext(RequestContext(memberId = testMemberId, role = "user"))
    }

    @AfterEach
    fun tearDown() {
        CourtboardContext.clearContext()
    }

    private fun validReqDto() = QuickTacticsReqDto(
        players = listOf(
            QuickTacticsReqDto.Player(id = 1, x = 100, y = 200),
            QuickTacticsReqDto.Player(id = 2, x = 150, y = 250),
        ),
        ball = QuickTacticsReqDto.Ball(x = 120, y = 180),
        playerInfo = listOf(
            QuickTacticsReqDto.PlayerInfo(id = 1, color = "red", name = "1"),
            QuickTacticsReqDto.PlayerInfo(id = 2, color = "blue", name = "2"),
        ),
        isHalfCourt = false,
    )

    @Test
    fun `GET api quick-tactics - 저장된 상태 조회 성공`() {
        val resDto = QuickTacticsResDto(
            states = mapOf("isHalfCourt" to false),
            updatedAt = LocalDateTime.now(),
        )
        whenever(quickTacticsService.getMyQuickTactics()).thenReturn(resDto)

        mockMvc.perform(get("/api/quick-tactics"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.states").exists())
    }

    @Test
    fun `GET api quick-tactics - 저장 이력 없으면 data null`() {
        whenever(quickTacticsService.getMyQuickTactics()).thenReturn(null)

        mockMvc.perform(get("/api/quick-tactics"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").doesNotExist())
    }

    @Test
    fun `PUT api quick-tactics - 상태 저장 성공`() {
        whenever(quickTacticsService.saveMyQuickTactics(any()))
            .thenReturn(mapOf("memberId" to testMemberId))

        mockMvc.perform(
            put("/api/quick-tactics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validReqDto()))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.memberId").value(testMemberId))
    }

    @Test
    fun `PUT api quick-tactics - 잘못된 player id 는 400`() {
        val invalidDto = validReqDto().copy(
            players = listOf(QuickTacticsReqDto.Player(id = 0, x = 10, y = 10)),
        )

        mockMvc.perform(
            put("/api/quick-tactics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
    }
}
