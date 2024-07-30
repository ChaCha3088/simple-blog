package com.example.simple_blog.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.validation.constraints.NotBlank

@Entity
class Post (
    title: String,
    content: String
): AuditingEntity() {
    @Column(nullable = false)
    @NotBlank
    var title: String = title
        protected set

    @Column(nullable = false)
    @NotBlank
    var content: String = content
        protected set
}