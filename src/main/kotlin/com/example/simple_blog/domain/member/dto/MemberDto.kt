package com.example.simple_blog.domain.member.dto

import com.example.simple_blog.domain.member.entity.Member

data class MemberDto(
    val id: Long,
    val email: String,
    val name: String,
    val nickname: String,
    val role: String
) {
    constructor(member: Member) : this(
        id = member.id ?: 0L,
        email = member.email,
        name = member.name,
        nickname = member.nickname,
        role = member.role.name
    )
}
