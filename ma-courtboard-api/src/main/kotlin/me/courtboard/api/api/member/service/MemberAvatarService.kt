package me.courtboard.api.api.member.service

import me.courtboard.api.api.member.dto.AvatarUpdateResDto
import me.courtboard.api.api.member.entity.MemberInfoEntity
import me.courtboard.api.api.member.repository.MemberInfoRepository
import me.courtboard.api.component.JwtProvider
import me.courtboard.api.global.Constants
import me.courtboard.api.global.error.CustomRuntimeException
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

private const val MAX_FILE_SIZE = 5L * 1024 * 1024
private const val THUMB_SIZE = 256
private const val URL_PREFIX = "/uploads/avatar/"

private val ALLOWED_MIME_TYPES = setOf(
    "image/png",
    "image/jpeg",
    "image/webp",
    "image/gif",
)

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
        val memberInfo = memberInfoRepository.findById(UUID.fromString(memberId))
            ?: throw CustomRuntimeException(HttpStatus.NOT_FOUND, "not found member")
        val info = memberInfo.get()
        if (info.provider == "google") {
            throw CustomRuntimeException(HttpStatus.FORBIDDEN, "Google account avatar cannot be edited")
        }

        if (file.isEmpty) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "empty file")
        }
        if (file.size > MAX_FILE_SIZE) {
            throw CustomRuntimeException(HttpStatus.PAYLOAD_TOO_LARGE, "file too large (max 5MB)")
        }
        val mime = file.contentType?.lowercase()
            ?: throw CustomRuntimeException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "unsupported image type")
        if (mime !in ALLOWED_MIME_TYPES) {
            throw CustomRuntimeException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "unsupported image type")
        }

        val bytes = file.bytes
        verifyMagicBytes(bytes, mime)

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
        val memberInfo = memberInfoRepository.findById(UUID.fromString(memberId))
            ?: throw CustomRuntimeException(HttpStatus.NOT_FOUND, "not found member")
        val info = memberInfo.get()
        if (info.provider == "google") {
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
        val role = enforcer.getRolesForUserInDomain(info.id.toString(), Constants.COURTBOARD)
            .firstOrNull() ?: Constants.ROLE_USER
        return jwtProvider.generateAccessToken(
            info.email!!, mapOf(
                "id" to info.id.toString(),
                "name" to info.name!!,
                "email" to info.email!!,
                "role" to role,
                "avatarUrl" to (info.avatarUrl ?: "")
            )
        )
    }

    private fun verifyMagicBytes(bytes: ByteArray, mime: String) {
        val ok = when (mime) {
            "image/png" -> bytes.size >= 8 &&
                bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() &&
                bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte() &&
                bytes[4] == 0x0D.toByte() && bytes[5] == 0x0A.toByte() &&
                bytes[6] == 0x1A.toByte() && bytes[7] == 0x0A.toByte()
            "image/jpeg" -> bytes.size >= 3 &&
                bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()
            "image/gif" -> bytes.size >= 6 &&
                bytes[0] == 0x47.toByte() && bytes[1] == 0x49.toByte() &&
                bytes[2] == 0x46.toByte() && bytes[3] == 0x38.toByte() &&
                (bytes[4] == 0x37.toByte() || bytes[4] == 0x39.toByte()) &&
                bytes[5] == 0x61.toByte()
            "image/webp" -> bytes.size >= 12 &&
                bytes[0] == 0x52.toByte() && bytes[1] == 0x49.toByte() &&
                bytes[2] == 0x46.toByte() && bytes[3] == 0x46.toByte() &&
                bytes[8] == 0x57.toByte() && bytes[9] == 0x45.toByte() &&
                bytes[10] == 0x42.toByte() && bytes[11] == 0x50.toByte()
            else -> false
        }
        if (!ok) {
            throw CustomRuntimeException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "invalid image signature")
        }
    }
}
