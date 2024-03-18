package me.multimoduleexam.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class JsonUtil {
    companion object {
        private val mapper = jacksonObjectMapper()
        private val typeOfMap: TypeReference<Map<String, Any>> = object : TypeReference<Map<String, Any>>() {}

        /**
         * Get Object from json string
         *
         * @param jsonStr String?
         * @param type Clazz<T>
         * @return T
         */
        fun <T> convertToObject(jsonStr: String, type: Class<T>): T {
            return try {
                mapper.readValue(jsonStr, type)
            } catch (e: Exception) {
                throw e
            }
        }

        /**
         * Get json string from Object
         *
         * @param obj Any?
         * @return String
         */
        fun convertToJsonStr(obj: Any): String {
            return try {
                mapper.writeValueAsString(obj)
            } catch (e: Exception) {
                throw e
            }
        }

        /**
         * Get map from Object
         *
         * @param jsonStr String?
         * @return Map
         */
        fun convertToMap(jsonStr: String): Map<String, Any> {
            return try {
                mapper.readValue(jsonStr, typeOfMap)
            } catch (e: Exception) {
                throw e
            }
        }
    }
}
