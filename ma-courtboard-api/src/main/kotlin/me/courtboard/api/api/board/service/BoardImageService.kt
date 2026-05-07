package me.courtboard.api.api.board.service

import me.courtboard.api.api.board.dto.BoardImageResDto
import me.courtboard.api.api.board.entity.BoardImageEntity
import me.courtboard.api.api.board.repository.BoardImageRepository
import me.courtboard.api.global.error.CustomRuntimeException
import me.multimoduleexam.util.GeneratorUtil
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val URL_PREFIX = "/uploads/board/"
private const val MAX_FILE_SIZE = 5L * 1024 * 1024 // 5MB
private val ALLOWED_MIME_TYPES = mapOf(
    "image/png" to "png",
    "image/jpeg" to "jpg",
    "image/webp" to "webp",
    "image/gif" to "gif",
)

@Service
class BoardImageService(
    private val boardImageRepository: BoardImageRepository,
    @Value("\${storage.path}")
    private val storagePath: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun upload(file: MultipartFile, memberId: String): BoardImageResDto {
        if (file.isEmpty) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "empty file")
        }
        if (file.size > MAX_FILE_SIZE) {
            throw CustomRuntimeException(HttpStatus.PAYLOAD_TOO_LARGE, "file too large (max 5MB)")
        }
        val mime = file.contentType?.lowercase()
            ?: throw CustomRuntimeException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "unsupported image type")
        val ext = ALLOWED_MIME_TYPES[mime]
            ?: throw CustomRuntimeException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "unsupported image type")

        val bytes = file.bytes
        verifyMagicBytes(bytes, mime)

        val now = LocalDateTime.now()
        val subDir = now.format(DateTimeFormatter.ofPattern("yyyy/MM"))
        val id = GeneratorUtil.generateUUIDWithoutDashes()
        val fileName = "$id.$ext"
        val relativePath = "board/$subDir/$fileName"

        val targetDir: Path = Paths.get(storagePath, "board", subDir)
        Files.createDirectories(targetDir)
        val target = targetDir.resolve(fileName)
        Files.write(target, bytes)

        val entity = BoardImageEntity(
            id = id,
            boardId = null,
            filePath = relativePath,
            urlPath = "/uploads/$relativePath",
            mimeType = mime,
            fileSize = file.size,
            createdId = memberId,
        )
        boardImageRepository.save(entity)

        return BoardImageResDto(url = entity.urlPath)
    }

    /**
     * 게시물 contents의 <img src>를 파싱해 매칭되는 업로드 이미지의 board_id를 갱신한다.
     * 본인이 업로드한 이미지만, 그리고 아직 어디에도 묶이지 않은 row만 연결한다.
     */
    @Transactional
    fun linkToBoard(boardId: String, contentsHtml: String, memberId: String) {
        val urls = extractUploadedUrls(contentsHtml)
        if (urls.isEmpty()) return

        val candidates = boardImageRepository.findAllByUrlPathIn(urls)
        val toUpdate = candidates.filter {
            it.createdId == memberId && (it.boardId == null || it.boardId == boardId)
        }
        toUpdate.forEach { it.boardId = boardId }
        boardImageRepository.saveAll(toUpdate)
    }

    /**
     * 수정 시점에, 이전 board에 속해있던 이미지 중 새 contents에서 사라진 것만 파일+row 삭제한다.
     */
    @Transactional
    fun cleanupRemovedImages(boardId: String, newContentsHtml: String) {
        val current = boardImageRepository.findAllByBoardId(boardId)
        if (current.isEmpty()) return

        val keepUrls = extractUploadedUrls(newContentsHtml).toSet()
        val toDelete = current.filter { it.urlPath !in keepUrls }
        if (toDelete.isEmpty()) return

        toDelete.forEach { deleteFileSilently(it.filePath) }
        boardImageRepository.deleteAll(toDelete)
    }

    @Transactional
    fun deleteAllByBoard(boardId: String) {
        val rows = boardImageRepository.findAllByBoardId(boardId)
        if (rows.isEmpty()) return
        rows.forEach { deleteFileSilently(it.filePath) }
        boardImageRepository.deleteAll(rows)
    }

    /**
     * 24시간 이상 board에 연결되지 않은 임시 업로드 이미지를 정리한다.
     * 삭제된 row(=파일) 수를 반환한다.
     */
    @Transactional
    fun cleanupOrphans(): Int {
        val threshold = LocalDateTime.now().minusHours(1)
        val orphans = boardImageRepository.findOrphans(threshold)
        if (orphans.isEmpty()) return 0

        log.info("cleanupOrphans: deleting {} orphan images", orphans.size)
        orphans.forEach { deleteFileSilently(it.filePath) }
        boardImageRepository.deleteAll(orphans)
        return orphans.size
    }

    /**
     * 클라이언트가 보낸 Content-Type 만으로는 신뢰할 수 없으므로 파일 시그니처(매직 바이트)를
     * 직접 확인해 선언된 MIME과 실제 컨텐츠가 일치하는지 검증한다.
     */
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

    private fun extractUploadedUrls(html: String?): List<String> {
        if (html.isNullOrBlank()) return emptyList()
        val doc = Jsoup.parse(html)
        return doc.select("img")
            .mapNotNull { it.attr("src").takeIf { src -> src.isNotBlank() } }
            .mapNotNull { src ->
                runCatching {
                    if (src.startsWith("/")) src
                    else java.net.URI(src).rawPath
                }.getOrNull()
            }
            .filter { it.startsWith(URL_PREFIX) }
            .distinct()
    }

    private fun deleteFileSilently(relativePath: String) {
        try {
            val absolute = Paths.get(storagePath, relativePath)
            Files.deleteIfExists(absolute)
        } catch (e: Exception) {
            log.warn("failed to delete board image file: {}", relativePath, e)
        }
    }
}
