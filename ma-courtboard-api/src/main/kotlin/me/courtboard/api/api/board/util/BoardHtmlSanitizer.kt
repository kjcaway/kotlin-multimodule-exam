package me.courtboard.api.api.board.util

import org.jsoup.Jsoup
import org.jsoup.safety.Safelist

/**
 * 리치 텍스트 에디터(Quill)에서 전달된 HTML을 안전한 태그/속성만 남기도록 정화한다.
 *
 * 허용:
 * - 기본 인라인/블록 태그 (Safelist.basicWithImages 기준): a, b, br, code, em, i, p, strong, u, ul, ol, li, blockquote, ...
 * - 헤딩: h1, h2, h3
 * - img: src(data:/http/https), alt, height, width
 *
 * 차단:
 * - script, style, iframe, object, embed
 * - 모든 on* 이벤트 핸들러 속성
 * - javascript:, vbscript: 등 위험 프로토콜
 */
object BoardHtmlSanitizer {

    private val safelist: Safelist = Safelist.basicWithImages()
        .addTags("h1", "h2", "h3")
        .addAttributes("img", "width")
        .addProtocols("img", "src", "data")

    fun sanitize(html: String): String {
        return Jsoup.clean(html, safelist)
    }
}
