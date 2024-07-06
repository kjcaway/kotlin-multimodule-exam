package me.multimoduleexam.util

import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object CommonUtil {
    private fun ByteArray.toHexString() = joinToString("") { it.toString(16).padStart(2, '0') }

    fun encodeBase64ToString(input: String): String {
        return Base64.getEncoder().encodeToString(input.encodeToByteArray())
    }

    fun encodeBase64(input: ByteArray): String {
        return Base64.getEncoder().encodeToString(input)
    }

    fun decodeBase64ToString(input: String): String {
        return String(Base64.getDecoder().decode(input))
    }

    fun decodeBase64(input: String): ByteArray {
        return Base64.getDecoder().decode(input)
    }

    fun compressGzip(input: String): ByteArray {
        val bos = ByteArrayOutputStream()
        val gzip = GZIPOutputStream(bos);
        gzip.write(input.toByteArray(Charsets.UTF_8))
        gzip.flush()
        gzip.close()

        return bos.toByteArray();
    }

    fun decompressGzip(input: ByteArray): String {
        val gzip = GZIPInputStream(ByteArrayInputStream(input))
        val brd = BufferedReader(InputStreamReader(gzip, Charsets.UTF_8))
        val sb = StringBuilder()

        var line: String?
        while (brd.readLine().also { line = it } != null) {
            sb.append(line)
        }

        return sb.toString()
    }

    fun printByteArray(input: ByteArray): String {
        return input.toHexString()
    }
}