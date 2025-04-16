package me.multimoduleexam.util

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("unit")
class UrlUtilTest {

    @Test
    fun `show sample results`() {
        println("UrlUtil.getRegex(\"/api/test/*\"): ${UrlUtil.getRegex("/api/test/*")}")
        println(
            "UrlUtil.match(\"/api/test/1\", UrlUtil.getRegex(\"/api/test/*\")): ${
                UrlUtil.match(
                    "/api/test/1",
                    UrlUtil.getRegex("/api/test/*")
                )
            }"
        )
    }

}