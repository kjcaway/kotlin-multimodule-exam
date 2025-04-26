package me.courtboard.api.util

import org.mindrot.jbcrypt.BCrypt

object PasswordUtil {
    // 비밀번호 암호화 함수
    fun hashPassword(plainPassword: String): String {
        // BCrypt workFactor 설정 (10-12가 권장됨)
        val workFactor = 12
        val salt = BCrypt.gensalt(workFactor)
        return BCrypt.hashpw(plainPassword, salt)
    }

    // 비밀번호 검증 함수
    fun checkPassword(plainPassword: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(plainPassword, hashedPassword)
    }
}