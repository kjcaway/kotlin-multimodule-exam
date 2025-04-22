package me.courtboard.api.api.member.service

import me.courtboard.api.component.CustomMailSender
import me.courtboard.api.global.error.CustomRuntimeException
import me.multimoduleexam.util.GeneratorUtil
import me.multimoduleexam.util.ValidationUtil
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val customMailSender: CustomMailSender
) {
    fun sendVerificationCodeToEmail(mailAddress: String) {
        if(!ValidationUtil.isValidEmail(mailAddress)) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid email address")
        }

        val code = GeneratorUtil.generateRandomNumber(6)

        val args = mapOf(
            "##mailAddress##" to mailAddress,
            "##code##" to code
        )

        customMailSender.sendMimeMessage("Code for sign up", mailAddress, args)
        // redisTemplate.opsForValue().set("email::${mailAddress}", code, 300, TimeUnit.SECONDS)
    }
}