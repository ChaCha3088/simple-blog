package com.example.simple_blog.domain

import com.example.simple_blog.enumstrorage.MemberRole
import com.example.simple_blog.enumstrorage.MemberRole.USER
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank

@Entity
class Member (
    @Column(nullable = false)
    @NotBlank
    var email: String,

    @Column(nullable = false)
    @NotBlank
    var password: String,

    @Column(nullable = false)
    @NotBlank
    var role: MemberRole = USER,

    @Column(nullable = false)
    @NotBlank
    var name: String,

    @Column(nullable = false)
    @NotBlank
    var nickname: String
): AuditingEntity() {

}