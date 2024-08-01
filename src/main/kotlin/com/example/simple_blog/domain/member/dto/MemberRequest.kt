package com.example.simple_blog.domain.member.dto

import com.example.simple_blog.domain.member.entity.Member
import jakarta.validation.constraints.NotBlank

data class MemberRequest(
    @field:NotBlank
    val email: String,

    @field:NotBlank
    val password: String,

    @field:NotBlank
    val name: String,

    @field:NotBlank
    val nickname: String
) {
    fun toEntity() = Member(
        email = email,
        password = password,
        name = name,
        nickname = nickname
    )
}
