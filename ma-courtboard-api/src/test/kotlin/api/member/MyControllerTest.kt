package api.member

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.courtboard.api.api.member.dto.AvatarUpdateResDto
import me.courtboard.api.api.member.dto.ChangeNameReqDto
import me.courtboard.api.api.member.dto.ChangePasswordReqDto
import me.courtboard.api.api.member.dto.MemberInfoResDto
import me.courtboard.api.api.member.service.MemberAvatarService
import me.courtboard.api.api.member.service.MemberService
import me.courtboard.api.api.my.MyController
import me.courtboard.api.api.tactics.dto.TacticsListResDto
import me.courtboard.api.api.tactics.service.TacticsService
import me.courtboard.api.global.CourtboardContext
import me.courtboard.api.global.RequestContext
import me.courtboard.api.global.error.CustomExceptionHandler
import me.courtboard.api.global.error.CustomRuntimeException
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class MyControllerTest {

    @Mock
    private lateinit var memberService: MemberService

    @Mock
    private lateinit var tacticsService: TacticsService

    @Mock
    private lateinit var memberAvatarService: MemberAvatarService

    @InjectMocks
    private lateinit var myController: MyController

    private lateinit var mockMvc: MockMvc

    private val objectMapper = jacksonObjectMapper()

    private val testMemberId = "11111111-2222-3333-4444-555555555555"

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(myController)
            .setControllerAdvice(CustomExceptionHandler())
            .build()
        CourtboardContext.setContext(RequestContext(memberId = testMemberId, role = "user"))
    }

    @AfterEach
    fun tearDown() {
        CourtboardContext.clearContext()
    }

    @Test
    fun `GET api my tactics - 내 전술 목록 조회 성공`() {
        val now = LocalDateTime.now()
        val list = listOf(
            TacticsListResDto(
                id = "id-1",
                name = "내 전술",
                description = null,
                isPublic = false,
                createdAt = now,
            ),
        )
        whenever(tacticsService.getMyTactics()).thenReturn(list)

        mockMvc.perform(get("/api/my/tactics"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(1)))
            .andExpect(jsonPath("$.data[0].id").value("id-1"))
    }

    @Test
    fun `GET api my info - 내 정보 조회 성공`() {
        val now = LocalDateTime.now()
        val resDto = MemberInfoResDto(
            id = testMemberId,
            email = "abc@gmail.com",
            name = "tester",
            avatarUrl = "/uploads/avatar/$testMemberId.png",
            provider = "local",
            createdAt = now,
            lastloginAt = now,
        )
        whenever(memberService.getMyInfo()).thenReturn(resDto)

        mockMvc.perform(get("/api/my/info"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(testMemberId))
            .andExpect(jsonPath("$.data.email").value("abc@gmail.com"))
            .andExpect(jsonPath("$.data.name").value("tester"))
    }

    @Test
    fun `GET api my info - 회원 없으면 404 반환`() {
        whenever(memberService.getMyInfo())
            .thenThrow(CustomRuntimeException(HttpStatus.NOT_FOUND, "not found member"))

        mockMvc.perform(get("/api/my/info"))
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `PUT api my info - 이름 변경 성공`() {
        val reqDto = ChangeNameReqDto(name = "new name")

        mockMvc.perform(
            put("/api/my/info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `PUT api my info - name 길이 미달 시 400 반환`() {
        val invalidBody = mapOf("name" to "ab")

        mockMvc.perform(
            put("/api/my/info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `PUT api my password - 비밀번호 변경 성공`() {
        val reqDto = ChangePasswordReqDto(
            currentPassword = "oldPassword1",
            newPassword = "newPassword1",
        )

        mockMvc.perform(
            put("/api/my/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `PUT api my password - 현재 비밀번호 틀리면 400 반환`() {
        val reqDto = ChangePasswordReqDto(
            currentPassword = "wrongPassword1",
            newPassword = "newPassword1",
        )
        doThrow(CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid email or password"))
            .whenever(memberService).changePassword(any())

        mockMvc.perform(
            put("/api/my/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Invalid email or password"))
    }

    @Test
    fun `PUT api my password - 신규 비밀번호 길이 미달 시 400 반환`() {
        val invalidBody = mapOf(
            "currentPassword" to "oldPassword1",
            "newPassword" to "short"
        )

        mockMvc.perform(
            put("/api/my/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `DELETE api my account - 회원 탈퇴 성공`() {
        mockMvc.perform(delete("/api/my/account"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `DELETE api my account - 회원 없으면 404 반환`() {
        doThrow(CustomRuntimeException(HttpStatus.NOT_FOUND, "not found member"))
            .whenever(memberService).deleteMember()

        mockMvc.perform(delete("/api/my/account"))
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `POST api my avatar - 아바타 업로드 성공`() {
        val file = MockMultipartFile(
            "file", "avatar.png", "image/png", byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
        )
        val resDto = AvatarUpdateResDto(
            avatarUrl = "/uploads/avatar/$testMemberId.png",
            accessToken = "new-access-token",
        )
        whenever(memberAvatarService.uploadAvatar(eq(testMemberId), any())).thenReturn(resDto)

        mockMvc.perform(multipart("/api/my/avatar").file(file))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.avatarUrl").value("/uploads/avatar/$testMemberId.png"))
            .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
    }

    @Test
    fun `POST api my avatar - 구글 계정이면 403 반환`() {
        val file = MockMultipartFile(
            "file", "avatar.png", "image/png", byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
        )
        whenever(memberAvatarService.uploadAvatar(eq(testMemberId), any()))
            .thenThrow(CustomRuntimeException(HttpStatus.FORBIDDEN, "Google account avatar cannot be edited"))

        mockMvc.perform(multipart("/api/my/avatar").file(file))
            .andDo(print())
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Google account avatar cannot be edited"))
    }

    @Test
    fun `DELETE api my avatar - 아바타 삭제 성공`() {
        val resDto = AvatarUpdateResDto(
            avatarUrl = null,
            accessToken = "new-access-token",
        )
        whenever(memberAvatarService.deleteAvatar(eq(testMemberId))).thenReturn(resDto)

        mockMvc.perform(delete("/api/my/avatar"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.avatarUrl").doesNotExist())
            .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
    }

    @Test
    fun `DELETE api my avatar - 구글 계정이면 403 반환`() {
        whenever(memberAvatarService.deleteAvatar(eq(testMemberId)))
            .thenThrow(CustomRuntimeException(HttpStatus.FORBIDDEN, "Google account avatar cannot be edited"))

        mockMvc.perform(delete("/api/my/avatar"))
            .andDo(print())
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Google account avatar cannot be edited"))
    }
}
