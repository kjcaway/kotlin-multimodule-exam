package me.multimoduleexam.util

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag(value = "unit")
class GeneratorUtilTest {

    @Test
    fun `show sample results`() {
        println("GeneratorUtil.generateRandomString(8): ${GeneratorUtil.generateRandomString(8)}")
        println("GeneratorUtil.generateUUIDWithoutDashes(): ${GeneratorUtil.generateUUIDWithoutDashes()}")
    }
}