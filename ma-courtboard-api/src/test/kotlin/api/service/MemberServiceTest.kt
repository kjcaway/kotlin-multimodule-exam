package api.service

import me.courtboard.api.api.member.repository.MemberInfoRepository
import me.courtboard.api.api.member.service.MemberMailService
import me.courtboard.api.component.CustomMailSender
import me.multimoduleexam.cache.LocalStorage
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MemberServiceTest(
    @Mock
    val customMailSender: CustomMailSender,
    @Mock
    val localStorage: LocalStorage<String, String>,
    @Mock
    val memberInfoRepository: MemberInfoRepository,
) {
    private lateinit var memberMailService: MemberMailService

    @BeforeAll
    fun setup() {
        memberMailService = MemberMailService(customMailSender, localStorage, memberInfoRepository, "prod")
    }

    @Test
    fun `send verifiy code mail test`() {
        val to = "abc@gmail.com"
        memberMailService.sendVerificationCodeToEmail(to)

        val stringCaptor = argumentCaptor<String>()
        val mapCaptor = argumentCaptor<Map<String, String>>()

        verify(customMailSender, times(1)).sendMimeMessage(
            stringCaptor.capture(),
            stringCaptor.capture(),
            mapCaptor.capture()
        )

        val strValues = stringCaptor.allValues
        println(strValues)
        val mapValues = mapCaptor.allValues
        println(mapValues)

        Assertions.assertEquals("Code for sign up", strValues[0])
        Assertions.assertEquals("abc@gmail.com", strValues[1])
    }
}