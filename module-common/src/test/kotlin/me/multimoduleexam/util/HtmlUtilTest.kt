package me.multimoduleexam.util

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag(value = "unit")
class HtmlUtilTest {

    @Test
    fun `read file`() {
        val contents = HtmlUtil.readHtmlFile("test.html")
        println(contents)
    }

    @Test
    fun `create html string`() {
        val map = mapOf(
            "##text##" to "This is a Data",
            "##value##" to "This is a Value"
        )
        val contents = HtmlUtil.createHtmlBody("test.html", map)
        println(contents)
    }
}