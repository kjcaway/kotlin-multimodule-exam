package api.member

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.courtboard.api.api.member.MemberAdminController
import me.courtboard.api.api.member.dto.MemberAdminListResDto
import me.courtboard.api.api.member.dto.MemberGrantReqDto
import me.courtboard.api.api.member.dto.MemberRoleUpdateReqDto
import me.courtboard.api.api.member.service.MemberService
import me.courtboard.api.global.error.CustomExceptionHandler
import me.courtboard.api.global.error.CustomRuntimeException
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class MemberAdminControllerTest {

    @Mock
    private lateinit var memberService: MemberService

    @InjectMocks
    private lateinit var memberAdminController: MemberAdminController

    private lateinit var mockMvc: MockMvc

    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(memberAdminController)
            .setControllerAdvice(CustomExceptionHandler())
            .build()
    }

    @Test
    fun `GET api admin users - 회원 목록 조회 성공`() {
        val now = LocalDateTime.now()
        val list = listOf(
            MemberAdminListResDto(
                id = "uuid-1",
                email = "a@gmail.com",
                name = "유저A",
                provider = "local",
                role = "admin",
                createdAt = now,
                lastloginAt = now,
            ),
            MemberAdminListResDto(
                id = "uuid-2",
                email = "b@gmail.com",
                name = "유저B",
                provider = "google",
                role = "user",
                createdAt = now,
                lastloginAt = null,
            ),
        )
        whenever(memberService.getAllMembers(eq(0), eq(10))).thenReturn(list)

        mockMvc.perform(
            get("/api/admin/users")
                .param("start", "0")
                .param("limit", "10")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(2)))
            .andExpect(jsonPath("$.data[0].id").value("uuid-1"))
            .andExpect(jsonPath("$.data[0].role").value("admin"))
            .andExpect(jsonPath("$.data[1].provider").value("google"))
            .andExpect(jsonPath("$.data[1].lastloginAt").doesNotExist())
    }

    @Test
    fun `GET api admin users - 쿼리 파라미터 미지정 시 기본값 사용`() {
        whenever(memberService.getAllMembers(eq(0), eq(10))).thenReturn(emptyList())

        mockMvc.perform(get("/api/admin/users"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(0)))
    }

    @Test
    fun `GET api admin users count - 회원 수 조회 성공`() {
        whenever(memberService.getAllMembersCount()).thenReturn(123L)

        mockMvc.perform(get("/api/admin/users/count"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.count").value(123))
    }

    @Test
    fun `POST api admin grant-role - 역할 부여 성공`() {
        val reqDto = MemberGrantReqDto(email = "a@gmail.com", role = "user")

        mockMvc.perform(
            post("/api/admin/grant-role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `POST api admin grant-role - 사용자 없으면 400 반환`() {
        val reqDto = MemberGrantReqDto(email = "missing@gmail.com", role = "user")
        doThrow(CustomRuntimeException(HttpStatus.BAD_REQUEST, "User not found"))
            .whenever(memberService).grantRoleForUser(eq("missing@gmail.com"), eq("user"))

        mockMvc.perform(
            post("/api/admin/grant-role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("User not found"))
    }

    @Test
    fun `PUT api admin users id role - 역할 수정 성공`() {
        val id = "uuid-1"
        val reqDto = MemberRoleUpdateReqDto(role = "admin")

        mockMvc.perform(
            put("/api/admin/users/{id}/role", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `PUT api admin users id role - role 누락 시 400 반환`() {
        val id = "uuid-1"
        val invalidBody = mapOf("role" to "")

        mockMvc.perform(
            put("/api/admin/users/{id}/role", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `PUT api admin users id role - 사용자 없으면 404 반환`() {
        val id = "missing-id"
        val reqDto = MemberRoleUpdateReqDto(role = "user")
        doThrow(CustomRuntimeException(HttpStatus.NOT_FOUND, "User not found"))
            .whenever(memberService).updateMemberRole(eq("missing-id"), eq("user"))

        mockMvc.perform(
            put("/api/admin/users/{id}/role", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("User not found"))
    }
}
