package me.courtboard.api.api.casbin.dto

data class PolicyResDto(
    val sub: String,
    val dom: String,
    val obj: String,
    val act: String,
)
