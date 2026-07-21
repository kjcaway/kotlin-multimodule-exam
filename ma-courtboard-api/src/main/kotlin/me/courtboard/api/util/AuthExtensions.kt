package me.courtboard.api.util

import me.courtboard.api.api.member.entity.MemberInfoEntity
import me.courtboard.api.global.Constants
import org.casbin.jcasbin.main.Enforcer

/**
 * Casbin courtboard 도메인에서 회원의 첫 역할을 반환한다. 지정된 역할이 없으면 기본 user 역할.
 */
fun Enforcer.resolveRole(memberId: String): String =
    getRolesForUserInDomain(memberId, Constants.COURTBOARD).firstOrNull() ?: Constants.ROLE_USER

/**
 * access token 발급용 claim map (id/name/email/role/avatarUrl)을 구성한다.
 */
fun MemberInfoEntity.toJwtClaims(role: String): Map<String, String> = mapOf(
    "id" to id.toString(),
    "name" to name!!,
    "email" to email!!,
    "role" to role,
    "avatarUrl" to (avatarUrl ?: "")
)
