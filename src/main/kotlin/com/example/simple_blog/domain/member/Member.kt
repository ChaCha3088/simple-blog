package com.example.simple_blog.domain.member

import com.example.simple_blog.domain.AuditingEntity
import com.example.simple_blog.enumstrorage.MemberRole
import com.example.simple_blog.enumstrorage.MemberRole.USER
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank

@Entity
class Member (
    email: String,
    password: String,
    name: String,
    nickname: String
): AuditingEntity() {
    @Column(nullable = false)
    @NotBlank
    var email: String = email
        protected set

    @Column(nullable = false)
    @NotBlank
    var password: String = password
        protected set

    @Column(nullable = false)
    @NotBlank
    var role: MemberRole = USER
        protected set

    @Column(nullable = false)
    @NotBlank
    var name: String = name
        protected set

    @Column(nullable = false)
    @NotBlank
    var nickname: String = nickname
        protected set
}