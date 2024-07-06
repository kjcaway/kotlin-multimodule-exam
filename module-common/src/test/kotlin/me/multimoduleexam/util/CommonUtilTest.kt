package me.multimoduleexam.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag(value = "unit")
class CommonUtilTest {

    @Test
    fun `test base64 encode & decode`() {
        val input = "abcd1q2w3e4r!@@@"
        println("input: $input")
        val encoded = CommonUtil.encodeBase64ToString(input)
        println("encoded: $encoded")

        val decoded = CommonUtil.decodeBase64ToString(encoded)
        println("decoded: $decoded")

        Assertions.assertEquals(input, decoded)
    }

    @Test
    fun `test gzip compress & decompress`() {
        val input =
            "abcd1q2w3e4r!@((!(!(abcd1q2w3e4r!@((!(!(abcd1q2w3e4r!@((!(!(abcd1q2w3e4r!@((!(!(abcd1q2w3e4r!@((!(!(abcd1q2w3e4r!@((!(!(abcd1q2w3e4r!@((!(!("

        val compressed = CommonUtil.compressGzip(input)
        println("compressed: ${CommonUtil.printByteArray(compressed)}")

        val decompressed = CommonUtil.decompressGzip(compressed)
        println("decompressed: $decompressed")

        Assertions.assertEquals(input, decompressed)
    }

    @Test
    fun `test gzip & base64`() {
        val input =
            "abcd1q2w3e4r!@((!(!(abcd1q2w3e4r!@((!(!(abcd1q2w3e4r!@((!(!(abcd1q2w3e4r!@((!(!(abcd1q2w3e4r!@((!(!(abcd1q2w3e4r!@((!(!(abcd1q2w3e4r!@((!(!("
        val compressed = CommonUtil.compressGzip(input)
        val encoded = CommonUtil.encodeBase64(compressed)
        println("encoded: $encoded")

        val decoded = CommonUtil.decodeBase64(encoded)
        val decompressed = CommonUtil.decompressGzip(decoded)
        println("decompressed: $decompressed")

        Assertions.assertEquals(input, decompressed)
    }
}