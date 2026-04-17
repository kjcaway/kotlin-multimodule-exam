package me.courtboard.api.api.member.service

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import me.courtboard.api.api.member.entity.MemberEntity
import me.courtboard.api.api.member.entity.MemberInfoEntity
import me.courtboard.api.api.member.repository.MemberInfoRepository
import me.courtboard.api.api.member.repository.MemberRepository
import me.courtboard.api.component.JwtProvider
import me.courtboard.api.global.Constants
import me.courtboard.api.global.error.CustomRuntimeException
import org.casbin.jcasbin.main.Enforcer
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class GoogleAuthService(
    private val memberRepository: MemberRepository,
    private val memberInfoRepository: MemberInfoRepository,
    private val jwtProvider: JwtProvider,
    private val enforcer: Enforcer,
    @Value("\${google.oauth.client-id}")
    private val clientId: String
) {
    companion object {
        private const val PROVIDER_GOOGLE = "google"
        private const val PROVIDER_LOCAL = "local"
    }

    private val verifier: GoogleIdTokenVerifier by lazy {
        GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(listOf(clientId))
            .build()
    }

    @Transactional
    fun loginWithGoogle(credential: String): Map<String, String> {
        val payload = verifyCredential(credential)

        val email = payload.email
            ?: throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Email not available from Google")
        if (payload.emailVerified != true) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Google email is not verified")
        }
        val sub = payload.subject
        val name = payload["name"] as? String ?: email.substringBefore("@")
        val picture = payload["picture"] as? String

        val memberInfo = memberInfoRepository.findByEmail(email)
            ?.also { existing ->
                existing.provider = PROVIDER_GOOGLE
                existing.providerUserId = sub
                if (existing.avatarUrl.isNullOrBlank()) existing.avatarUrl = picture
            }
            ?: createGoogleMember(email, name, picture, sub)

        val accessToken = jwtProvider.generateAccessToken(
            email, mapOf(
                "id" to memberInfo.id.toString(),
                "name" to memberInfo.name!!,
                "email" to memberInfo.email!!
            )
        )
        val refreshToken = jwtProvider.generateRefreshToken(email)

        memberInfo.lastloginAt = LocalDateTime.now()
        memberInfo.refreshToken = refreshToken
        memberInfoRepository.save(memberInfo)

        return mapOf(
            "access_token" to accessToken,
            "refresh_token" to refreshToken
        )
    }

    private fun verifyCredential(credential: String): GoogleIdToken.Payload {
        val idToken: GoogleIdToken = try {
            verifier.verify(credential)
        } catch (e: Exception) {
            throw CustomRuntimeException(HttpStatus.UNAUTHORIZED, "Invalid Google credential", e)
        } ?: throw CustomRuntimeException(HttpStatus.UNAUTHORIZED, "Invalid Google credential")

        return idToken.payload
    }

    private fun createGoogleMember(email: String, name: String, picture: String?, sub: String): MemberInfoEntity {
        val id = UUID.randomUUID()
        val info = MemberInfoEntity(
            id = id,
            email = email,
            name = name,
            avatarUrl = picture ?: "",
            provider = PROVIDER_GOOGLE,
            providerUserId = sub
        )
        val saved = memberInfoRepository.save(info)
        memberRepository.save(MemberEntity(id = id, passwd = ""))
        enforcer.addRoleForUserInDomain(id.toString(), Constants.ROLE_USER, Constants.COURTBOARD)
        return saved
    }
}
