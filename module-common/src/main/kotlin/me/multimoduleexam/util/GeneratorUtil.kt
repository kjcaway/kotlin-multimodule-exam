package me.multimoduleexam.util

import java.util.*
import kotlin.random.Random

object GeneratorUtil {
    /**
     * get random string in char pool(0-9,a-z,A-Z)
     */
    fun generateRandomString(length: Int): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    /**
     * get uuid without -
     */
    fun generateUUIDWithoutDashes(): String {
        val uuid = UUID.randomUUID().toString().replace("-", "")
        return uuid
    }
}