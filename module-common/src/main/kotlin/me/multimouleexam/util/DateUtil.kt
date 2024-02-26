package me.multimouleexam.util

import java.sql.Timestamp
import java.text.SimpleDateFormat

open class DateUtil {
    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        /**
         * Get Timestamp from string ('YYYY-mm-dd HH:mi:ss')
         *
         * @param dateStr String
         * @return timestamp Timestamp
         */
        fun getTimestamp(dateStr: String): Timestamp {
            try{
                val date = dateFormat.parse(dateStr)
                return Timestamp(date.time)
            } catch(e: Exception){
                throw e
            }
        }

        /**
         * Get Date String from timestamp ('YYYY-mm-dd HH:mi:ss')
         *
         * @param date Timestamp
         * @return dateStr String ('YYYY-mm-dd HH:mi:ss')
         */
        fun getDateString(date: Timestamp): String{
            try {
                return dateFormat.format(date.time)
            } catch (e: Exception) {
                throw e
            }
        }

        /**
         * Get Now timestamp
         *
         * @return timestamp
         */
        fun getNow(): Timestamp{
            return Timestamp(getNowMilliSecond())
        }

        /**
         * Get Now time(millisecond)
         *
         * @return time Long
         */
        private fun getNowMilliSecond(): Long{
            return System.currentTimeMillis()
        }

        /**
         * Get Now Date String
         *
         * @return dateString String
         */
        fun getNowDateStr(): String{
            return dateFormat.format(System.currentTimeMillis())
        }
    }
}