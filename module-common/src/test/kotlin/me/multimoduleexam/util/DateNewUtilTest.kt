package me.multimoduleexam.util

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Tag(value = "unit")
class DateNewUtilTest {

    @Test
    fun `show sample results`() {
        println("DateNewUtil.getNowTimeStr(): ${DateNewUtil.getNowTimeStr()}")
        println("DateNewUtil.getNowTimeStrKst(): ${DateNewUtil.getNowTimeStrKst()}")
        println("DateNewUtil.getNowTimeStrByFormat(\"yyyy-MM-dd HH:mm\"): ${DateNewUtil.getNowTimeStrByFormat("yyyy-MM-dd HH:mm")}")
        println("DateNewUtil.getNowTimeStrKstByFormat(\"yyyy-MM-dd HH:mm\"): ${DateNewUtil.getNowTimeStrKstByFormat("yyyy-MM-dd HH:mm")}")
        println(
            "DateNewUtil.getTimeStrByFormat(LocalDateTime.now(), \"yyyy-MM-dd HH:mm\"): ${
                DateNewUtil.getTimeStrByFormat(
                    LocalDateTime.now(), "yyyy-MM-dd HH:mm"
                )
            }"
        )
        println(
            "DateNewUtil.getTimeStrByFormat(ZonedDateTime.now(ZoneId.of(\"Asia/Seoul\")), \"yyyy-MM-dd HH:mm\"): ${
                DateNewUtil.getTimeStrByFormat(
                    ZonedDateTime.now(ZoneId.of("Asia/Seoul")), "yyyy-MM-dd HH:mm"
                )
            }"
        )
        println(
            "DateNewUtil.getTimeStrByFormat(ZonedDateTime.now(ZoneOffset.UTC), \"yyyy-MM-dd HH:mm\"): ${
                DateNewUtil.getTimeStrByFormat(
                    ZonedDateTime.now(ZoneOffset.UTC), "yyyy-MM-dd HH:mm"
                )
            }"
        )
    }
}