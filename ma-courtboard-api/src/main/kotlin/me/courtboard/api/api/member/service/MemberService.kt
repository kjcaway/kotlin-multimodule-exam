package me.courtboard.api.api.member.service

import me.courtboard.api.api.member.dto.*
import me.courtboard.api.api.member.dto.MemberAdminListResDto.Companion.toMemberAdminListResDto
import me.courtboard.api.api.member.dto.MemberInfoResDto.Companion.toMemberInfoResDto
import me.courtboard.api.api.member.entity.MemberEntity
import me.courtboard.api.api.member.repository.MemberInfoRepository
import me.courtboard.api.api.member.repository.MemberRepository
import me.courtboard.api.component.JwtProvider
import me.courtboard.api.global.Constants
import me.courtboard.api.global.CourtboardContext
import me.courtboard.api.global.error.CustomRuntimeException
import me.courtboard.api.util.PasswordUtil
import me.courtboard.api.util.resolveRole
import me.courtboard.api.util.toJwtClaims
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
            .orElseThrow { CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid email or password") }

        if (!PasswordUtil.checkPassword(dto.password, member.passwd)) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid email or password")
        }

        val accessToken = jwtProvider.generateAccessToken(
            dto.email,
            memberInfo.toJwtClaims(enforcer.resolveRole(memberInfo.id.toString()))
        )
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
                email,
                memberInfo.toJwtClaims(enforcer.resolveRole(memberInfo.id.toString()))
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
            .orElseThrow { CustomRuntimeException(HttpStatus.NOT_FOUND, "not found member") }
        return memberInfo.toMemberInfoResDto()
    }

    fun changeName(dto: ChangeNameReqDto) {
        val memberId = CourtboardContext.getContext().memberId
        val memberInfo = memberInfoRepository.findById(UUID.fromString(memberId))
            .orElseThrow { CustomRuntimeException(HttpStatus.NOT_FOUND, "not found member") }

        if (dto.name.isBlank()) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Name cannot be empty")
        }

        memberInfo.name = dto.name
        memberInfoRepository.save(memberInfo)
    }

    @Transactional
    fun changePassword(dto: ChangePasswordReqDto) {
        val memberId = CourtboardContext.getContext().memberId
        val member = memberRepository.findById(UUID.fromString(memberId))
            .orElseThrow { CustomRuntimeException(HttpStatus.NOT_FOUND, "not found member") }

        if (!PasswordUtil.checkPassword(dto.currentPassword, member.passwd)) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid email or password")
        }

        member.passwd = PasswordUtil.hashPassword(dto.newPassword)
        memberRepository.save(member)
    }

    @Transactional
    fun deleteMember() {
        val memberId = CourtboardContext.getContext().memberId
        val memberInfo = memberInfoRepository.findById(UUID.fromString(memberId))
            .orElseThrow { CustomRuntimeException(HttpStatus.NOT_FOUND, "not found member") }

        memberInfoRepository.delete(memberInfo)
        memberRepository.deleteById(UUID.fromString(memberId))

        enforcer.deleteUser(memberId)
    }

    fun getAllMembers(start: Int, limit: Int): List<MemberAdminListResDto> {
        val pageable = org.springframework.data.domain.PageRequest.of(
            start / limit.coerceAtLeast(1),
            limit.coerceAtLeast(1),
            org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")
        )
        return memberInfoRepository.findAll(pageable).content.map {
            it.toMemberAdminListResDto(enforcer.resolveRole(it.id.toString()))
        }
    }

    fun updateMemberRole(memberId: String, role: String) {
        memberInfoRepository.findById(UUID.fromString(memberId))
            .orElseThrow { CustomRuntimeException(HttpStatus.NOT_FOUND, "User not found") }
        enforcer.getRolesForUserInDomain(memberId, Constants.COURTBOARD).forEach { existingRole ->
            enforcer.deleteRoleForUserInDomain(memberId, existingRole, Constants.COURTBOARD)
        }
        enforcer.addRoleForUserInDomain(memberId, role, Constants.COURTBOARD)
    }

    fun getAllMembersCount(): Long {
        return memberInfoRepository.count()
    }
}
