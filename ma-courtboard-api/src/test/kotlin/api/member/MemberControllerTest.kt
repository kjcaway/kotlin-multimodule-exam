package api.member

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.courtboard.api.api.member.MemberController
import me.courtboard.api.api.member.dto.GoogleLoginReqDto
import me.courtboard.api.api.member.dto.MemberCodeCheckReqDto
import me.courtboard.api.api.member.dto.MemberLoginReqDto
import me.courtboard.api.api.member.dto.MemberReqDto
import me.courtboard.api.api.member.dto.MemberSendCodeReqDto
import me.courtboard.api.api.member.dto.RefreshTokenReqDto
import me.courtboard.api.api.member.service.GoogleAuthService
import me.courtboard.api.api.member.service.MemberMailService
import me.courtboard.api.api.member.service.MemberService
import me.courtboard.api.global.error.CustomExceptionHandler
import me.courtboard.api.global.error.CustomRuntimeException
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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockitoExtension::class)
class MemberControllerTest {

    @Mock
    private lateinit var memberService: MemberService

    @Mock
    private lateinit var memberMailService: MemberMailService

    @Mock
    private lateinit var googleAuthService: GoogleAuthService

    @InjectMocks
    private lateinit var memberController: MemberController

    private lateinit var mockMvc: MockMvc

    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(memberController)
            .setControllerAdvice(CustomExceptionHandler())
            .build()
    }

    @Test
    fun `GET api member check - 토큰 유효 시 success true 반환`() {
        mockMvc.perform(get("/api/member/check"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `POST api member - 회원가입 성공`() {
        val reqDto = validMemberReqDto()

        mockMvc.perform(
            post("/api/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `POST api member - email 형식 잘못되면 400 반환`() {
        val invalidBody = mapOf(
            "email" to "not-an-email",
            "name" to "tester",
            "passwd" to "password123",
            "code" to "123456"
        )

        mockMvc.perform(
            post("/api/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `POST api member - name 길이 미달 시 400 반환`() {
        val invalidBody = mapOf(
            "email" to "abc@gmail.com",
            "name" to "ab",
            "passwd" to "password123",
            "code" to "123456"
        )

        mockMvc.perform(
            post("/api/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `POST api member - passwd 길이 미달 시 400 반환`() {
        val invalidBody = mapOf(
            "email" to "abc@gmail.com",
            "name" to "tester",
            "passwd" to "short",
            "code" to "123456"
        )

        mockMvc.perform(
            post("/api/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `POST api member - code 자릿수 위반 시 400 반환`() {
        val invalidBody = mapOf(
            "email" to "abc@gmail.com",
            "name" to "tester",
            "passwd" to "password123",
            "code" to "12345"
        )

        mockMvc.perform(
            post("/api/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `POST api member - 인증 코드 잘못되면 400 반환`() {
        val reqDto = validMemberReqDto()
        doThrow(CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid verification code"))
            .whenever(memberService).createNewMember(any())

        mockMvc.perform(
            post("/api/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Invalid verification code"))
    }

    @Test
    fun `POST api member - 이미 가입된 이메일이면 400 반환`() {
        val reqDto = validMemberReqDto()
        doThrow(CustomRuntimeException(HttpStatus.BAD_REQUEST, "Email already exists"))
            .whenever(memberService).createNewMember(any())

        mockMvc.perform(
            post("/api/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Email already exists"))
    }

    @Test
    fun `POST api member login - 로그인 성공 시 토큰 반환`() {
        val reqDto = MemberLoginReqDto(email = "abc@gmail.com", password = "password123")
        val tokens = mapOf(
            "access_token" to "access-token-value",
            "refresh_token" to "refresh-token-value"
        )
        whenever(memberService.getToken(any())).thenReturn(tokens)

        mockMvc.perform(
            post("/api/member/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.access_token").value("access-token-value"))
            .andExpect(jsonPath("$.data.refresh_token").value("refresh-token-value"))
    }

    @Test
    fun `POST api member login - 잘못된 자격 증명 시 400 반환`() {
        val reqDto = MemberLoginReqDto(email = "abc@gmail.com", password = "password123")
        whenever(memberService.getToken(any()))
            .thenThrow(CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid email or password"))

        mockMvc.perform(
            post("/api/member/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Invalid email or password"))
    }

    @Test
    fun `POST api member login - email 형식 잘못되면 400 반환`() {
        val invalidBody = mapOf(
            "email" to "no-at-sign",
            "password" to "password123"
        )

        mockMvc.perform(
            post("/api/member/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `POST api member google-login - 구글 로그인 성공`() {
        val reqDto = GoogleLoginReqDto(credential = "fake-credential")
        val tokens = mapOf(
            "access_token" to "google-access",
            "refresh_token" to "google-refresh"
        )
        whenever(googleAuthService.loginWithGoogle(eq("fake-credential"))).thenReturn(tokens)

        mockMvc.perform(
            post("/api/member/google-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.access_token").value("google-access"))
    }

    @Test
    fun `POST api member google-login - credential 누락 시 400 반환`() {
        val invalidBody = mapOf("credential" to "")

        mockMvc.perform(
            post("/api/member/google-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `POST api member google-login - 잘못된 자격증명 시 401 반환`() {
        val reqDto = GoogleLoginReqDto(credential = "invalid-credential")
        whenever(googleAuthService.loginWithGoogle(eq("invalid-credential")))
            .thenThrow(CustomRuntimeException(HttpStatus.UNAUTHORIZED, "Invalid Google credential"))

        mockMvc.perform(
            post("/api/member/google-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `POST api member refresh - 토큰 재발급 성공`() {
        val refreshToken = "a".repeat(32)
        val reqDto = RefreshTokenReqDto(refreshToken = refreshToken)
        val tokens = mapOf(
            "access_token" to "new-access",
            "refresh_token" to "new-refresh"
        )
        whenever(memberService.getTokenByRefreshToken(any())).thenReturn(tokens)

        mockMvc.perform(
            post("/api/member/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.access_token").value("new-access"))
            .andExpect(jsonPath("$.data.refresh_token").value("new-refresh"))
    }

    @Test
    fun `POST api member refresh - refreshToken 길이 미달 시 400 반환`() {
        val invalidBody = mapOf("refreshToken" to "short")

        mockMvc.perform(
            post("/api/member/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `POST api member refresh - 만료되거나 잘못된 토큰이면 400 반환`() {
        val refreshToken = "a".repeat(32)
        val reqDto = RefreshTokenReqDto(refreshToken = refreshToken)
        whenever(memberService.getTokenByRefreshToken(any()))
            .thenThrow(CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid refresh token"))

        mockMvc.perform(
            post("/api/member/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Invalid refresh token"))
    }

    @Test
    fun `POST api member send-verification-code - 인증 코드 발송 성공`() {
        val reqDto = MemberSendCodeReqDto(email = "abc@gmail.com")

        mockMvc.perform(
            post("/api/member/send-verification-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `POST api member send-verification-code - email 형식 잘못되면 400 반환`() {
        val invalidBody = mapOf("email" to "no-at-sign")

        mockMvc.perform(
            post("/api/member/send-verification-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `POST api member send-verification-code - 이미 가입된 이메일이면 400 반환`() {
        val reqDto = MemberSendCodeReqDto(email = "abc@gmail.com")
        doThrow(CustomRuntimeException(HttpStatus.BAD_REQUEST, "Email already exists"))
            .whenever(memberMailService).sendVerificationCodeToEmail(eq("abc@gmail.com"))

        mockMvc.perform(
            post("/api/member/send-verification-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Email already exists"))
    }

    @Test
    fun `POST api member check-verification-code - 인증 코드 확인 성공`() {
        val reqDto = MemberCodeCheckReqDto(email = "abc@gmail.com", code = "123456")
        whenever(memberMailService.checkVerificationCode(eq("abc@gmail.com"), eq("123456")))
            .thenReturn(true)

        mockMvc.perform(
            post("/api/member/check-verification-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value(true))
    }

    @Test
    fun `POST api member check-verification-code - 잘못된 코드면 400 반환`() {
        val reqDto = MemberCodeCheckReqDto(email = "abc@gmail.com", code = "999999")
        whenever(memberMailService.checkVerificationCode(eq("abc@gmail.com"), eq("999999")))
            .thenThrow(CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid verification code"))

        mockMvc.perform(
            post("/api/member/check-verification-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Invalid verification code"))
    }

    @Test
    fun `POST api member check-verification-code - code 자릿수 위반 시 400 반환`() {
        val invalidBody = mapOf(
            "email" to "abc@gmail.com",
            "code" to "12345"
        )

        mockMvc.perform(
            post("/api/member/check-verification-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    private fun validMemberReqDto(): MemberReqDto {
        return MemberReqDto(
            email = "abc@gmail.com",
            name = "tester",
            avatarUrl = null,
            passwd = "password123",
            code = "123456"
        )
    }
}
