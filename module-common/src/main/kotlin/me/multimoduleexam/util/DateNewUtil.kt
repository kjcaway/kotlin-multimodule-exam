package me.multimoduleexam.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


object DateNewUtil {
    private const val DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS"
    val KST_ZONE: ZoneId = ZoneId.of("Asia/Seoul")

    /**
     * get now datetime string by default formatted(yyyy-MM-dd HH:mm:ss.SSS)
     *
     */
    fun getNowTimeStr(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DEFAULT_PATTERN))
    }

    /**
     * get now datetime(KST) string by default formatted(yyyy-MM-dd HH:mm:ss.SSS)
     *
     */
    fun getNowTimeStrKst(): String {
        return ZonedDateTime.now(KST_ZONE).format(DateTimeFormatter.ofPattern(DEFAULT_PATTERN))
    }

    /**
     * get now datetime string by formatted
     *
     */
    fun getNowTimeStrByFormat(format: String): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format))
    }

    /**
     * get now datetime(KST) string by formatted
     *
     */
    fun getNowTimeStrKstByFormat(format: String): String {
        return ZonedDateTime.now(KST_ZONE).format(DateTimeFormatter.ofPattern(format))
    }

    /**
     * get datetime string by formatted
     *
     */
    fun getTimeStrByFormat(time: LocalDateTime, format: String): String {
        return time.format(DateTimeFormatter.ofPattern(format))
    }

    /**
     * get datetime string by formatted(ZonedDateTime)
     *
     */
    fun getTimeStrByFormat(time: ZonedDateTime, format: String): String {
        return time.format(DateTimeFormatter.ofPattern(format))
    }

    /**
     * get datetime(KST) string by formatted
     *
     */
    fun getTimeStrByFormatInKst(time: LocalDateTime, format: String): String {
        return time.atZone(ZoneId.systemDefault())
            .withZoneSameInstant(KST_ZONE)
            .format(DateTimeFormatter.ofPattern(format))
    }

    /**
     * add days datetime
     *
     */
    fun getAddDatetime(day: Int): LocalDateTime {
        return LocalDateTime.now().plusDays(day.toLong())
    }

    /**
     * add days datetime(KST)
     *
     */
    fun getAddDatetimeInKst(day: Int): ZonedDateTime {
        return ZonedDateTime.now(KST_ZONE).plusDays(day.toLong())
    }

    /**
     * Localdatetime to ZonedDatetime
     *
     */
    fun convertToKst(time: LocalDateTime): ZonedDateTime {
        return time.atZone(ZoneId.systemDefault()).withZoneSameInstant(KST_ZONE)
    }
}