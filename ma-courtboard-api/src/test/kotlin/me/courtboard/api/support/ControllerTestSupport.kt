package me.courtboard.api.support

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.courtboard.api.global.error.CustomExceptionHandler
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders

/**
 * standaloneSetup 기반 컨트롤러 단위 테스트 공통 지원.
 * 각 테스트는 이 클래스를 상속해 `buildMockMvc(controller)`로 MockMvc를 구성하고,
 * 공용 [objectMapper]로 요청 본문을 직렬화한다.
 */
abstract class ControllerTestSupport {

    protected val objectMapper: ObjectMapper = jacksonObjectMapper()

    protected fun buildMockMvc(controller: Any): MockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(CustomExceptionHandler())
            .build()
}
