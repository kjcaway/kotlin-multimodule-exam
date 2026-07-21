package me.courtboard.api.api.member.service

import me.courtboard.api.api.member.dto.AvatarUpdateResDto
import me.courtboard.api.api.member.entity.MemberInfoEntity
import me.courtboard.api.api.member.repository.MemberInfoRepository
import me.courtboard.api.component.JwtProvider
import me.courtboard.api.global.Constants
import me.courtboard.api.global.error.CustomRuntimeException
import me.courtboard.api.util.ImageValidationUtil
import me.courtboard.api.util.resolveRole
import me.courtboard.api.util.toJwtClaims
import net.coobird.thumbnailator.Thumbnails
import org.casbin.jcasbin.main.Enforcer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID

private const val THUMB_SIZE = 256
private const val URL_PREFIX = "/uploads/avatar/"

@Service
class MemberAvatarService(
    private val memberInfoRepository: MemberInfoRepository,
    private val jwtProvider: JwtProvider,
    private val enforcer: Enforcer,
    @Value("\${storage.path}")
    private val storagePath: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun uploadAvatar(memberId: String, file: MultipartFile): AvatarUpdateResDto {
        val info = memberInfoRepository.findById(UUID.fromString(memberId))
            .orElseThrow { CustomRuntimeException(HttpStatus.NOT_FOUND, "not found member") }
        if (info.provider == Constants.PROVIDER_GOOGLE) {
            throw CustomRuntimeException(HttpStatus.FORBIDDEN, "Google account avatar cannot be edited")
        }

        ImageValidationUtil.validate(file)

        val targetDir: Path = Paths.get(storagePath, "avatar")
        Files.createDirectories(targetDir)
        val target = targetDir.resolve("$memberId.png")

        file.inputStream.use { input ->
            Thumbnails.of(input)
                .size(THUMB_SIZE, THUMB_SIZE)
                .outputFormat("png")
                .toFile(target.toFile())
        }

        info.avatarUrl = "$URL_PREFIX$memberId.png"
        memberInfoRepository.save(info)

        return AvatarUpdateResDto(
            avatarUrl = info.avatarUrl,
            accessToken = issueAccessToken(info),
        )
    }

    @Transactional
    fun deleteAvatar(memberId: String): AvatarUpdateResDto {
        val info = memberInfoRepository.findById(UUID.fromString(memberId))
            .orElseThrow { CustomRuntimeException(HttpStatus.NOT_FOUND, "not found member") }
        if (info.provider == Constants.PROVIDER_GOOGLE) {
            throw CustomRuntimeException(HttpStatus.FORBIDDEN, "Google account avatar cannot be edited")
        }

        try {
            val target = Paths.get(storagePath, "avatar", "$memberId.png")
            Files.deleteIfExists(target)
        } catch (e: Exception) {
            log.warn("failed to delete avatar file for member {}", memberId, e)
        }

        info.avatarUrl = null
        memberInfoRepository.save(info)

        return AvatarUpdateResDto(
            avatarUrl = null,
            accessToken = issueAccessToken(info),
        )
    }

    private fun issueAccessToken(info: MemberInfoEntity): String {
        val role = enforcer.resolveRole(info.id.toString())
        return jwtProvider.generateAccessToken(info.email!!, info.toJwtClaims(role))
    }

}
