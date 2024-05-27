package me.multimoduleexam.util

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

open class DateUtil {
    companion object {
        private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        private val defaultFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

        /**
         * Get timestamp from string format ('YYYY-mm-dd HH:mi:ss')
         *
         * @param dateStr String
         * @return timestamp Timestamp
         */
        fun getTimestamp(dateStr: String): Timestamp {
            try {
                val date = simpleDateFormat.parse(dateStr)
                return Timestamp(date.time)
            } catch (e: Exception) {
                throw e
            }
        }

        /**
         * Get date string format from input timestamp ('YYYY-mm-dd HH:mi:ss')
         *
         * @param date Timestamp
         * @return dateStr String ('YYYY-mm-dd HH:mi:ss')
         */
        fun getDateString(date: Timestamp): String {
            try {
                return simpleDateFormat.format(date.time)
            } catch (e: Exception) {
                throw e
            }
        }

        /**
         * Get now timestamp
         *
         * @return timestamp
         */
        fun getNow(): Timestamp {
            return Timestamp(getNowMilliSecond())
        }

        /**
         * Get current time (millisecond)
         *
         * @return time Long
         */
        private fun getNowMilliSecond(): Long {
            return System.currentTimeMillis()
        }

        /**
         * Get now date string format ('YYYY-mm-dd HH:mi:ss')
         *
         * @return dateString String
         */
        fun getNowDateStr(): String {
            return simpleDateFormat.format(System.currentTimeMillis())
        }

        /**
         * Get plus date (min)
         */
        fun getPlusMinDate(tokenExpiredMin: Int): Date {
            return Date.from(
                LocalDateTime.now()
                    .plusMinutes(tokenExpiredMin.toLong())
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
            )
        }

        /**
         * Converts the given UTC time to KST and formats it using the provided pattern.
         */
        fun getTimeStrKst(dateTimeUtc: LocalDateTime, pattern: String): String {
            val formatter = DateTimeFormatter.ofPattern(pattern)
            return ZonedDateTime.of(dateTimeUtc, ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
                .format(formatter)
        }
    }
}