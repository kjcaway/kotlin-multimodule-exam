package me.courtboard.api.api.member.service

import me.courtboard.api.api.member.dto.*
import me.courtboard.api.api.member.dto.MemberInfoResDto.Companion.toMemberInfoResDto
import me.courtboard.api.api.member.entity.MemberEntity
import me.courtboard.api.api.member.repository.MemberInfoRepository
import me.courtboard.api.api.member.repository.MemberRepository
import me.courtboard.api.component.JwtProvider
import me.courtboard.api.global.Constants
import me.courtboard.api.global.CourtboardContext
import me.courtboard.api.global.error.CustomRuntimeException
import me.courtboard.api.util.PasswordUtil
import org.casbin.jcasbin.main.Enforcer
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*


@Service
class MemberService(
    private val memberMailService: MemberMailService,
    private val memberRepository: MemberRepository,
    private val memberInfoRepository: MemberInfoRepository,
    private val jwtProvider: JwtProvider,
    private val enforcer: Enforcer
) {

    @Transactional
    fun getToken(dto: MemberLoginReqDto): Map<String, String> {
        val memberInfo = memberInfoRepository.findByEmail(dto.email)
            ?: throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid email or password")
        val member = memberRepository.findById(memberInfo.id)
            ?: throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid email or password")

        if (!PasswordUtil.checkPassword(dto.password, member.get().passwd)) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid email or password")
        }

        val accessToken = jwtProvider.generateAccessToken( dto.email, mapOf(
            "id" to memberInfo.id.toString(),
            "name" to memberInfo.name!!,
            "email" to memberInfo.email!!
        ))
        val refreshToken = jwtProvider.generateRefreshToken(dto.email)

        // last login, refresh token update
        memberInfo.lastloginAt = LocalDateTime.now()
        memberInfo.refreshToken = refreshToken
        memberInfoRepository.save(memberInfo)

        return mapOf(
            "access_token" to accessToken,
            "refresh_token" to refreshToken
        )
    }

    fun getTokenByRefreshToken(dto: RefreshTokenReqDto): Map<String, String> {
        try {
            val claims = jwtProvider.getAllClaimsFromToken(dto.refreshToken)

            if (!jwtProvider.isRefreshToken(dto.refreshToken)) {
                throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid refresh token")
            }

            val email = claims.subject

            val memberInfo = memberInfoRepository.findByEmailAndRefreshToken(email, dto.refreshToken)
                ?: throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid email or refresh token")

            val accessToken = jwtProvider.generateAccessToken(
                email, mapOf(
                    "id" to memberInfo.id.toString(),
                    "name" to memberInfo.name!!,
                    "email" to memberInfo.email!!
                )
            )
            val refreshToken = jwtProvider.generateRefreshToken(email)

            // last login, refresh token update
            memberInfo.lastloginAt = LocalDateTime.now()
            memberInfo.refreshToken = refreshToken
            memberInfoRepository.save(memberInfo)

            return mapOf(
                "access_token" to accessToken,
                "refresh_token" to refreshToken
            )
        } catch (e: Exception) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid refresh token", e)
        }
    }

    @Transactional
    fun createNewMember(memberReqDto: MemberReqDto) {
        if (!memberMailService.checkVerificationCode(memberReqDto.email, memberReqDto.code)) {
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

        memberMailService.removeVerificationCode(memberReqDto.email)

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

    fun changeName(dto: ChangeNameReqDto) {
        if (!CourtboardContext.isLogin()) {
            throw CustomRuntimeException(HttpStatus.FORBIDDEN)
        }
        val memberId = CourtboardContext.getContext().memberId
        val memberInfo = memberInfoRepository.findById(UUID.fromString(memberId))
            ?: throw CustomRuntimeException(HttpStatus.NOT_FOUND, "not found member")

        if (dto.name.isBlank()) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Name cannot be empty")
        }

        memberInfo.get().name = dto.name
        memberInfoRepository.save(memberInfo.get())
    }

    @Transactional
    fun changePassword(dto: ChangePasswordReqDto) {
        if (!CourtboardContext.isLogin()) {
            throw CustomRuntimeException(HttpStatus.FORBIDDEN)
        }
        val memberId = CourtboardContext.getContext().memberId
        val member = memberRepository.findById(UUID.fromString(memberId))
            ?: throw CustomRuntimeException(HttpStatus.NOT_FOUND, "not found member")

        if (!PasswordUtil.checkPassword(dto.currentPassword, member.get().passwd)) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid email or password")
        }

        member.get().passwd = PasswordUtil.hashPassword(dto.newPassword)
        memberRepository.save(member.get())
    }

    @Transactional
    fun deleteMember() {
        if (!CourtboardContext.isLogin()) {
            throw CustomRuntimeException(HttpStatus.FORBIDDEN)
        }
        val memberId = CourtboardContext.getContext().memberId
        val memberInfo = memberInfoRepository.findById(UUID.fromString(memberId))
            ?: throw CustomRuntimeException(HttpStatus.NOT_FOUND, "not found member")

        memberInfoRepository.delete(memberInfo.get())
        memberRepository.deleteById(UUID.fromString(memberId))

        enforcer.deleteUser(memberId)
    }

    fun checkToken() {
        if (!CourtboardContext.isLogin()) {
            throw CustomRuntimeException(HttpStatus.UNAUTHORIZED, "Invalid token")
        }
    }
}
