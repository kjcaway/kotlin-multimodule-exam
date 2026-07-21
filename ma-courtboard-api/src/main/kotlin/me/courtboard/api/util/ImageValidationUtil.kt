package me.courtboard.api.util

import me.courtboard.api.global.error.CustomRuntimeException
import org.springframework.http.HttpStatus
import org.springframework.web.multipart.MultipartFile

/**
 * 업로드 이미지 공통 검증 유틸. 게시판 이미지/아바타 업로드가 공유한다.
 */
object ImageValidationUtil {
    const val MAX_FILE_SIZE = 5L * 1024 * 1024 // 5MB

    // mime -> 확장자
    val ALLOWED_MIME_TYPES = mapOf(
        "image/png" to "png",
        "image/jpeg" to "jpg",
        "image/webp" to "webp",
        "image/gif" to "gif",
    )

    /**
     * 빈 파일/크기/MIME 화이트리스트/매직 바이트를 검증하고, 검증된 (mime, bytes)를 반환한다.
     */
    fun validate(file: MultipartFile): Pair<String, ByteArray> {
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
        return mime to bytes
    }

    /**
     * 클라이언트가 보낸 Content-Type 만으로는 신뢰할 수 없으므로 파일 시그니처(매직 바이트)를
     * 직접 확인해 선언된 MIME과 실제 컨텐츠가 일치하는지 검증한다.
     */
    fun verifyMagicBytes(bytes: ByteArray, mime: String) {
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
