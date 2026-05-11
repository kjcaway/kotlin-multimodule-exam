package api.tactics

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.courtboard.api.api.tactics.TacticsAdminController
import me.courtboard.api.api.tactics.dto.TacticsListResDto
import me.courtboard.api.api.tactics.dto.TacticsTemplateToggleReqDto
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class TacticsAdminControllerTest {

    @Mock
    private lateinit var tacticsService: TacticsService

    @InjectMocks
    private lateinit var tacticsAdminController: TacticsAdminController

    private lateinit var mockMvc: MockMvc

    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tacticsAdminController)
            .setControllerAdvice(CustomExceptionHandler())
            .build()
    }

    @Test
    fun `GET api admin tactics - 전체 전술 목록 조회 성공`() {
        val now = LocalDateTime.now()
        val list = listOf(
            TacticsListResDto(
                id = "id-1",
                name = "Public Tactic",
                description = "desc",
                isPublic = true,
                isTemplate = false,
                createdAt = now,
                createdName = "user-a"
            ),
            TacticsListResDto(
                id = "id-2",
                name = "Private Tactic",
                description = null,
                isPublic = false,
                isTemplate = true,
                createdAt = now,
                createdName = null
            ),
        )
        whenever(tacticsService.getAllTactics(eq(0), eq(10))).thenReturn(list)

        mockMvc.perform(
            get("/api/admin/tactics")
                .param("start", "0")
                .param("limit", "10")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(2)))
            .andExpect(jsonPath("$.data[0].id").value("id-1"))
            .andExpect(jsonPath("$.data[0].isPublic").value(true))
            .andExpect(jsonPath("$.data[1].isTemplate").value(true))
    }

    @Test
    fun `GET api admin tactics - 쿼리 파라미터 미지정 시 기본값 사용`() {
        whenever(tacticsService.getAllTactics(eq(0), eq(10))).thenReturn(emptyList())

        mockMvc.perform(get("/api/admin/tactics"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(0)))
    }

    @Test
    fun `GET api admin tactics count - 전술 개수 조회 성공`() {
        whenever(tacticsService.getAllTacticsCount()).thenReturn(42L)

        mockMvc.perform(get("/api/admin/tactics/count"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.count").value(42))
    }

    @Test
    fun `PUT api admin tactics template - 템플릿 토글 성공`() {
        val id = "tactic-toggle"
        val reqDto = TacticsTemplateToggleReqDto(id = id)
        whenever(tacticsService.toggleTemplate(eq(id)))
            .thenReturn(mapOf("id" to id, "isTemplate" to true))

        mockMvc.perform(
            put("/api/admin/tactics/template")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(id))
            .andExpect(jsonPath("$.data.isTemplate").value(true))
    }

    @Test
    fun `PUT api admin tactics template - id 누락 시 400 반환`() {
        val invalidBody = mapOf("id" to "")

        mockMvc.perform(
            put("/api/admin/tactics/template")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `PUT api admin tactics template - 존재하지 않으면 404 반환`() {
        val id = "missing-id"
        val reqDto = TacticsTemplateToggleReqDto(id = id)
        whenever(tacticsService.toggleTemplate(eq(id)))
            .thenThrow(CustomRuntimeException(HttpStatus.NOT_FOUND))

        mockMvc.perform(
            put("/api/admin/tactics/template")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
    }
}
