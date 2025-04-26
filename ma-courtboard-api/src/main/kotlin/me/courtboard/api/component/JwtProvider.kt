package me.courtboard.api.component

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider(
    @Value("\${jwt.secret}")
    private val secret: String,

    @Value("\${jwt.access-token-expiration}")
    private val accessTokenExpirationMs: Long,

    @Value("\${jwt.refresh-token-expiration}")
    private val refreshTokenExpirationMs: Long
) {
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))
    }

    fun generateAccessToken(userId: String, claimMap: Map<String, String>): String {
        val now = Date()
        val expiryDate = Date(now.time + accessTokenExpirationMs)

        return Jwts.builder()
            .subject(userId)
            .issuedAt(now)
            .expiration(expiryDate)
            .claim("name", claimMap["name"])
            .claim("email", claimMap["email"])
            .signWith(secretKey)
            .compact()
    }

    fun generateRefreshToken(userId: String): String {
        val now = Date()
        val expiryDate = Date(now.time + refreshTokenExpirationMs)

        return Jwts.builder()
            .subject(userId)
            .issuedAt(now)
            .expiration(expiryDate)
            .claim("token_type", "refresh")
            .signWith(secretKey)
            .compact()
    }

    // Extract all claims from token
    fun getAllClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    // Check if token is refresh token
    fun isRefreshToken(token: String): Boolean {
        val claims = getAllClaimsFromToken(token)
        return "refresh" == claims["token_type"]
    }
}