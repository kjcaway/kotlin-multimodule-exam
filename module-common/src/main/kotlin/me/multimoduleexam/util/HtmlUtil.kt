package me.multimoduleexam.util

import java.nio.charset.StandardCharsets

object HtmlUtil {
    fun createHtmlBody(filePath: String, args: Map<String, String?> = mapOf()): String {
        return convertWithArgs(readHtmlFile(filePath), args)
    }

    fun readHtmlFile(filePath: String): String {
        try {
            val inputStream = javaClass.classLoader.getResourceAsStream(filePath)
            val bytes = inputStream?.readBytes()
            return String(bytes!!, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun convertWithArgs(target: String, args: Map<String, String?>): String {
        var result = target
        for ((key, value) in args) {
            if (value != null) {
                result = result.replace(key, value)
            }
        }
        return result
    }
}