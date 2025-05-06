package me.courtboard.api.api.member.service

import me.courtboard.api.api.member.dto.MemberInfoResDto
import me.courtboard.api.api.member.dto.MemberInfoResDto.Companion.toMemberInfoResDto
import me.courtboard.api.api.member.dto.MemberLoginReqDto
import me.courtboard.api.api.member.dto.MemberReqDto
import me.courtboard.api.api.member.entity.MemberEntity
import me.courtboard.api.api.member.repository.MemberInfoRepository
import me.courtboard.api.api.member.repository.MemberRepository
import me.courtboard.api.component.CustomMailSender
import me.courtboard.api.component.JwtProvider
import me.courtboard.api.global.Constants
import me.courtboard.api.global.CourtboardContext
import me.courtboard.api.global.error.CustomRuntimeException
import me.courtboard.api.util.PasswordUtil
import me.multimoduleexam.cache.LocalStorage
import me.multimoduleexam.util.GeneratorUtil
import me.multimoduleexam.util.ValidationUtil
import org.casbin.jcasbin.main.Enforcer
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*


@Service
class MemberService(
    private val customMailSender: CustomMailSender,
    private val localStorage: LocalStorage<String, String>,
    private val memberRepository: MemberRepository,
    private val memberInfoRepository: MemberInfoRepository,
    private val jwtProvider: JwtProvider,
    private val enforcer: Enforcer
) {

    private val logger = LoggerFactory.getLogger(MemberService::class.java)

    @Transactional
    fun getToken(dto: MemberLoginReqDto): Map<String,String> {
        val memberInfo = memberInfoRepository.findByEmail(dto.email)
            ?: throw CustomRuntimeException(HttpStatus.UNAUTHORIZED, "Invalid email or password")
        val member = memberRepository.findById(memberInfo.id)
            ?: throw CustomRuntimeException(HttpStatus.UNAUTHORIZED, "Invalid email or password")

        if (!PasswordUtil.checkPassword(dto.password, member.get().passwd)) {
            throw CustomRuntimeException(HttpStatus.UNAUTHORIZED, "Invalid email or password")
        }

        val accessToken = jwtProvider.generateAccessToken( dto.email, mapOf(
            "id" to memberInfo.id.toString(),
            "name" to memberInfo.name!!,
            "email" to memberInfo.email!!
        ))
        val refreshToken = jwtProvider.generateRefreshToken(dto.email)

        // last login update
        memberInfo.lastloginAt = LocalDateTime.now()
        memberInfoRepository.save(memberInfo)

        return mapOf(
            "access_token" to accessToken,
            "refresh_token" to refreshToken
        )
    }

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

        // customMailSender.sendMimeMessage("Code for sign up", mailAddress, args)

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

    @Transactional
    fun createNewMember(memberReqDto: MemberReqDto) {
        if (!checkVerificationCode(memberReqDto.email, memberReqDto.code)) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid verification code")
        }

        if (memberInfoRepository.existsByEmail(memberReqDto.email)) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Email already exists")
        }

        val member = memberInfoRepository.save(memberReqDto.toEntity())
        val hashedPassword = PasswordUtil.hashPassword(memberReqDto.passwd)
        val memberEntity = MemberEntity(
            id = member.id,
            passwd = hashedPassword
        )
        memberRepository.save(memberEntity)

        localStorage.remove(memberReqDto.email)

        grantRoleForUser(member.email!!, Constants.ROLE_USER)
    }

    fun grantRoleForUser(email: String, role: String) {
        val info = memberInfoRepository.findByEmail(email)
            ?: throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "User not found")
        enforcer.addRoleForUserInDomain(info.id.toString(), role, Constants.COURTBOARD)
        enforcer.roleManager.printRoles()
    }

    fun getMyInfo(): MemberInfoResDto {
        val memberId = CourtboardContext.getContext().memberId
        val memberInfo = memberInfoRepository.findById(UUID.fromString(memberId))
            ?: throw CustomRuntimeException(HttpStatus.NOT_FOUND, "not found member")
        return memberInfo.get().toMemberInfoResDto()
    }
}