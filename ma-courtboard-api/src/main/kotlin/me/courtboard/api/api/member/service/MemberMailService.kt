package me.courtboard.api.api.member.service

import me.courtboard.api.api.member.repository.MemberInfoRepository
import me.courtboard.api.component.CustomMailSender
import me.courtboard.api.global.error.CustomRuntimeException
import me.multimoduleexam.cache.LocalStorage
import me.multimoduleexam.util.GeneratorUtil
import me.multimoduleexam.util.ValidationUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class MemberMailService(
    private val customMailSender: CustomMailSender,
    private val localStorage: LocalStorage<String, String>,
    private val memberInfoRepository: MemberInfoRepository,
    @Value("\${spring.profiles.active}")
    private val activeProfile: String
) {
    private val logger = LoggerFactory.getLogger(MemberMailService::class.java)

    fun sendVerificationCodeToEmail(mailAddress: String) {
        if (!ValidationUtil.isValidEmail(mailAddress)) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid email address")
        }

        if (memberInfoRepository.existsByEmail(mailAddress)) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Email already exists")
        }

        val code = GeneratorUtil.generateRandomNumber(6)

        val args = mapOf(
            "##mailAddress##" to mailAddress,
            "##code##" to code
        )

        logger.info("Sending verification code to $mailAddress -> $code")

        if (activeProfile == "prod") {
            customMailSender.sendMimeMessage("Code for sign up", mailAddress, args)
        }

        localStorage.put(mailAddress, code)
    }

    fun checkVerificationCode(mailAddress: String, code: String): Boolean {
        if (!ValidationUtil.isValidEmail(mailAddress)) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid email address")
        }

        val cachedCode = localStorage.get(mailAddress)
        if (cachedCode == null || code != cachedCode) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid verification code")
        }
        return true
    }

    fun removeVerificationCode(mailAddress: String) {
        localStorage.remove(mailAddress)
    }
}
