package me.courtboard.api.api.internal

import jakarta.servlet.http.HttpServletRequest
import me.courtboard.api.api.board.service.BoardImageService
import me.courtboard.api.global.dto.ApiResult
import me.courtboard.api.global.error.CustomRuntimeException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.InetAddress

// 내부(loopback) 호출 전용 엔드포인트.
// 운영자가 컨테이너 안에서 다음과 같이 호출한다고 가정한다.
//   docker exec courtboard-api curl -X POST http://127.0.0.1:8080/api/internal/board/images/cleanup-orphans
// reverse proxy/외부 클라이언트 등 loopback이 아닌 모든 요청은 404로 응답해 엔드포인트 존재 자체를 숨긴다.
@RestController
@RequestMapping("/api/internal")
class InternalController(
    private val boardImageService: BoardImageService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/board/images/cleanup-orphans")
    fun cleanupOrphans(request: HttpServletRequest): ApiResult<*> {
        requireLoopback(request)
        val deleted = boardImageService.cleanupOrphans()
        log.info("internal cleanupOrphans done: deleted={}", deleted)
        return ApiResult.ok(mapOf("deleted" to deleted))
    }

    private fun requireLoopback(request: HttpServletRequest) {
        val remote = request.remoteAddr
        val isLoopback = runCatching { InetAddress.getByName(remote).isLoopbackAddress }.getOrDefault(false)
        if (!isLoopback) {
            log.warn("internal endpoint blocked from non-loopback address: {}", remote)
            throw CustomRuntimeException(HttpStatus.NOT_FOUND)
        }
    }
}
